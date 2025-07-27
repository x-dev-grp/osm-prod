package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.ParameterDto;
import com.osm.oilproductionservice.model.Parameter;
import com.osm.oilproductionservice.repository.ParameterRepo;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ParameterService extends BaseServiceImpl<Parameter, ParameterDto, ParameterDto> {

    private final ParameterRepo parameterRepo;

    protected ParameterService(BaseRepository<Parameter> repository, ModelMapper modelMapper, ParameterRepo parameterRepo) {
        super(repository, modelMapper);
        this.parameterRepo = parameterRepo;
    }
    public Parameter getByCode(String code, UUID tenantId) {
        Parameter param = parameterRepo.findByTenantIdAndCode(tenantId,code)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found: " + code));
        return modelMapper.map(param,Parameter.class);
    }
    @Override
    public Set<Action> actionsMapping(Parameter millMachine) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
