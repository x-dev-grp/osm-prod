package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.model.OilContainer;
import com.osm.oilproductionservice.model.OilContainerSale;
import com.xdev.xdevbase.repos.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OilContainerRepository extends BaseRepository<OilContainer> {
   }
