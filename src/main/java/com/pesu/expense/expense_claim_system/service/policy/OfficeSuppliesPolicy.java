package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import org.springframework.stereotype.Component;

@Component("OFFICE_SUPPLIES")
public class OfficeSuppliesPolicy implements PolicyStrategy {
    @Override
    public boolean isValid(ExpenseEntry entry) {
        return entry.getConvertedAmountUsd() <= 250;
    }

    @Override
    public String violationMessage() {
        return "Office supplies are capped at $250 per entry.";
    }
}
