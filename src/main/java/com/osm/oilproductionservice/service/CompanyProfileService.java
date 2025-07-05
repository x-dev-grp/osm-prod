package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.CompanyProfileDTO;
import com.osm.oilproductionservice.enums.TypeCategory;
import com.osm.oilproductionservice.model.BaseType;
import com.osm.oilproductionservice.model.CompanyProfile;
import com.osm.oilproductionservice.repository.GenericRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class CompanyProfileService extends BaseServiceImpl<CompanyProfile, CompanyProfileDTO, CompanyProfileDTO> {
    private final GenericRepository genericRepository;

    public CompanyProfileService(BaseRepository<CompanyProfile> repository, ModelMapper modelMapper, GenericRepository genericRepository) {
        super(repository, modelMapper);
        this.genericRepository = genericRepository;
    }


    public List<BaseType> getAllTypes(TypeCategory type) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getAllTypes", type);

        try {
            List<BaseType> types = this.genericRepository.findAllByType(type);

            OSMLogger.logDataAccess(this.getClass(), "GET_ALL_TYPES", "BaseType");
            OSMLogger.logBusinessEvent(this.getClass(), "COMPANY_TYPES_RETRIEVED",
                    "Retrieved " + types.size() + " types for category: " + type.name());
            OSMLogger.logMethodExit(this.getClass(), "getAllTypes", "Found " + types.size() + " types");
            OSMLogger.logPerformance(this.getClass(), "getAllTypes", startTime, System.currentTimeMillis());

            return types;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting all types for category: " + type.name(), e);
            throw e;
        }
    }

    @Override
    public Set<Action> actionsMapping(CompanyProfile companyProfile) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", companyProfile);

        try {
            Set<Action> actions = new HashSet<>();
            actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", "Actions: " + actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());

            return actions;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error mapping actions for CompanyProfile: " + companyProfile.getId(), e);
            throw e;
        }
    }
}
