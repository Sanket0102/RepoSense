package com.llm.JavaLLMBackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "github_id",unique = true,nullable = false)
    private Long githubId;

    @Column(name = "github_username", nullable = false,length = 100)
    private String githubUsername;

    @Column(name = "display_name", nullable = false,length = 200)
    private String displayName;

    @Column(name = "avatar_url", length = 400)
    private String avatarUrl;

    @Column(name = "access_token" , length = 500, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "token_scopes", length = 500)
    private String tokenScopes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if(createdAt == null){
            createdAt = Instant.now();
        }
    }
}
