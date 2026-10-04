package com.llm.JavaLLMBackend.exceptions;

public class UserNotFoundException extends RepoNotFoundException{

    public UserNotFoundException(String message) {
        super(message);
    }
}
