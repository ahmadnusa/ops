package com.dansmultipro.ops.constant;

public enum ResponseConstant {
    SAVED("Saved"),
    UPDATED("Updated"),
    NOT_FOUND("Not Found"),
    ALREADY_EXISTS("Already Exists"),
    INVALID_VALUE("is invalid."),
    INVALID_CREDENTIAL("Email or password is incorrect."),
    ACCOUNT_INACTIVE("is not active yet. Please wait for admin approval."),
    OLD_PASSWORD_INVALID("Old password is incorrect."),
    SUPER_ADMIN_REQUIRED("requires Super Admin privileges."),
    CUSTOMER_REQUIRED("requires Customer privileges."),
    GATEWAY_REQUIRED("requires Gateway privileges."),
    NOT_OWNER("is not owned by you."),
    NOT_PROCESSING("must be in PROCESSING status.");

    private final String value;

    ResponseConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
