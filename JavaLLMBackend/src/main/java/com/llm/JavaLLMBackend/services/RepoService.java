package com.llm.JavaLLMBackend.services;

import com.llm.JavaLLMBackend.client.GithubClient;
import com.llm.JavaLLMBackend.dto.response.IndexStatusResponse;
import com.llm.JavaLLMBackend.dto.response.RepoResponse;
import com.llm.JavaLLMBackend.entity.RepositoryEntity;
import com.llm.JavaLLMBackend.entity.UserEntity;
import com.llm.JavaLLMBackend.repositories.RepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RepoService {

    @Autowired
    private GithubClient githubClient;

    @Autowired
    private RepoRepository repoRepository;

    @Autowired
    private UsersServices userService;

    @Transactional
    public List<RepoResponse> syncAndListRepos(UUID userId){
        UserEntity user = userService.getUser(userId);
        String token = userService.decryptAccessToken(user);
        List<Map<String,Object>> remoteRepos = githubClient.fetchUserRepos(token);

        List<RepositoryEntity> saved = new ArrayList<>();

        remoteRepos.stream().forEach(map -> {
            Long gitRepoId = toLong(map.get("id"));
            RepositoryEntity repo = repoRepository.findByUserIdAndRepoId(userId,gitRepoId)
                    .orElseGet(RepositoryEntity::new);
            String fullName = String.valueOf(map.get("full_name"));
            String[] parts = fullName.split("/",2);

            String repoOwner = null;
            if(repo.getOwner() == null || repo.getOwner().isBlank()){
                Object ownerObj = map.get("owner");
                if(ownerObj instanceof Map<?,?> ownerMap && ownerMap.get("login") != null){
                    repoOwner = String.valueOf(ownerMap.get("login"));
                }
            }

            RepositoryEntity repository = RepositoryEntity.builder()
                    .userId(userId)
                    .repoId(gitRepoId)
                    .owner(parts.length > 0 ? parts[0] : String.valueOf(map.get("owner")))
                    .name(parts.length > 1 ? parts[1] : String.valueOf(map.get("owner")))
                    .isPrivate((boolean)map.get("private"))
                    .fullName(fullName)
                    .branch(map.get("default_branch") != null ?
                            map.get("default_branch").toString() : "main")
                    .language(map.get("language") != null ?
                            map.get("language").toString() : null)
                    .description(map.get("description") != null ? map.get("description").toString() : null)
                    .updatedAt(Instant.now())
                    .owner(repoOwner)
                    .build();
            RepositoryEntity savedRepositoryEntity = null;
            if(!repoRepository.existsByRepoId(repository.getRepoId())){
                savedRepositoryEntity = repoRepository.save(repository);
            }
            saved.add(repository);
        });
        List<RepoResponse> repoResponses =  saved.stream()
                .map(RepoService :: toResponse)
                .collect(Collectors.toList());
        return repoResponses;
    }

    @Transactional(readOnly = true)
    public List<RepoResponse> listStored(UUID userId){
        List<RepoResponse> repoResponses = repoRepository.findByUserIdOrderByFullNameAsc(userId)
                .stream()
                .map(RepoService :: toResponse)
                .toList();
        return repoResponses;
    }

    @Transactional(readOnly = true)
    public RepositoryEntity requiredOwned(UUID repoId, UUID userId){
        System.out.println(" Repo Id :" + repoId);
        System.out.println(" User Id :" + userId);
        RepositoryEntity requiredRepository =  repoRepository.findByIdAndUserId(repoId,userId)
                .orElseThrow(() ->new RuntimeException(" Repo Not Found for Repo Id" + repoId ));
        return requiredRepository;
    }

    @Transactional(readOnly = true)
    public IndexStatusResponse status(UUID repoId, UUID userId){
        RepositoryEntity repo = requiredOwned(repoId, userId);
        return IndexStatusResponse.builder()
                .indexStatus(repo.getIndexStatus())
                .filesProcessed(repo.getFilesProcessed())
                .chunkCount(repo.getChunkCount())
                .totalFiles(repo.getTotalFiles())
                .indexedAt(repo.getIndexedAt())
                .errorMessages(repo.getErrorMessages())
                .build();
    }

    public static RepoResponse toResponse(RepositoryEntity repositoryEntity){
        RepoResponse repoResponse = RepoResponse.builder()
                .id(repositoryEntity.getId())
                .repoId(repositoryEntity.getRepoId())
                .owner(repositoryEntity.getOwner())
                .name(repositoryEntity.getName())
                .fullName(repositoryEntity.getFullName())
                .isPrivate(repositoryEntity.isPrivate())
                .defaultBranch(repositoryEntity.getBranch())
                .language(repositoryEntity.getLanguage())
                .htmlUrl(repositoryEntity.getHtmlUrl())
                .description(repositoryEntity.getDescription())
                .indexedAt(repositoryEntity.getIndexedAt())
                .indexStatus(repositoryEntity.getIndexStatus())
                .chunkCount(repositoryEntity.getChunkCount())
                .filesProcessed(repositoryEntity.getFilesProcessed())
                .errorMessages(repositoryEntity.getErrorMessages())
                .build();
        return repoResponse;
    }

    private static Long toLong(Object obj){
        if(obj instanceof Number num){
            return num.longValue();
        }
        return Long.parseLong(String.valueOf(obj));
    }
}
