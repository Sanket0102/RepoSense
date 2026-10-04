package com.llm.JavaLLMBackend.exceptions;

public class RepoNotFoundException extends RuntimeException{

    public RepoNotFoundException(String message) {
        super(message);
    }
}
