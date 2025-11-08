package com.dansmultipro.ops.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public final class AuthUtil {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static UUID getLoginId() {
        return SYSTEM_USER_ID;
    }

    // public UUID getLoginId(){
    // var auth = SecurityContextHolder.getContext().getAuthentication();
    // if (auth != null) {
    // var principal = (AuthorizationPOJO) auth.getPrincipal();
    // return UUID.fromString(principal.getId());
    // }
    // throw new IllegalStateException("Authentication object is null");
    // }
}
