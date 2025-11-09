package com.dansmultipro.ops.constant;

public enum RoleTypeConstant {
    SA("Super Admin"),
    CUSTOMER("Customer"),
    GATEWAY("Gateway"),
    SYSTEM("System");

    private final String name;

    RoleTypeConstant(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
