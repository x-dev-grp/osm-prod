package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.MachinePlan;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MachinePlanRepository extends BaseRepository<MachinePlan> {
    // Add custom queries if needed.
}
