package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import org.springframework.stereotype.Component;

@Component("TRAVEL")
public class TravelPolicy implements PolicyStrategy {
    @Override
    public boolean isValid(ExpenseEntry entry) {
        return entry.getConvertedAmountUsd() <= 1500 && entry.isReceiptVerified();
    }

    @Override
    public String violationMessage() {
        return "Travel claims must be below $1500 per entry and have a verified receipt.";
    }
}
