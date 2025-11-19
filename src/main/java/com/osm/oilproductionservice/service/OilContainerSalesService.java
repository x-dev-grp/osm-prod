package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilContainerDTO;
import com.osm.oilproductionservice.dto.OilContainerSaleDto;
import com.osm.oilproductionservice.model.OilContainer;
import com.osm.oilproductionservice.model.OilContainerSale;
import com.osm.oilproductionservice.repository.OilContainerRepository;
import com.osm.oilproductionservice.repository.OilContainerSaleRepo;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OilContainerSalesService extends BaseServiceImpl<OilContainerSale, OilContainerSaleDto, OilContainerSaleDto> {

    public OilContainerSalesService(OilContainerSaleRepo repository, ModelMapper modelMapper ) {
        super(repository, modelMapper);
     }
    @Override
    public Set<Action> actionsMapping(OilContainerSale oilContainerSale) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }

}
