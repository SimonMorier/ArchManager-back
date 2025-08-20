package com.archmanager_back.shared.validator;

import com.archmanager_back.domain.project.Permission;
import com.archmanager_back.domain.project.Project;
import com.archmanager_back.domain.user.RoleEnum;
import com.archmanager_back.domain.user.User;
import com.archmanager_back.infrastructure.config.constant.ErrorLabel;
import com.archmanager_back.infrastructure.persistence.jpa.PermissionRepository;
import com.archmanager_back.infrastructure.persistence.jpa.UserRepository;
import com.archmanager_back.shared.exception.custom.InsufficientPermissionException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final PermissionRepository permRepo;
    private final UserRepository userRepo;

    public void requirePermission(UserDetails userDetails, Project project, RoleEnum minRole) {

        User user = userRepo.findByUsernameWithPermissions(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown user: " + userDetails.getUsername()));

        Permission perm = permRepo
                .findByUserIdAndProjectId(user.getId(), project.getId())
                .orElseThrow(() -> new InsufficientPermissionException(
                        ErrorLabel.NO_PERMISSION_ON_PROJECT));

        if (perm.getRole().ordinal() < minRole.ordinal()) {
            throw new InsufficientPermissionException(ErrorLabel.PERMISSION_INSUFFICIENT);
        }
    }

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