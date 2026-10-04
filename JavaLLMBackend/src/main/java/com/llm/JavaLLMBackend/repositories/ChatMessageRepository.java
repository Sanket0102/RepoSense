package com.llm.JavaLLMBackend.repositories;

import com.llm.JavaLLMBackend.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    public List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

}
