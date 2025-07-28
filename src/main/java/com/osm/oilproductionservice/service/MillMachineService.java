package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.MillMachineDto;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.repository.MillMachineRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MillMachineService extends BaseServiceImpl<MillMachine, MillMachineDto, MillMachineDto> {
    private final MillMachineRepository millMachineRepository;

    public MillMachineService(MillMachineRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.millMachineRepository = repository;
    }

    @Override
    public Set<Action> actionsMapping(MillMachine millMachine) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ, Action.MAINTENANCE));
        return actions;
    }
}
