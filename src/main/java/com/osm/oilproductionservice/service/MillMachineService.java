package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.MillMachineDto;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.repository.MillMachineRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
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
    public Set<String> actionsMapping(MillMachine millMachine) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }
}
