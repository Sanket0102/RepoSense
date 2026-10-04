package com.llm.JavaLLMBackend.entity;

import com.llm.JavaLLMBackend.enums.MessageRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id",nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_role",nullable = false)
    private MessageRole messageRole;

    @Column(columnDefinition = "TEXT")
    private String citations;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate(){
        if(createdAt == null){
            createdAt = Instant.now();
        }
    }




}
