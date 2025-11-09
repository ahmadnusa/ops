package com.dansmultipro.ops.pojo;

import com.dansmultipro.ops.constant.RoleTypeConstant;

public class AuthorizationPOJO {

    private final String id;
    private final String email;
    private final RoleTypeConstant role;

    public AuthorizationPOJO(String id, String email, RoleTypeConstant role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public RoleTypeConstant getRole() {
        return role;
    }
}
