package com.dansmultipro.ops.spec;

import com.dansmultipro.ops.model.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {
    public static Specification<User> hasActiveStatus(Boolean isActive) {
        return (root, query, builder) -> (isActive == null) ? null : builder.equal(root.get("isActive"), isActive);
    }

    public static Specification<User> hasRole(String role) {
            return (root, query, builder) -> (role == null) ? null :
                    builder.equal(root.get("role").get("code"), role);
        }
}
