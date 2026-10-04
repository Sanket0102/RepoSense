package com.llm.JavaLLMBackend.controller;

import com.llm.JavaLLMBackend.dto.request.ChatMessageRequest;
import com.llm.JavaLLMBackend.dto.request.ChatSessionRequest;
import com.llm.JavaLLMBackend.dto.response.ChatMessageResponse;
import com.llm.JavaLLMBackend.dto.response.ChatSessionResponse;
import com.llm.JavaLLMBackend.security.CurrentUser;
import com.llm.JavaLLMBackend.services.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final CurrentUser currentUser;
    private final ChatService chatService;

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createSession(
            @Valid @RequestBody ChatSessionRequest chatSessionRequest){
        UUID userId = currentUser.require().getId();
        return ResponseEntity.ok(chatService.createSession(userId,chatSessionRequest));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionResponse>> listSession(
            @RequestParam UUID repoId){
        UUID userId = currentUser.require().getId();
        return ResponseEntity.ok(chatService.getAllChatSessions(userId,repoId));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable("id") UUID sessionId
    ){
        UUID userId = currentUser.require().getId();
        return ResponseEntity.ok(chatService.chatMessageResponses(sessionId,userId));
    }

    @PostMapping(value = "/sessions/{id}/messages",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(@PathVariable UUID id, @RequestBody ChatMessageRequest messageRequest){
        UUID userId = currentUser.require().getId();
        return chatService.streamReply(userId,id, messageRequest.getContent());

    }


}
