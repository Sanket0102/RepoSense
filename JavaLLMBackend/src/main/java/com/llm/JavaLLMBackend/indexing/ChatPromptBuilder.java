package com.llm.JavaLLMBackend.indexing;

import org.springframework.stereotype.Component;

@Component
public class ChatPromptBuilder {

    public String systemPrompt(String repositoryFullName){
        return """
                You are an expert assistance for the %s codebase.
                Answer using Only the provided context.
                If the context is insufficient, say you are unaware
                Cite filepaths and line ranges when relevant.
                Be concise and technical.
                """.formatted(repositoryFullName);
    }

    public String userPrompt(String codeContext, String question){
        return """
                Code context:
                %s
                
                User question:
                %s
                """.formatted(codeContext,question);
    }
}
