package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.PlanningSaveRequest;
import com.osm.oilproductionservice.service.PlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.xdev.xdevbase.utils.OSMLogger;

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
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "savePlanning", request);
        try {
            log.info("Saving planning");
            planningService.savePlanning(request);
            return ResponseEntity.ok("Planning saved successfully");
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "savePlanning", e);
            log.error("Error saving planning: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to save planning: " + e.getMessage());
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "savePlanning", null);
            OSMLogger.logPerformance(this.getClass(), "savePlanning", startTime, System.currentTimeMillis());
        }
    }

    /* ───── NEW: mark LOT completed ───── */
    @PostMapping("/planning/lots/{lotNumber}/completed")
    public void completeLot(@PathVariable String lotNumber, @RequestBody Map<String, Object> body) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "completeLot", new Object[]{lotNumber, body});
        try {
            Double oilQuantity = body.get("oilQuantity") != null ? ((Number) body.get("oilQuantity")).doubleValue() : null;
            Double rendement = body.get("rendement") != null ? ((Number) body.get("rendement")).doubleValue() : null;
            planningService.markLotCompleted(lotNumber, oilQuantity, rendement);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "completeLot", e);
            log.error("Error completing lot: {}", e.getMessage());
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "completeLot", null);
            OSMLogger.logPerformance(this.getClass(), "completeLot", startTime, System.currentTimeMillis());
        }
    }

    /* ───── NEW: mark GLOBAL-LOT completed ───── */
    @PostMapping("/planning/globalLots/{globalLotNumber}/completed")
    public void completeGlobalLot(@PathVariable String globalLotNumber, @RequestBody Map<String, Object> body) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "completeGlobalLot", new Object[]{globalLotNumber, body});
        try {
            Double oilQuantity = body.get("oilQuantity") != null ? ((Number) body.get("oilQuantity")).doubleValue() : null;
            Double rendement = body.get("rendement") != null ? ((Number) body.get("rendement")).doubleValue() : null;
            planningService.markGlobalLotCompleted(globalLotNumber, oilQuantity, rendement);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "completeGlobalLot", e);
            log.error("Error completing global lot: {}", e.getMessage());
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "completeGlobalLot", null);
            OSMLogger.logPerformance(this.getClass(), "completeGlobalLot", startTime, System.currentTimeMillis());
        }
    }
}