package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.TransporterDTO;
import com.osm.oilproductionservice.model.Transporter;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
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
    public Set<String> actionsMapping(Transporter transporter) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }
}
