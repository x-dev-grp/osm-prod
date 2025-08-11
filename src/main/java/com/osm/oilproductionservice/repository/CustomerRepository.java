package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.Customer;
import com.osm.oilproductionservice.model.MachinePlan;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends BaseRepository<Customer> {
    // Add custom queries if needed.
}
