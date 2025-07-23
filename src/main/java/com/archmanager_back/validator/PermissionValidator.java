package com.archmanager_back.validator;

import com.archmanager_back.config.constant.ErrorLabel;
import com.archmanager_back.exception.custom.InsufficientPermissionException;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.entity.jpa.Permission;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.model.entity.jpa.User;
import com.archmanager_back.repository.jpa.PermissionRepository;
import com.archmanager_back.repository.jpa.UserRepository;
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