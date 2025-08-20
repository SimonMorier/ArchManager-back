package com.archmanager_back.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.archmanager_back.domain.project.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  boolean existsBySlug(String slug);

  Optional<Project> findBySlug(String slug);

  Optional<Project> findByContainerId(String containerId);

  List<Project> findByActiveSessionCountEqualsAndLastActivityBeforeAndContainerIdIsNotNull(
      int activeSessionCount, Instant cutoff);

  List<Project> findAllByPermissions_User_Username(String username);
}
