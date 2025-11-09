package com.dansmultipro.ops.util;

import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.model.user.User;
import com.dansmultipro.ops.pojo.AuthorizationPOJO;
import com.dansmultipro.ops.repository.UserRepo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public final class AuthUtil {

    private final UserRepo userRepo;

    public AuthUtil(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean isAuthenticated() {
        return resolvePrincipal().isPresent();
    }

    public UUID getSystemId() {
        return userRepo.findFirstByRoleCode(RoleTypeConstant.SYSTEM.name())
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("System user is not configured."));
    }

    public UUID getLoginId() {
        AuthorizationPOJO principal = resolvePrincipal()
                .orElseThrow(() -> new IllegalStateException("Authentication is required."));
        return UUID.fromString(principal.getId());
    }

    public RoleTypeConstant roleLogin() {
        AuthorizationPOJO principal = resolvePrincipal()
                .orElseThrow(() -> new IllegalStateException("Authentication is required."));
        return principal.getRole();
    }

    public boolean hasRole(RoleTypeConstant role) {
        Optional<AuthorizationPOJO> principal = resolvePrincipal();
        if (principal.isEmpty()) {
            return false;
        }
        return principal.get().getRole().equals(role);
    }

    private Optional<AuthorizationPOJO> resolvePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof AuthorizationPOJO authorizationPOJO) {
                return Optional.of(authorizationPOJO);
            }
        }
        return Optional.empty();
    }
}
