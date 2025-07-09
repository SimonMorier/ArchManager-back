package com.archmanager_back.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.archmanager_back.model.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Retrouvez la permission d'un utilisateur sur un projet donné.
     */
    Optional<Permission> findByUserIdAndProjectId(Long userId, Long projectId);

    /**
     * Toutes les permissions pour un utilisateur.
     */
    List<Permission> findByUserId(Long userId);

    /**
     * Toutes les permissions pour un projet.
     */
    List<Permission> findByProjectId(Long projectId);
}
