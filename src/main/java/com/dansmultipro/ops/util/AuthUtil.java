package com.dansmultipro.ops.util;

import java.util.UUID;

public final class AuthUtil {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private AuthUtil() {
        // utility class
    }

    public static UUID idLoginOrSystem() {
        return SYSTEM_USER_ID;
    }
}
