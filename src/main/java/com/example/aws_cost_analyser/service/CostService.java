package com.example.aws_cost_analyser.service;

import com.example.aws_cost_analyser.entity.CostResponse;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;

@Service
public class CostService {

    private final CostExplorerClient costExplorerClient;

    public CostService(CostExplorerClient costExplorerClient) {
        this.costExplorerClient = costExplorerClient;
    }

    public CostResponse getMonthlyCost(String accountId, String start, String end) {

        GetCostAndUsageRequest request =
                GetCostAndUsageRequest.builder()
                        .timePeriod(DateInterval.builder()
                                .start(start)
                                .end(end)
                                .build())
                        .granularity(Granularity.MONTHLY)
                        .metrics("UnblendedCost")
                        .filter(Expression.builder()
                                .dimensions(DimensionValues.builder()
                                        .key("LINKED_ACCOUNT")
                                        .values(accountId)
                                        .build())
                                .build())
                        .build();

        GetCostAndUsageResponse response =
                costExplorerClient.getCostAndUsage(request);

        ResultByTime result = response.resultsByTime().get(0);
        MetricValue cost = result.total().get("UnblendedCost");

        CostResponse dto = new CostResponse();
        dto.setAccountId(accountId);
        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setBigDecimalAmount(cost.amount());
        dto.setCurrency(cost.unit());

        return dto;
    }
}
