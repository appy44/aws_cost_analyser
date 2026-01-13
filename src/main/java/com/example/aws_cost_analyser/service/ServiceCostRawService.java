package com.example.aws_cost_analyser.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServiceCostRawService {

    private final CostExplorerClient ce;

    public ServiceCostRawService(CostExplorerClient ce) {
        this.ce = ce;
    }

    public Map<String, Object> getServiceWiseCostRaw(
            String accountId,
            String start,
            String end
    ) {

        GetCostAndUsageRequest request =
                GetCostAndUsageRequest.builder()
                        .timePeriod(DateInterval.builder()
                                .start(start)
                                .end(end)
                                .build())
                        .granularity(Granularity.MONTHLY)
                        .metrics("UnblendedCost")
                        .groupBy(
                                GroupDefinition.builder()
                                        .type(GroupDefinitionType.DIMENSION)
                                        .key("SERVICE")
                                        .build()
                        )
                        .filter(
                                Expression.builder()
                                        .dimensions(
                                                DimensionValues.builder()
                                                        .key("LINKED_ACCOUNT")
                                                        .values(accountId)
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        GetCostAndUsageResponse response = ce.getCostAndUsage(request);

        List<Map<String, Object>> services = new ArrayList<>();

        for (ResultByTime rbt : response.resultsByTime()) {
            for (Group group : rbt.groups()) {

                MetricValue mv = group.metrics().get("UnblendedCost");

                services.add(Map.of(
                        "service", group.keys().get(0),
                        "amount", mv.amount(),
                        "unit", mv.unit()
                ));
            }
        }

        return Map.of(
                "start", start,
                "end", end,
                "services", services
        );
    }
}

