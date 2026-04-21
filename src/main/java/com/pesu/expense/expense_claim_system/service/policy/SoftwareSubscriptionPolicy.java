package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import org.springframework.stereotype.Component;

@Component("SOFTWARE_SUBSCRIPTION")
public class SoftwareSubscriptionPolicy implements PolicyStrategy {
    @Override
    public boolean isValid(ExpenseEntry entry) {
        return entry.getConvertedAmountUsd() <= 2000;
    }

    @Override
    public String violationMessage() {
        return "Software subscriptions must be below $2000 per entry.";
    }
}
