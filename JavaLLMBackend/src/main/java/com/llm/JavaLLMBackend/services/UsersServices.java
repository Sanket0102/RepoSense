package com.llm.JavaLLMBackend.services;

import com.llm.JavaLLMBackend.entity.UserEntity;
import com.llm.JavaLLMBackend.repositories.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersServices {

    private final UsersRepository usersRepository;
    private final TextEncryptor textEncryptor;

    @Transactional
    public UserEntity getUser(UUID uuid){
        return usersRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    public String decryptAccessToken(UserEntity user){
        return textEncryptor.decrypt(user.getAccessToken());
    }

    private static Long toLong(Object value){
        if(value instanceof Number number){
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    public UserEntity upsertFromGithub(Map<String, Object> attributes, String accessToken, String scopes) {
        Long githubId = toLong(attributes.get("id"));
        String login = String.valueOf(attributes.get("login"));
        String name = attributes.get("name") != null
                ? String.valueOf(attributes.get("name")) : login;
        String avatarUrl = attributes.get("avatar_url") != null ? String.valueOf(attributes.get("avatar_url")):null;
        String encryptedToken = textEncryptor.encrypt(accessToken);

        UserEntity user = usersRepository.findByGithubId(githubId).orElseGet(UserEntity::new);
        user.setGithubId(githubId);
        user.setGithubUsername(name);
        user.setAvatarUrl(avatarUrl);
        user.setAccessToken(encryptedToken);
        user.setDisplayName(name);
        user.setTokenScopes(scopes);

        usersRepository.save(user);
        return user;
    }
}
