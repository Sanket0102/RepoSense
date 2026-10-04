package com.llm.JavaLLMBackend.controller;

import com.llm.JavaLLMBackend.dto.response.UserResponse;
import com.llm.JavaLLMBackend.entity.UserEntity;
import com.llm.JavaLLMBackend.security.AppUserPrincipal;
import com.llm.JavaLLMBackend.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final CurrentUser currentUser;

    @GetMapping("/login-url")
    public Map<String,String> loginUrl(){
        return Map.of("url","/oauth2/authorization/github");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(){
        AppUserPrincipal principal = currentUser.require();
        UserEntity user = principal.getUser();
        return ResponseEntity.ok(UserResponse.builder()
                        .id(user.getUuid().toString())
                        .githubId(user.getGithubId().toString())
                        .githubUserName(user.getGithubUsername())
                        .displayName(user.getDisplayName())
                        .avatarUrl(user.getAvatarUrl())
                        .build()
                );
    }
}
