package com.llm.JavaLLMBackend.indexing;

import com.llm.JavaLLMBackend.dto.CitationDto;
import com.llm.JavaLLMBackend.dto.response.ChatMessageResponse;
import com.llm.JavaLLMBackend.entity.ChatMessageEntity;
import com.llm.JavaLLMBackend.enums.MessageRole;
import com.llm.JavaLLMBackend.repositories.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatStreamHandler {

    private final ChatModel chatModel;
    private final ChatMessageRepository chatMessageRepository;
    private final CitationMapper citationMapper;
    private SseEmitter sseEmitter;

    public SseEmitter stream(UUID sessionId,
                             ChatMessageResponse savedUserMessage,
                             List<CitationDto> citationDtoList,
                             String systemPrompt,
                             String userPrompt){
        sseEmitter = new SseEmitter(RagSettings.STREAM_TIMEOUT_MS);
        StringBuilder fullReply = new StringBuilder();

        try{
            sseEmitter.send(
                    SseEmitter.event()
                            .name("User_Message")
                            .data(savedUserMessage)
            );

            ChatClient.builder(chatModel)
                    .build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .stream()
                    .content()
                    .doOnNext(token -> appendToken(sseEmitter,fullReply,token))
                    .doOnError(err ->{
                        log.error("Chat Stream Error",err);
                        sseEmitter.completeWithError(err);
                    })
                    .doOnComplete(() -> completeStream(
                            sseEmitter, sessionId,fullReply, citationDtoList
                    ))
                    .subscribe();
        }
        catch (IOException e){
            sseEmitter.completeWithError(e);
        }
        return sseEmitter;
    }

    private void completeStream(SseEmitter sseEmitter, UUID sessionId, StringBuilder fullReply, List<CitationDto> citationDtos) {

        try {
            ChatMessageEntity assistance = chatMessageRepository.save(
                    ChatMessageEntity.builder()
                            .sessionId(sessionId)
                            .messageRole(MessageRole.ASSISTANT)
                            .content(fullReply.toString())
                            .citations(citationMapper.toJson(citationDtos))
                            .build()
            );

            sseEmitter.send(SseEmitter.event()
                    .name("assistance_message")
                    .data(toMessageResponse(assistance))
            );

            sseEmitter.send(
                    SseEmitter.event()
                            .name("done")
                            .data("[DONE]")
            );
            sseEmitter.complete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private ChatMessageResponse toMessageResponse(ChatMessageEntity assistance) {
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse();
        chatMessageResponse = ChatMessageResponse.builder()
                .messageRole(assistance.getMessageRole())
                .citationDtos(citationMapper.fromJson(assistance.getCitations()))
                .build();
        return chatMessageResponse;
    }

    private void appendToken(SseEmitter sseEmitter, StringBuilder fullReply, String token) {
        fullReply.append(token);
        try {
            sseEmitter.send(
                    SseEmitter.event()
                            .name("token")
                            .data(token, MediaType.APPLICATION_JSON)
            );
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
