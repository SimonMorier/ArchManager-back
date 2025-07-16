package com.archmanager_back.validator;

import com.archmanager_back.config.constant.ErrorLabel;
import com.archmanager_back.exception.InsufficientPermissionException;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.entity.jpa.Permission;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.model.entity.jpa.User;
import com.archmanager_back.repository.jpa.PermissionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Encapsulates permission‐lookup and minimum‐role checks.
 */
@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final PermissionRepository permRepo;

    /**
     * Fetches the Permission for this user/project, or throws 403 if none,
     * then ensures its role ≥ minRole, else throws 403.
     */
    public Permission requirePermission(User user, Project project, RoleEnum minRole) {
        Permission perm = permRepo
                .findByUserIdAndProjectId(user.getId(), project.getId())
                .orElseThrow(() -> new InsufficientPermissionException(
                        ErrorLabel.NO_PERMISSION_ON_PROJECT));

        if (perm.getRole().ordinal() < minRole.ordinal()) {
            throw new InsufficientPermissionException(
                    ErrorLabel.PERMISSION_INSUFFICIENT);
        }
        return perm;
    }
}
