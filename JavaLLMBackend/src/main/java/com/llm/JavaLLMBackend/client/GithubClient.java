package com.llm.JavaLLMBackend.client;

import com.llm.JavaLLMBackend.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GithubClient {

    @Autowired
    private CommonUtils commonUtils;

    private List<Map<String,Object>> listMap = new ArrayList<>();
    private Map<String,Object> map = new HashMap<>();

    public List<Map<String,Object>> fetchUserRepos(String accessToken){
        List<Map<String,Object>> all = new ArrayList<>();
        int page = 1;
        while(page <= 10){
            final int currentPage = page;
            RestClient restClient = commonUtils.getRestClient(accessToken);
            List<Map<String,Object>> pageRepos = restClient.get()
                    .uri(uriBuilder -> {
                         return uriBuilder.path("/user/repos")
                                .queryParam("affiliation","owner,collaborator,organization_member")
                                .queryParam("sort","updated")
                                .queryParam("per_page",100)
                                .queryParam("page",currentPage)
                                .build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String,Object>>>() {});

            if (pageRepos == null || pageRepos.isEmpty()){
                break;
            }
            all.addAll(pageRepos);
            if (pageRepos.size() < 100){
                break;
            }
            page++;
        }
        return all;
    }

    public String fetchFileContent(String accessToken, String owner, String repo, String path){
        Map<String,Object> files = commonUtils.getRestClient(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/contents/{path}",owner,repo,path)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if(files == null){
            return null;
        }
        Object encoding = files.get("encoding");
        Object content = files.get("content");
        if(content == null){
            return null;
        }
        if ("base64".equalsIgnoreCase(String.valueOf(encoding))){
            String raw = String.valueOf(content).replaceAll("\\s","");
            return new String(Base64.getDecoder().decode(raw), StandardCharsets.UTF_8);
        }
        return String.valueOf(content);
    }

    public Map<String,Object> fetchRepoTree(String accessToken,String owner,String repo,String branch){
        return commonUtils.getRestClient(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1",owner,repo,branch)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String,Object>>() {});
    }


}
