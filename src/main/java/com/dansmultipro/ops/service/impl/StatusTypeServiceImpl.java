package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.dto.master.StatusTypeResponseDto;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.master.StatusType;
import com.dansmultipro.ops.repository.StatusTypeRepo;
import com.dansmultipro.ops.service.StatusTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class StatusTypeServiceImpl extends BaseService implements StatusTypeService {

    private final StatusTypeRepo repository;

    public StatusTypeServiceImpl(StatusTypeRepo repository) {
        this.repository = repository;
    }

    @Override
    public List<StatusTypeResponseDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public StatusTypeResponseDto findById(String id) {
        UUID uuid = Objects.requireNonNull(getUUID(id), "Invalid UUID: id is null");
        StatusType statusType = repository.findById(uuid).orElseThrow(
                            () -> new ResourceNotFoundException(messageBuilder("Status Type", ResponseConstant.NOT_FOUND)));

        return toDto(statusType);
    }

    private StatusTypeResponseDto toDto(StatusType entity) {
        return new StatusTypeResponseDto(
                entity.getId().toString(),
                entity.getCode(),
                entity.getName(),
                entity.getIsActive(),
                entity.getOptLock());
    }
}
