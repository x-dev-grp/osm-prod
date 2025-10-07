package com.osm.oilproductionservice.repository;

import com.xdev.communicator.models.enums.TypeCategory;
import com.osm.oilproductionservice.model.BaseType;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenericRepository extends BaseRepository<BaseType> {
    List<BaseType> findAllByType(TypeCategory type);
}
