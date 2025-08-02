package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.WasteDTO;
import com.osm.oilproductionservice.model.Waste;
import com.osm.oilproductionservice.repository.WasteRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class WasteService extends BaseServiceImpl<Waste, WasteDTO, WasteDTO> {

    public WasteService(WasteRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(Waste millMachine) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ, Action.MAINTENANCE));
        return actions;
    }
}
