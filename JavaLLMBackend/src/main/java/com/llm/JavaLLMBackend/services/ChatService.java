package com.llm.JavaLLMBackend.services;

import com.llm.JavaLLMBackend.dto.request.ChatSessionRequest;
import com.llm.JavaLLMBackend.dto.response.ChatMessageResponse;
import com.llm.JavaLLMBackend.dto.response.ChatSessionResponse;
import com.llm.JavaLLMBackend.entity.ChatMessageEntity;
import com.llm.JavaLLMBackend.entity.ChatSessionEntity;
import com.llm.JavaLLMBackend.entity.RepositoryEntity;
import com.llm.JavaLLMBackend.enums.IndexStatus;
import com.llm.JavaLLMBackend.enums.MessageRole;
import com.llm.JavaLLMBackend.indexing.ChatPromptBuilder;
import com.llm.JavaLLMBackend.indexing.ChatStreamHandler;
import com.llm.JavaLLMBackend.indexing.CitationMapper;
import com.llm.JavaLLMBackend.indexing.CodeContextRetriever;
import com.llm.JavaLLMBackend.repositories.ChatMessageRepository;
import com.llm.JavaLLMBackend.repositories.ChatSessionRepository;
import com.llm.JavaLLMBackend.repositories.RepoRepository;
import com.llm.JavaLLMBackend.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final RepoRepository repoRepository;
    private final UsersRepository usersRepository;
    private final RepoService repoService;
    private final CitationMapper citationMapper;
    private final CodeContextRetriever codeContextRetriever;
    private final ChatPromptBuilder chatPromptBuilder;
    private final ChatStreamHandler chatStreamHandler;

    @Transactional
    public ChatSessionResponse createSession(UUID userId, ChatSessionRequest chatSessionRequest){
        RepositoryEntity repository = repoService.requiredOwned(chatSessionRequest.getRepoId(),userId);

        if(repository.getIndexStatus() != IndexStatus.READY){
            throw new RuntimeException("Repository is not Ready");
        }
        String title = null;
        if(chatSessionRequest.getTitle() != null && !chatSessionRequest.getTitle().isBlank()){
            title = chatSessionRequest.getTitle();
        }
        else{
            title = "Chat with " + repository.getFullName();
        }

        ChatSessionEntity chatSessionEntity = ChatSessionEntity.builder()
                .title(title)
                .userId(userId)
                .repoId(repository.getId())
                .build();
        return toChatSessionResponse(chatSessionRepository.save(chatSessionEntity));
    }

    private static ChatSessionResponse toChatSessionResponse(ChatSessionEntity chatSessionEntity) {
        return ChatSessionResponse.builder()
                .id(chatSessionEntity.getId())
                .repoId(chatSessionEntity.getRepoId())
                .createdAt(Instant.now())
                .title(chatSessionEntity.getTitle())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> getAllChatSessions(UUID usedId, UUID repoId){
        return chatSessionRepository.findByUserIdAndRepoIdOrderByCreatedAtDesc(usedId,repoId)
                .stream()
                .map(ChatService::toChatSessionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse requiredSession(UUID userId, UUID sessionId){
        return toChatSessionResponse(chatSessionRepository.findByIdAndUserId(sessionId,userId)
                .orElseThrow(() -> new RuntimeException(" Session Not found")));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> chatMessageResponses(UUID sessionId,UUID userId){
        ChatSessionResponse chatSessionResponse = requiredSession(userId,sessionId);
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(chatSessionResponse.getId())
                .stream()
                .map(this::toChatMessageResponse)
                .toList();
    }

    public SseEmitter streamReply(UUID userId, UUID sessionId, String content){
        ChatSessionResponse chatSessionResponse = requiredSession(userId,sessionId);
        RepositoryEntity repo = repoService.requiredOwned(chatSessionResponse.getRepoId(),userId);

        if(repo.getIndexStatus() != IndexStatus.READY){
            throw new RuntimeException("Chat Repository is Not indexed");
        }

        ChatMessageEntity chatMessage = chatMessageRepository.save(
                ChatMessageEntity.builder()
                        .sessionId(chatSessionResponse.getId())
                        .messageRole(MessageRole.USER_ROLE)
                        .content(content)
                        .build()
        );

        var retrievedContext = codeContextRetriever.retrieveContext(repo.getId(),content);

        String systemPrompt = chatPromptBuilder.systemPrompt(repo.getFullName());
        String userPrompt = chatPromptBuilder.userPrompt(retrievedContext.getContextText(),content);

        return chatStreamHandler.stream(sessionId,
                toChatMessageResponse(chatMessage),
                retrievedContext.getCitationDtoList(),
                systemPrompt,
                userPrompt);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessageEntity chatMessageEntity) {
        return ChatMessageResponse.builder()
                .uuid(chatMessageEntity.getId())
                .messageRole(chatMessageEntity.getMessageRole())
                .citationDtos(citationMapper.fromJson(chatMessageEntity.getCitations()))
                .content(chatMessageEntity.getContent())
                .createdAt(chatMessageEntity.getCreatedAt())
                .build();
    }


}
