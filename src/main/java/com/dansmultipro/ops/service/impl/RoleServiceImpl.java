package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.dto.master.RoleResponseDto;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.master.Role;
import com.dansmultipro.ops.repository.RoleRepo;
import com.dansmultipro.ops.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class RoleServiceImpl extends BaseService implements RoleService {

    private final RoleRepo repository;

    public RoleServiceImpl(RoleRepo repository) {
        this.repository = repository;
    }

    @Override
    public List<RoleResponseDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public RoleResponseDto findById(String id) {
        UUID uuid = Objects.requireNonNull(getUUID(id), "Invalid UUID: id is null");
        Role role = repository.findById(uuid).orElseThrow(
                () -> new ResourceNotFoundException(messageBuilder("Role", ResponseConstant.NOT_FOUND)));

        return toDto(role);
    }

    private RoleResponseDto toDto(Role entity) {
        return new RoleResponseDto(
                entity.getId().toString(),
                entity.getCode(),
                entity.getName(),
                entity.getIsActive(),
                entity.getOptLock());
    }
}
