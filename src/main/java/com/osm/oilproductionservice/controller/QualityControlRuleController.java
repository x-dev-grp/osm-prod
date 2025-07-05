package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.QualityControlRuleDto;
import com.osm.oilproductionservice.model.QualityControlRule;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/qualitycontrolrules")
public class QualityControlRuleController extends BaseControllerImpl<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> {
    public QualityControlRuleController(BaseService<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "QualityControlRule".toUpperCase();
    }

//    private final QualityControlRuleService qualityControlRuleService;
//
//    @Autowired
//    public QualityControlRuleController(QualityControlRuleService qualityControlRuleService) {
//        this.qualityControlRuleService = qualityControlRuleService;
//    }
//
//    /**
//     * Create a new quality control rule.
//     */
//    @PostMapping
//    public ResponseEntity<ApiResponse<QualityControlRuleDto>> createRule(@RequestBody QualityControlRuleDto ruleDto) {
//        try {
//            QualityControlRuleDto createdRule = qualityControlRuleService.createRule(ruleDto);
//            ApiResponse<QualityControlRuleDto> response = new ApiResponse<>(true, "Quality control rule created successfully", createdRule);
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            ApiResponse<QualityControlRuleDto> response = new ApiResponse<>(false, e.getMessage(), null);
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    /**
//     * Retrieve all quality control rules.
//     */
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<QualityControlRuleDto>>> getAllRules() {
//        List<QualityControlRuleDto> rules = qualityControlRuleService.getAllRules();
//        ApiResponse<List<QualityControlRuleDto>> response = new ApiResponse<>(true, "Quality control rules fetched successfully", rules);
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Retrieve a quality control rule by its ID.
//     */
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<QualityControlRuleDto>> getRuleById(@PathVariable Long id) {
//        QualityControlRuleDto ruleDto = qualityControlRuleService.getRuleById(id);
//        if (ruleDto != null) {
//            ApiResponse<QualityControlRuleDto> response = new ApiResponse<>(true, "Rule found", ruleDto);
//            return ResponseEntity.ok(response);
//        } else {
//            ApiResponse<QualityControlRuleDto> response = new ApiResponse<>(false, "Rule not found", null);
//            return ResponseEntity.status(404).body(response);
//        }
//    }
//
//    /**
//     * Update an existing quality control rule.
//     */
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<QualityControlRuleDto>> updateRule(@PathVariable Long id, @RequestBody QualityControlRuleDto ruleDetailsDto) {
//        try {
//            QualityControlRuleDto updatedRule = qualityControlRuleService.updateRule(id, ruleDetailsDto);
//            if (updatedRule != null) {
//                ApiResponse<QualityControlRuleDto> response = new ApiResponse<>(true, "Quality control rule updated successfully", updatedRule);
//                return ResponseEntity.ok(response);
//            } else {
//                ApiResponse<QualityControlRuleDto> response = new ApiResponse<>(false, "Rule not found", null);
//                return ResponseEntity.status(404).body(response);
//            }
//        } catch (RuntimeException e) {
//            ApiResponse<QualityControlRuleDto> response = new ApiResponse<>(false, e.getMessage(), null);
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    /**
//     * Delete a quality control rule by its ID.
//     */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
//        boolean deleted = qualityControlRuleService.deleteRule(id);
//        if (deleted) {
//            ApiResponse<Void> response = new ApiResponse<>(true, "Quality control rule deleted successfully", null);
//            return ResponseEntity.ok(response);
//        } else {
//            ApiResponse<Void> response = new ApiResponse<>(false, "Rule not found", null);
//            return ResponseEntity.status(404).body(response);
//        }
//    }
}