package com.llm.JavaLLMBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class CryptoConfig {

    @Value("${app.token-encryptor-password}")
    private String password;

    @Value("${app.token-encryptor-salt}")
    private String salt;

    @Bean
    private TextEncryptor tokenEncryptor(){
        return Encryptors.text(password,salt);
    }
}
