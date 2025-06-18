package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.MachinePlanDto;
import com.osm.oilproductionservice.model.MachinePlan;
import com.osm.oilproductionservice.repository.MachinePlanRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MachinePlanService extends BaseServiceImpl<MachinePlan, MachinePlanDto, MachinePlanDto> {

    private final MachinePlanRepository machinePlanRepository;

    public MachinePlanService(MachinePlanRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.machinePlanRepository = repository;
    }

    // Add any machine-plan–specific business logic here if needed.

    @Override
    public Set<String> actionsMapping(MachinePlan machinePlan) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }
}
