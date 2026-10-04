package com.llm.JavaLLMBackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CommonUtils {

    @Value("${app.github.base.url}")
    public String BASE_URL;

    public RestClient getRestClient(String accessToken){
        RestClient restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.ACCEPT,"application/vnd.github+json")
                .defaultHeader("X-Github-Api-Version","2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT,"DevPilot")
                .build();
        return restClient;
    }


}
