package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.CustomerDto;
import com.osm.oilproductionservice.model.Customer;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomerService extends BaseServiceImpl<Customer, CustomerDto, CustomerDto> {

    public CustomerService(BaseRepository<Customer> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }
    @Override
    public Set<Action> actionsMapping(Customer Customer) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
} 