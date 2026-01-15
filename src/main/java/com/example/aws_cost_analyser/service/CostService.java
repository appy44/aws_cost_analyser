package com.example.aws_cost_analyser.service;

import com.example.aws_cost_analyser.entity.CombinedCostResponse;
import com.example.aws_cost_analyser.entity.CostResponse;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CostService {

    private final CostExplorerClient costExplorerClient;

    public CostService(CostExplorerClient costExplorerClient) {
        this.costExplorerClient = costExplorerClient;
    }

    // ===================== PUBLIC API =====================

    public CombinedCostResponse getCombinedCost(String accountId, String start, String end) {

        Map<String, Object> monthly = getMonthlyTotal(accountId, start, end);

        Map<String, Object> daily = getDailyByService(accountId, start, end);

        Map<String, Object> byService = getMonthlyByService(accountId, start, end);

        Map<String, Object> byAccount = getOrgMonthlyByAccount(start, end);

        return new CombinedCostResponse(monthly, daily, byService, byAccount);
    }

    // ===================== COMMON CORE METHOD =====================

    private GetCostAndUsageResponse fetchCostData(String start, String end, Granularity granularity, List<GroupDefinition> groupBy, Expression filter) {

        GetCostAndUsageRequest.Builder builder = GetCostAndUsageRequest.builder().timePeriod(DateInterval.builder().start(start).end(end).build()).granularity(granularity).metrics("UnblendedCost");

        if (groupBy != null && !groupBy.isEmpty()) {
            builder.groupBy(groupBy);
        }

        if (filter != null) {
            builder.filter(filter);
        }

        return costExplorerClient.getCostAndUsage(builder.build());
    }

    // ===================== MONTHLY TOTAL =====================

    private Map<String, Object> getMonthlyTotal(String accountId, String start, String end) {

        Expression filter = linkedAccountFilter(accountId);

        GetCostAndUsageResponse response = fetchCostData(start, end, Granularity.MONTHLY, null, filter);

        Map<String, Object> result = new HashMap<>();

        if (response.resultsByTime().isEmpty()) {
            return result;
        }

        MetricValue cost = response.resultsByTime().get(0).total().get("UnblendedCost");

        result.put(accountId, Map.of("total", new BigDecimal(cost.amount()), "currency", cost.unit()));

        return result;
    }

    // ===================== DAILY BY SERVICE =====================

    private Map<String, Object> getDailyByService(String accountId, String start, String end) {

        Expression filter = linkedAccountFilter(accountId);

        List<GroupDefinition> groupBy = List.of(serviceGroup());

        GetCostAndUsageResponse response = fetchCostData(start, end, Granularity.DAILY, groupBy, filter);

        Map<String, Object> result = new HashMap<>();

        for (ResultByTime rbt : response.resultsByTime()) {
            String date = rbt.timePeriod().start();

            for (Group group : rbt.groups()) {
                String service = group.keys().get(0);
                BigDecimal cost = new BigDecimal(group.metrics().get("UnblendedCost").amount());

                result.computeIfAbsent(accountId, a -> new HashMap<>());
                Map<String, Object> serviceMap = (Map<String, Object>) result.get(accountId);

                serviceMap.computeIfAbsent(service, s -> new HashMap<>());
                Map<String, BigDecimal> dailyMap = (Map<String, BigDecimal>) serviceMap.get(service);

                dailyMap.merge(date, cost, BigDecimal::add);
            }
        }

        return result;
    }

    // ===================== MONTHLY BY SERVICE =====================

    private Map<String, Object> getMonthlyByService(String accountId, String start, String end) {

        Expression filter = linkedAccountFilter(accountId);

        List<GroupDefinition> groupBy = List.of(serviceGroup());

        GetCostAndUsageResponse response = fetchCostData(start, end, Granularity.MONTHLY, groupBy, filter);

        Map<String, Object> result = new HashMap<>();
        Map<String, BigDecimal> serviceTotals = new HashMap<>();

        for (ResultByTime rbt : response.resultsByTime()) {
            for (Group group : rbt.groups()) {
                String service = group.keys().get(0);
                BigDecimal cost = new BigDecimal(group.metrics().get("UnblendedCost").amount());

                serviceTotals.merge(service, cost, BigDecimal::add);
            }
        }

        result.put(accountId, serviceTotals);
        return result;
    }

    // ===================== ORG MONTHLY BY ACCOUNT =====================

    private Map<String, Object> getOrgMonthlyByAccount(String start, String end) {

        List<GroupDefinition> groupBy = List.of(linkedAccountGroup());

        GetCostAndUsageResponse response = fetchCostData(start, end, Granularity.MONTHLY, groupBy, null);

        Map<String, Object> result = new HashMap<>();

        for (ResultByTime rbt : response.resultsByTime()) {
            for (Group group : rbt.groups()) {
                String accountId = group.keys().get(0);
                BigDecimal cost = new BigDecimal(group.metrics().get("UnblendedCost").amount());

                result.put(accountId, cost);
            }
        }

        return result;
    }

    // ===================== HELPERS =====================

    private Expression linkedAccountFilter(String accountId) {
        return Expression.builder().dimensions(DimensionValues.builder().key("LINKED_ACCOUNT").values(accountId).build()).build();
    }

    private GroupDefinition serviceGroup() {
        return GroupDefinition.builder().type(GroupDefinitionType.DIMENSION).key("SERVICE").build();
    }

    private GroupDefinition linkedAccountGroup() {
        return GroupDefinition.builder().type(GroupDefinitionType.DIMENSION).key("LINKED_ACCOUNT").build();
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
