package com.dansmultipro.ops.service;

import com.dansmultipro.ops.dto.master.RoleResponseDto;

import java.util.List;

public interface RoleService {

    List<RoleResponseDto> findAll();

    RoleResponseDto findById(String id);
}
