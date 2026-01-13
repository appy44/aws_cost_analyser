package com.example.aws_cost_analyser.controller;

import com.example.aws_cost_analyser.service.ServiceCostRawService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageResponse;

import java.time.LocalDate;
import java.util.Map;
@RestController
@RequestMapping("/api/cost/services")
public class ServiceCostRawController {

    private final ServiceCostRawService service;

    public ServiceCostRawController(ServiceCostRawService service) {
        this.service = service;
    }

    @GetMapping(value = "/raw", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getServiceCost(
            @RequestParam String accountId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(
                service.getServiceWiseCostRaw(
                        accountId,
                        startDate.toString(),
                        endDate.toString()
                )
        );
    }
}
