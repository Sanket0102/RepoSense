package com.llm.JavaLLMBackend.indexing;

import com.llm.JavaLLMBackend.client.GithubClient;
import com.llm.JavaLLMBackend.entity.RepositoryEntity;
import com.llm.JavaLLMBackend.enums.IndexStatus;
import com.llm.JavaLLMBackend.exceptions.RepoNotFoundException;
import com.llm.JavaLLMBackend.repositories.RepoRepository;
import com.llm.JavaLLMBackend.services.RepoService;
import com.llm.JavaLLMBackend.services.UsersServices;
import com.llm.JavaLLMBackend.utils.GithubRateLimiter;
import org.springframework.ai.vectorstore.VectorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingService {

    private static final int VECTOR_BATCH_SIZE = 32;
    private static final int PROGRESS_EVERY_N_FILES = 5;

    @Autowired
    private RepoRepository repoRepository;

    @Autowired
    private UsersServices usersServices;

    @Autowired
    private GithubClient githubClient;

    @Autowired
    private CodeFileFilter codeFileFilter;

    @Autowired
    private CodeChunker codeChunker;

    @Autowired
    private GithubRateLimiter githubRateLimiter;

    private final VectorStore vectorStore;

    @Value("${app.indexing.max-file-bytes}")
    private long maxFileBytes;

    public RepositoryEntity startIndexing(UUID userId, UUID repoId){
        RepositoryEntity repository = repoRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repo Not Found Exception"));

        if(repository.getIndexStatus().equals(IndexStatus.INDEXING)){
            throw new RuntimeException("Indexing is already in progress");
        }

        repository.setIndexStatus(IndexStatus.INDEXING);
        repository.setIndexedAt(Instant.now());
        repository.setUpdatedAt(Instant.now());
        repository.setChunkCount(0);
        repository.setFilesProcessed(0);
        repository.setErrorMessages(null);
        return repoRepository.save(repository);
    }

    @Async
    public void indexAsync(RepositoryEntity repository, UUID repoId, UUID userId){
        try{
            doIndex(repoId,userId);
        }
        catch (Exception ex){
            log.error("Indexing failed for Repo {}",repoId,ex);
            repository.setIndexStatus(IndexStatus.FAILED);
        }
    }

    private void doIndex(UUID repoId, UUID userId) {
        RepositoryEntity repository = repoRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repo Not Found"));

        String token = usersServices.decryptAccessToken(usersServices.getUser(userId));

        deleteExistingVectors(repoId.toString());

        Map<String,Object> tree = githubClient.fetchRepoTree(token,repository.getOwner(),repository.getName(),repository.getBranch());
        List<String> filepaths = listIndexableFiles(tree);

        updateProgress(repoId,filepaths.size(),0,0,IndexStatus.INDEXING,null);

        List<Document> batch = new ArrayList<>();
        int processed = 0;
        int totalChunks = 0;

        for (String file : filepaths){
            try{
                String fileContent = githubClient.fetchFileContent(token, repository.getOwner(), repository.getName(), file);
                List<Document> chunks = codeChunker.chunkFile(repoId.toString(),file,fileContent);
                batch.addAll(chunks);
                totalChunks += chunks.size();
                if(batch.size() >= VECTOR_BATCH_SIZE){
                    vectorStore.add(batch);
                    batch.clear();
                }
            }
            catch (Exception ex){
                log.warn("Skipping file {} in {} : {}",file,repository.getFullName(),ex.getMessage());
            }
            processed++;
            if(processed % PROGRESS_EVERY_N_FILES == 0 || processed == filepaths.size()){
                updateProgress(repoId , filepaths.size(),processed,totalChunks,IndexStatus.INDEXING,null);
            }
            githubRateLimiter.pause();
        }

        if(!batch.isEmpty()){
            vectorStore.add(batch);
        }
        markReady(repoId,filepaths.size(),processed,totalChunks,repository.getFullName());
    }

    private void markReady(UUID repoId, int totalFiles, int processedFiles, int totalChunks, String fullName) {
        repoRepository.findById(repoId).ifPresent(repo -> {
            repo.setIndexStatus(IndexStatus.READY);
            repo.setTotalFiles(totalFiles);
            repo.setFilesProcessed(processedFiles);
            repo.setChunkCount(totalChunks);
            repo.setIndexedAt(Instant.now());
            repo.setErrorMessages(null);
            repo.setUpdatedAt(Instant.now());
            repoRepository.save(repo);
        });
        log.info("Indexed {} files ({} chunks) for {}", processedFiles, totalChunks, fullName);
    }

    @Transactional
    private void updateProgress(UUID repoId, int total, int processed, int chunks, IndexStatus indexStatus, String error) {
        RepositoryEntity repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new RepoNotFoundException("Repo with id :- " + repoId + " not found "));
        repo.setTotalFiles(total);
        repo.setFilesProcessed(processed);
        repo.setChunkCount(chunks);
        repo.setIndexStatus(indexStatus);
        repo.setErrorMessages(error);
        repo.setUpdatedAt(Instant.now());
        repoRepository.save(repo);

    }

    private List<String> listIndexableFiles(Map<String, Object> tree) {
        if(tree == null || tree.get("tree") == null){
            return List.of();
        }

        List<Map<String,Object>> entries = (List<Map<String,Object>>) tree.get("tree");
        List<String> files =  entries.stream()
                .filter(entry -> String.valueOf(entry.get("type")).equals("blob"))
                .filter(entry -> {
                    String path = entry.get("path").toString();
                    System.out.println(path);
                    System.out.println(entry.get("size") instanceof Number);
                    long size = entry.get("size") instanceof Number n ? n.longValue() : 0L;
                    return codeFileFilter.isEligible(path,size,maxFileBytes);
                })
                .map(entry -> String.valueOf(entry.get("path")))
                .toList();

        return files;

    }

    private void deleteExistingVectors(String repoId) {
        try{
            var filter = new FilterExpressionBuilder().eq(RagSettings.METADATA_REPO_ID,repoId).build();
            vectorStore.delete(filter);
        } catch (RuntimeException e) {
            log.warn("Could not Delete existing Vector for repo {} : {}", repoId,e.getMessage());
        }
    }


}
