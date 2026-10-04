package com.llm.JavaLLMBackend.dto.response;

import com.llm.JavaLLMBackend.enums.IndexStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexStatusResponse {

    private UUID repositoryId;
    private IndexStatus indexStatus;
    private int totalFiles;
    private int filesProcessed;
    private int chunkCount;
    private Instant indexedAt;
    private String errorMessages;

}
