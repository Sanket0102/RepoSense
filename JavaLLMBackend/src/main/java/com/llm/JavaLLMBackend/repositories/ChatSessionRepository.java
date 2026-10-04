package com.llm.JavaLLMBackend.repositories;

import com.llm.JavaLLMBackend.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, UUID> {

    List<ChatSessionEntity> findByUserIdAndRepoIdOrderByCreatedAtDesc(UUID userId,UUID repoID);

    Optional<ChatSessionEntity> findByIdAndUserId(UUID id,UUID userId);
}
