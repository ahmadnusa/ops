package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.model.BaseEntity;
import com.dansmultipro.ops.util.AuthUtil;
import com.dansmultipro.ops.util.UUIDUtil;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseService {

    protected AuthUtil authUtil;

    @Autowired
    public void setAuthUtil(AuthUtil authUtil) {
        this.authUtil = authUtil;
    }

    protected <E extends BaseEntity> E prepareInsert(E entity) {
        return prepareInsert(entity, Boolean.TRUE);
    }

    protected <E extends BaseEntity> E prepareInsert(E entity, Boolean isActive) {
        LocalDateTime now = LocalDateTime.now();
        UUID actorId = authUtil.isAuthenticated() ? authUtil.getLoginId() : authUtil.getSystemId();

        entity.setId(UUID.randomUUID());
        entity.setIsActive(isActive);
        entity.setCreatedAt(now);
        entity.setCreatedBy(actorId);

        return entity;
    }

    protected <E extends BaseEntity> E prepareUpdate(E entity) {
        UUID actorId = authUtil.isAuthenticated() ? authUtil.getLoginId() : authUtil.getSystemId();

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(actorId);

        return entity;
    }

    protected String messageBuilder(String resourceName, String action) {
        return "%s has been %s successfully.".formatted(resourceName, action);
    }

    protected String messageBuilder(String resourceName, ResponseConstant message) {
        return "%s %s".formatted(resourceName, message.getValue());
    }

    protected UUID getUUID(String uuidStr) {
        return UUIDUtil.getUUID.apply(uuidStr);
    }
}
