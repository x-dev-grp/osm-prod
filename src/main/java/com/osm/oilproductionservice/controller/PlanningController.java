package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.PlanningSaveRequest;
import com.osm.oilproductionservice.service.PlanningService;
import com.xdev.communicator.models.shared.ChildLotCompletionDto;
import com.xdev.xdevbase.utils.OSMLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.math3.util.Precision.round;

@RestController
@RequestMapping("/api/production")
public class PlanningController {

    private static final Logger log = LoggerFactory.getLogger(PlanningController.class);
    public static final String OIL_QUANTITY = "oilQuantity";
    public static final String RENDEMENT = "rendement";
    public static final String UNPAID_PRICE = "unpaidPrice";
    public static final String AUTO_SET_STORAGE = "autoSetStorage";
    public static final String TRT_DURATION = "triturationDurationInMinutes";

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

    /* ───── MARK LOT completed ───── */
    @PostMapping("/planning/lots/{lotNumber}/completed")
    public ResponseEntity<String> completeLot(@PathVariable String lotNumber, @RequestBody Map<String, Object> body) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "completeLot", new Object[]{lotNumber, body});

        try {
            if (body == null) {
                return ResponseEntity
                        .badRequest()
                        .body("Request body cannot be null");
            }

            Double oilQuantity = getDouble(body, OIL_QUANTITY);
            Double rendement   = getDouble(body, RENDEMENT);
            Double unpaidPrice = getDouble(body, UNPAID_PRICE);
            int duree = getInt(body, TRT_DURATION);

            if (oilQuantity == null && rendement == null && unpaidPrice == null) {
                return ResponseEntity
                        .badRequest()
                        .body("At least one of oilQuantity, rendement or unpaidPrice must be provided");
            }
            boolean autoSetStorage=false;
            if(body.get(AUTO_SET_STORAGE)!=null && body.get(AUTO_SET_STORAGE) instanceof Boolean b) {
               autoSetStorage= b;
            }
            planningService.markLotCompleted(lotNumber, "0" , oilQuantity, rendement, unpaidPrice,autoSetStorage,duree);
            return ResponseEntity
                    .ok("Lot completed successfully");

        } catch (EntityNotFoundException e) {
            log.error("Lot not found: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Lot not found: " + e.getMessage());

        } catch (Exception e) {
            log.error("Error completing lot: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to complete lot: " + e.getMessage());

        } finally {
            OSMLogger.logMethodExit(this.getClass(), "completeLot", null);
            OSMLogger.logPerformance(
                    this.getClass(), "completeLot", startTime, System.currentTimeMillis());
        }
    }

    private int getInt(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return (v instanceof Number) ? ((Number) v).intValue() : 0;
    }

    /* ───── MARK GLOBAL-LOT completed ───── */
    @PostMapping("/planning/globalLots/{globalLotNumber}/completed")
    public ResponseEntity<String> completeGlobalLot(
            @PathVariable String globalLotNumber,
            @RequestBody Map<String, Object> body) {
        long startTime = System.currentTimeMillis();


        List<ChildLotCompletionDto> childLots = (List<ChildLotCompletionDto>) body.get("childLots");
        OSMLogger.logMethodEntry(this.getClass(), "completeGlobalLot", globalLotNumber, childLots);   try {
            if (childLots == null || childLots.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("Child lot details must be provided");
            }

            planningService.markGlobalLotCompleted(globalLotNumber, body);
            return ResponseEntity
                    .ok("Global lot completed successfully");

        } catch (EntityNotFoundException e) {
            log.error("Global lot not found: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Global lot not found: " + e.getMessage());

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "completeGlobalLot", e);
            log.error("Error completing global lot {}: {}", globalLotNumber, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to complete global lot: " + e.getMessage());

        } finally {
            OSMLogger.logMethodExit(this.getClass(), "completeGlobalLot", null);
            OSMLogger.logPerformance(
                    this.getClass(), "completeGlobalLot", startTime, System.currentTimeMillis());
        }
    }
    /** helper to safely extract a Double from the request body */
    private Double getDouble(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return (v instanceof Number) ? round(((Number) v).doubleValue(),3) : null;
    }
    protected String getResourceName() {
        return "Parameter".toUpperCase();
    }
}