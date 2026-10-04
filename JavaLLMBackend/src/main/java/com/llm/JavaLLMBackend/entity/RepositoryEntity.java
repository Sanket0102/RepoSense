package com.llm.JavaLLMBackend.entity;

import com.llm.JavaLLMBackend.enums.IndexStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "repository",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id","repo_id" })
       }
)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id" , nullable = false)
    private UUID userId;

    @Column(name = "repo_id" , nullable = false)
    private Long repoId;

    @Column(name = "owner" , length = 50)
    private String owner;

    @Column(name = "name" , length = 50)
    private String name;

    @Column(name = "full_name" , length = 150)
    private String fullName;

    @Column(name = "is_private")
    private boolean isPrivate;

    @Column(name = "language")
    private String language;

    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    @Column(name = "branch")
    private String branch;

    @Column(name = "description" , columnDefinition = "TEXT")
    private String description;

    @Column(name = "index_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private IndexStatus indexStatus;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name = "chunk_count")
    private int chunkCount;

    @Column(name = "total_files")
    private int totalFiles;

    @Column(name = "files_processed")
    private int filesProcessed;

    @Column(name = "error_messages")
    private String errorMessages;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    private void onCreate(){
        if(createdAt == null){
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
        if(indexStatus == null){
            indexStatus = IndexStatus.PENDING;
        }
    }


}
