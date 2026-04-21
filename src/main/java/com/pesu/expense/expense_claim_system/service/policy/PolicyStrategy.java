package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;

public interface PolicyStrategy {
    boolean isValid(ExpenseEntry entry);
    String violationMessage();
}
