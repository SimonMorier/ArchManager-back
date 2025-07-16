package com.archmanager_back.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.archmanager_back.model.entity.jpa.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  boolean existsBySlug(String slug);

  Optional<Project> findBySlug(String slug);

  Optional<Project> findByContainerId(String containerId);
}