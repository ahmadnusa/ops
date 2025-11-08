package com.dansmultipro.ops.service;

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

    protected <E extends BaseEntity> E prepareCreate(E entity) {
        LocalDateTime now = LocalDateTime.now();
        UUID actorId = AuthUtil.getLoginId();

        entity.setId(UUID.randomUUID());
        entity.setIsActive(Boolean.TRUE);
        entity.setCreatedAt(now);
        entity.setCreatedBy(actorId);

        return entity;
    }

    protected <E extends BaseEntity> E prepareUpdate(E entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(AuthUtil.getLoginId());

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
