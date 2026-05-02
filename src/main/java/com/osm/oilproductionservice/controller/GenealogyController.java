package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.GenealogyDto;
import com.osm.oilproductionservice.service.GenealogyService;
import com.xdev.communicator.models.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/production/traceability")
public class GenealogyController {

    private final GenealogyService genealogyService;

    public GenealogyController(GenealogyService genealogyService) {
        this.genealogyService = genealogyService;
    }

    @GetMapping("/genealogy/{storageUnitId}")
    public ResponseEntity<ApiResponse<GenealogyDto>> getGenealogy(@PathVariable UUID storageUnitId) {
        GenealogyDto data = genealogyService.getFullGenealogy(storageUnitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Genealogy fetched successfully", data));
    }
}
