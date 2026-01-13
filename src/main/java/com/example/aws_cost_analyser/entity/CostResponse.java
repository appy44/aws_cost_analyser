package com.example.aws_cost_analyser.entity;

import java.math.BigDecimal;

public class CostResponse {

    private String accountId;
    private String startDate;
    private String endDate;
    private String amount;
    private String bigDecimalAmount;
    private String currency;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {

        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBigDecimalAmount(String amount) {
        return bigDecimalAmount;

    }

    public void setBigDecimalAmount(String amount) {
        this.setAmount(amount);
        this.bigDecimalAmount = amount;
    }
// getters & setters
}
