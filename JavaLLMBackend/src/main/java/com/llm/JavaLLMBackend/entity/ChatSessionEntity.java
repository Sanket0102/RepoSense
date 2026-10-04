package com.llm.JavaLLMBackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;

@Entity
@Table(name = "chat_session")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id",nullable = false)
    private UUID userId;

    @Column(name = "repo_id",nullable = false)
    private UUID repoId;

    @Column(name = "title",nullable = false,length = 200)
    private String title;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if (createdAt == null){
            createdAt = Instant.now();
        }
    }




}
