package com.dansmultipro.ops.service;

import com.dansmultipro.ops.model.BaseEntity;
import com.dansmultipro.ops.util.AuthUtil;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseService<T extends BaseEntity> {

    protected void prepareInsert(T entity) {
        Objects.requireNonNull(entity, "Entity must not be null");

        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setIsActive(Boolean.TRUE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        UUID actorId = AuthUtil.idLoginOrSystem();
        entity.setCreatedBy(actorId);
        entity.setUpdatedBy(actorId);
    }

    protected void prepareUpdate(T entity) {
        Objects.requireNonNull(entity, "Entity must not be null");

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(AuthUtil.idLoginOrSystem());
    }

    protected String messageBuilder(String resourceName, String action) {
        Objects.requireNonNull(resourceName, "Resource name must not be null");
        Objects.requireNonNull(action, "Action must not be null");

        return "%s has been %s successfully.".formatted(resourceName, action);
    }

    protected UUID getUUID(String value) {
        Objects.requireNonNull(value, "Value must not be null");
        return UUID.fromString(value);
    }
}
