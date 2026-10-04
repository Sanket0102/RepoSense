package com.llm.JavaLLMBackend.repositories;

import com.llm.JavaLLMBackend.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepoRepository extends JpaRepository<RepositoryEntity, UUID> {

    List<RepositoryEntity> findByUserIdOrderByFullNameAsc(UUID userId);

    Optional<RepositoryEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<RepositoryEntity> findByUserIdAndRepoId(UUID userid, Long repoId);

    boolean existsByRepoId(@Param("repoId") Long repoId);

}
