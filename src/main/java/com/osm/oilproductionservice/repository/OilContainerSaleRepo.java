package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.OilContainerSale;
import com.osm.oilproductionservice.model.OilSale;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OilContainerSaleRepo extends BaseRepository<OilContainerSale> {

    List<OilContainerSale> findByOilSaleId(UUID oilSaleId);}