package com.dansmultipro.ops.repository;

import com.dansmultipro.ops.model.master.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatusTypeRepository extends JpaRepository<StatusType, UUID> {

    Optional<StatusType> findByCode(String code);
}
