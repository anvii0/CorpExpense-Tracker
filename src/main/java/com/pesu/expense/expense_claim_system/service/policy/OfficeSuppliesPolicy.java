package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import org.springframework.stereotype.Component;

@Component("OFFICE_SUPPLIES")
public class OfficeSuppliesPolicy implements PolicyStrategy {
    @Override
    public boolean isValid(ExpenseEntry entry) {
        return entry.getConvertedAmountUsd() <= 2000;
    }

    @Override
    public String violationMessage() {
        return "Office supplies must be below $2000 per entry.";
    }
}
