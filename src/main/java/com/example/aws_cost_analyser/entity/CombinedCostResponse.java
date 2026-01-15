package com.example.aws_cost_analyser.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class CombinedCostResponse {

    private Map<String, Object> monthly;
    private Map<String, Object> daily;
    private Map<String, Object> byService;
    private Map<String, Object> byAccount;

    public CombinedCostResponse(
            Map<String, Object> monthly,
            Map<String, Object> daily,
            Map<String, Object> byService,
            Map<String, Object> byAccount) {

        this.monthly = monthly;
        this.daily = daily;
        this.byService = byService;
        this.byAccount = byAccount;
    }
}
