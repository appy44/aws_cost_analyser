package com.example.aws_cost_analyser.controller;

import com.example.aws_cost_analyser.entity.CombinedCostResponse;
import com.example.aws_cost_analyser.entity.CostResponse;
import com.example.aws_cost_analyser.service.CostService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cost")
public class CostController {

    private final CostService costService;

    public CostController(CostService costService) {
        this.costService = costService;
    }

    @GetMapping("/account/monthly")
    public ResponseEntity<CostResponse> getAccountMonthlyCost(
            @RequestParam
            @Pattern(regexp = "\\d{12}", message = "Account ID must be 12 digits")
            String accountId,

            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        CostResponse response =
                costService.getMonthlyCost(
                        accountId,
                        startDate.toString(),
                        endDate.toString()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/combined")
    public CombinedCostResponse getCost(
            @RequestParam String accountId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return costService.getCombinedCost(accountId, startDate, endDate);
    }

}
