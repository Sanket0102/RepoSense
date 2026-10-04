package com.llm.JavaLLMBackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GithubRateLimiter {

    @Value("${app.github.api-delay-ms}")
    private Long delayMs;

    public void pause(){
        if(delayMs <= 0){
            return;
        }
        try{
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
