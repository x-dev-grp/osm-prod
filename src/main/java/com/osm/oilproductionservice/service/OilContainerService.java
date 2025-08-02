package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilContainerDTO;
import com.osm.oilproductionservice.model.OilContainer;
import com.osm.oilproductionservice.repository.OilContainerRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class OilContainerService extends BaseServiceImpl<OilContainer, OilContainerDTO, OilContainerDTO> {
    private final OilContainerRepository oilContainerRepository;

    public OilContainerService(OilContainerRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.oilContainerRepository = repository;
    }

    @Override
    public Set<Action> actionsMapping(OilContainer OilContainer) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ, Action.MAINTENANCE));
        return actions;
    }
}
