package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import org.springframework.stereotype.Component;

@Component("SOFTWARE_SUBSCRIPTION")
public class SoftwareSubscriptionPolicy implements PolicyStrategy {
    @Override
    public boolean isValid(ExpenseEntry entry) {
        return entry.getConvertedAmountUsd() <= 500;
    }

    @Override
    public String violationMessage() {
        return "Software subscriptions above $500 require manual procurement approval.";
    }
}
