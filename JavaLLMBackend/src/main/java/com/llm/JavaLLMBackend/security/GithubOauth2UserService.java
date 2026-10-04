package com.llm.JavaLLMBackend.security;

import com.llm.JavaLLMBackend.entity.UserEntity;
import com.llm.JavaLLMBackend.services.UsersServices;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GithubOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsersServices usersServices;
    private final DefaultOAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();


    @Override
    public @Nullable OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User gitUser = oAuth2UserService.loadUser(userRequest);

        String accessToken = userRequest.getAccessToken().getTokenValue();
        String scopes = userRequest.getAccessToken().getScopes() != null ?
                String.join(",",userRequest.getAccessToken().getScopes())
                : "read:user,repo";

        UserEntity user = usersServices.upsertFromGithub(gitUser.getAttributes(),accessToken,scopes);
        return new AppUserPrincipal(user,gitUser.getAttributes());
    }
}
