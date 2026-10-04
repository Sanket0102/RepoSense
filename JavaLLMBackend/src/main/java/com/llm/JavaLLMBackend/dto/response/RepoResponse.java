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
public class RepoResponse {

    private UUID id;
    private Long repoId;
    private String owner;
    private String name;
    private String fullName;
    private boolean isPrivate;
    private String defaultBranch;
    private String language;
    private String htmlUrl;
    private String description;
    private IndexStatus indexStatus;
    private Instant indexedAt;
    private int chunkCount;
    private int filesTotal;
    private int filesProcessed;
    private String errorMessages;
}
