package com.llm.JavaLLMBackend.dto.response;

import com.llm.JavaLLMBackend.dto.CitationDto;
import com.llm.JavaLLMBackend.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private UUID uuid;
    private MessageRole messageRole;
    private String content;
    private List<CitationDto> citationDtos;
    private Instant createdAt;
}
