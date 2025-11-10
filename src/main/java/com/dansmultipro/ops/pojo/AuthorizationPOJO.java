package com.dansmultipro.ops.pojo;

import com.dansmultipro.ops.constant.RoleTypeConstant;

public class AuthorizationPOJO {

    private final String id;
    private final RoleTypeConstant role;

    public AuthorizationPOJO(String id, RoleTypeConstant role) {
        this.id = id;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public RoleTypeConstant getRole() {
        return role;
    }
}
