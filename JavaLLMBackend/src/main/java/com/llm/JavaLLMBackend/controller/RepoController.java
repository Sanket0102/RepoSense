package com.llm.JavaLLMBackend.controller;

import com.llm.JavaLLMBackend.dto.response.IndexStatusResponse;
import com.llm.JavaLLMBackend.dto.response.RepoResponse;
import com.llm.JavaLLMBackend.entity.RepositoryEntity;
import com.llm.JavaLLMBackend.indexing.IndexingService;
import com.llm.JavaLLMBackend.security.CurrentUser;
import com.llm.JavaLLMBackend.services.RepoService;
import com.llm.JavaLLMBackend.services.UsersServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repos")
public class RepoController {

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RepoService repoService;

    @Autowired
    private UsersServices userService;

    @Autowired
    private IndexingService indexingService;

    @GetMapping
    public ResponseEntity<List<RepoResponse>> list(
            @RequestParam(name = "refresh",defaultValue = "true")boolean refresh){
        UUID userId = currentUser.require().getId();
        if(refresh){
            return ResponseEntity.ok(repoService.syncAndListRepos(userId));
        }
        return ResponseEntity.ok(repoService.listStored(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepoResponse> get(@PathVariable UUID id){
        UUID userId = currentUser.require().getId();
        RepositoryEntity repositoryEntity = repoService.requiredOwned(id,userId);
        RepoResponse repoResponse = RepoService.toResponse(repositoryEntity);
        return ResponseEntity.ok(repoResponse);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<IndexStatusResponse> status(@PathVariable("id") UUID uuid){
        UUID userId = currentUser.require().getId();
        IndexStatusResponse indexStatusResponse = repoService.status(uuid,userId);
        return ResponseEntity.ok(indexStatusResponse);
    }

    @PostMapping("/{id}/index")
    public ResponseEntity<RepoResponse> index(@PathVariable("id") UUID id){
        UUID userId = currentUser.require().getId();
        RepositoryEntity repository = indexingService.startIndexing(userId,id);
        indexingService.indexAsync(repository,id,userId);
        return ResponseEntity.ok(RepoService.toResponse(repository));
    }

}
