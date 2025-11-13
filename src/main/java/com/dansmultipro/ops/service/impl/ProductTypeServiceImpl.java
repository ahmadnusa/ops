package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.dto.master.ProductTypeResponseDto;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.master.ProductType;
import com.dansmultipro.ops.repository.ProductTypeRepo;
import com.dansmultipro.ops.service.ProductTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductTypeServiceImpl extends BaseService implements ProductTypeService {

    private final ProductTypeRepo repository;

    public ProductTypeServiceImpl(ProductTypeRepo repository) {
        this.repository = repository;
    }

    @Override
    public List<ProductTypeResponseDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ProductTypeResponseDto findById(String id) {
        UUID uuid = Objects.requireNonNull(getUUID(id), "Invalid UUID: id is null");
        ProductType productType = repository.findById(uuid).orElseThrow(
                () -> new ResourceNotFoundException(messageBuilder("Product Type", ResponseConstant.NOT_FOUND)));

        return toDto(productType);
    }

    private ProductTypeResponseDto toDto(ProductType entity) {
        return new ProductTypeResponseDto(
                entity.getId().toString(),
                entity.getCode(),
                entity.getName(),
                entity.getIsActive(),
                entity.getOptLock());
    }
}
