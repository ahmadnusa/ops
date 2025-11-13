package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.BaseEntity;
import com.dansmultipro.ops.model.User;
import com.dansmultipro.ops.repository.UserRepo;
import com.dansmultipro.ops.util.AuthUtil;
import com.dansmultipro.ops.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseService {

    protected AuthUtil authUtil;
    protected UserRepo userRepo;

    @Autowired
    public void setAuthUtil(AuthUtil authUtil) {
        this.authUtil = authUtil;
    }

    @Autowired
    public void setUserRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    protected <E extends BaseEntity> E prepareCreate(E entity) {
        return prepareCreate(entity, Boolean.TRUE);
    }

    protected <E extends BaseEntity> E prepareCreate(E entity, Boolean isActive) {
        LocalDateTime now = LocalDateTime.now();
        UUID actorId = resolveActorId();

        entity.setId(UUID.randomUUID());
        entity.setIsActive(isActive);
        entity.setCreatedAt(now);
        entity.setCreatedBy(actorId);

        return entity;
    }

    protected <E extends BaseEntity> E prepareUpdate(E entity) {
        UUID actorId = resolveActorId();

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(actorId);

        return entity;
    }

    private UUID resolveActorId() {
        return authUtil.isAuthenticated()
                ? authUtil.getLoginId()
                : fetchSystemUserId();
    }

    private UUID fetchSystemUserId() {
        return userRepo.findFirstByRoleCode(RoleTypeConstant.SYSTEM.name())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("System user is not configured."));
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
