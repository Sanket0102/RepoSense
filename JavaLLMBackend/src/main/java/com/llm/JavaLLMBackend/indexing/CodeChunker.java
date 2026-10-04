package com.llm.JavaLLMBackend.indexing;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class CodeChunker {

    private final TokenTextSplitter tokenTextSplitter;
    private final CodeFileFilter codeFileFilter;

    public CodeChunker(
            CodeFileFilter codeFileFilter,
            @Value("${app.indexing.chunk-size}") int chunkSize) {
        int chunkTokens = Math.max(50, chunkSize / 4);

        this.tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(chunkTokens)
                .build();

        this.codeFileFilter = codeFileFilter;
    }

    public List<Document> chunkFile(String repoId, String filePath, String content){
        if(content == null || content.isBlank()){
            return List.of();
        }

        String language = codeFileFilter.detectLanguage(filePath);
        String header = "// File: " + filePath + "\n";

        Document source = new Document(header + content, baseMetaData(repoId,filePath,language));
        List<Document> split = tokenTextSplitter.apply(List.of(source));

        return IntStream.range(0, split.size())
                .mapToObj(i -> withChunkIndex(split.get(i) , repoId, filePath,language,i))
                .toList();
    }

    private static Document withChunkIndex(Document chunk, String repoId, String filePath, String language,int chunkIndex) {
        Map<String,Object> metadata = new HashMap<>(chunk.getMetadata());
        metadata.put(RagSettings.METADATA_REPO_ID,repoId);
        metadata.put("filepath",filePath);
        metadata.put("language",language);
        metadata.put("chunkIndex", chunkIndex);
        return new Document(chunk.getText(),metadata);
    }

    private static Map<String, Object> baseMetaData(String repoId, String filePath, String language) {
        Map<String,Object> metadata = new HashMap<>();
        metadata.put(RagSettings.METADATA_REPO_ID,repoId);
        metadata.put("filepath",filePath);
        metadata.put("language",language);
        return metadata;
    }




}
