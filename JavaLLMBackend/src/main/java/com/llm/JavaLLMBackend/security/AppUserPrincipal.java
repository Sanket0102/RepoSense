package com.llm.JavaLLMBackend.security;

import com.llm.JavaLLMBackend.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class AppUserPrincipal implements OAuth2User {

    private final UserEntity userEntity;
    private final Map<String,Object> attributes;

    public UUID getId(){
        return userEntity.getUuid();
    }

    public UserEntity getUser(){
        return userEntity;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }

    @Override
    public String getName() {
        return userEntity.getGithubId().toString();
    }
}
