package com.dansmultipro.ops.util;

import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.pojo.AuthorizationPOJO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public final class AuthUtil {

    public boolean isAuthenticated() {
        return resolvePrincipal().isPresent();
    }

    public UUID getLoginId() {
        AuthorizationPOJO principal = resolvePrincipal()
                .orElseThrow(() -> new IllegalStateException("Authentication is required."));
        return UUID.fromString(principal.id());
    }

    public boolean hasRole(RoleTypeConstant role) {
        Optional<AuthorizationPOJO> principal = resolvePrincipal();
        return principal.map(authorizationPOJO -> authorizationPOJO.role().equals(role)).orElse(false);
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
