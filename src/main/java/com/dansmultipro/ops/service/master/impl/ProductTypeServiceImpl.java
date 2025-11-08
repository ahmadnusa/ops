package com.dansmultipro.ops.service.master.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.dto.master.ProductTypeResponseDto;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.master.ProductType;
import com.dansmultipro.ops.repository.ProductTypeRepository;
import com.dansmultipro.ops.service.BaseService;
import com.dansmultipro.ops.service.master.ProductTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductTypeServiceImpl extends BaseService implements ProductTypeService {

    private final ProductTypeRepository repository;

    public ProductTypeServiceImpl(ProductTypeRepository repository) {
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

    @Override
    public ProductTypeResponseDto findByCode(String code) {
        ProductType productType = repository.findByCode(code).orElseThrow(
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
