package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.PlanningSaveRequest;
import com.osm.oilproductionservice.service.PlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production")
public class PlanningController {

    private static final Logger log = LoggerFactory.getLogger(PlanningController.class);

    private final PlanningService planningService;

    @Autowired
    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @GetMapping("/planning")
    public ResponseEntity<PlanningSaveRequest> getPlanning() {
        log.info("Fetching planning");
        return ResponseEntity.ok(planningService.getPlanning());
    }

    @PostMapping("/planning")
    public ResponseEntity<String> savePlanning(@RequestBody PlanningSaveRequest request) {
        log.info("Saving planning");
        try {
            planningService.savePlanning(request);
            return ResponseEntity.ok("Planning saved successfully");
        } catch (Exception e) {
            log.error("Error saving planning: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to save planning: " + e.getMessage());
        }
    }
    /* ───── NEW: mark LOT completed ───── */
    @PostMapping("/planning/lots/{lotNumber}/completed")
    public ResponseEntity<Void> completeLot(@PathVariable String lotNumber) {
        planningService.markLotCompleted(lotNumber);
        return ResponseEntity.noContent().build();
    }

    /* ───── NEW: mark GLOBAL-LOT completed ───── */
    @PostMapping("/planning/globalLots/{globalLotNumber}/completed")
    public ResponseEntity<Void> completeGlobalLot(@PathVariable String globalLotNumber) {
        planningService.markGlobalLotCompleted(globalLotNumber);
        return ResponseEntity.noContent().build();
    }
}