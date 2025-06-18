package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.CompanyProfileDTO;
import com.osm.oilproductionservice.enums.TypeCategory;
import com.osm.oilproductionservice.model.BaseType;
import com.osm.oilproductionservice.model.CompanyProfile;
import com.osm.oilproductionservice.repository.GenericRepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.HashSet;


@Service
public class CompanyProfileService extends BaseServiceImpl<CompanyProfile, CompanyProfileDTO, CompanyProfileDTO> {
    private final GenericRepository genericRepository;

    public CompanyProfileService(BaseRepository<CompanyProfile> repository, ModelMapper modelMapper, GenericRepository genericRepository) {
        super(repository, modelMapper);
        this.genericRepository = genericRepository;
    }


    public List<BaseType> getAllTypes(TypeCategory type) {

        return this.genericRepository.findAllByType(type);
    }

    @Override
    public Set<String> actionsMapping(CompanyProfile companyProfile) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }
}
