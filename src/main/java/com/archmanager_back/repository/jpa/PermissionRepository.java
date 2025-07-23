package com.archmanager_back.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.entity.jpa.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

        Optional<Permission> findByUserIdAndProjectId(Long userId, Long projectId);

        List<Permission> findByUserId(Long userId);

        List<Permission> findByProjectId(Long projectId);

        @Query(value = """
                        SELECT EXISTS (
                          SELECT 1
                          FROM permissions p
                          JOIN project pr  ON p.project_id = pr.id
                          JOIN users u     ON p.user_id    = u.id
                          WHERE pr.slug    = :projectSlug
                            AND u.username = :username
                            AND p.role     = :role
                        )
                        """, nativeQuery = true)
        boolean existsByProjectSlugAndUsernameAndRole(String projectSlug,
                        String username,
                        RoleEnum role);

        @Query(value = """
                        SELECT *
                        FROM permissions p
                        JOIN project pr  ON p.project_id = pr.id
                        JOIN users u     ON p.user_id    = u.id
                        WHERE pr.slug    = :projectSlug
                          AND u.username = :username
                          AND p.role     = :role
                        """, nativeQuery = true)
        Optional<Permission> findByProjectSlugAndUsernameAndRole(String projectSlug,
                        String username,
                        RoleEnum role);

        boolean existsByProject_SlugAndUser_UsernameAndRole(
                        String projectSlug,
                        String username,
                        RoleEnum role);
}
