package com.llm.JavaLLMBackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String githubId;
    private String githubUserName;
    private String displayName;
    private String avatarUrl;
}
