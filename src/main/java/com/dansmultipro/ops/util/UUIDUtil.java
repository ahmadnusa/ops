package com.dansmultipro.ops.util;

import java.util.UUID;
import java.util.function.Function;

public class UUIDUtil {
    public static Function<String, UUID> getUUID = value -> {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value);
        }
    };
}
