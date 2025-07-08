package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.TransporterDTO;
import com.osm.oilproductionservice.model.Transporter;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TransporterService extends BaseServiceImpl<Transporter, TransporterDTO, TransporterDTO> {


    public TransporterService(BaseRepository<Transporter> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);

    }

    @Override
    public Set<Action> actionsMapping(Transporter transporter) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
