package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.Parameter;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface ParameterRepo extends BaseRepository<Parameter> {
    List<Parameter> findByTenantId(UUID tenantId);

    Optional<Parameter> findByTenantIdAndCode(UUID tenantId, String code);
}
