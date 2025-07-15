package com.archmanager_back.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.archmanager_back.model.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByUserIdAndProjectId(Long userId, Long projectId);

    List<Permission> findByUserId(Long userId);

    List<Permission> findByProjectId(Long projectId);
}
