package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.model.OilContainer;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OilContainerRepository extends BaseRepository<OilContainer> {
}
