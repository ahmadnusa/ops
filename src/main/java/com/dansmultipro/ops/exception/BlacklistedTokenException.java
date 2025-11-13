package com.dansmultipro.ops.exception;

public class BlacklistedTokenException extends RuntimeException{
    public BlacklistedTokenException(String message) {
        super(message);
    }
}
