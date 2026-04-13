package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import org.springframework.stereotype.Component;

@Component("MEALS")
public class MealsPolicy implements PolicyStrategy {
    @Override
    public boolean isValid(ExpenseEntry entry) {
        return entry.getConvertedAmountUsd() <= 75;
    }

    @Override
    public String violationMessage() {
        return "Meals are capped at $75 per entry.";
    }
}
