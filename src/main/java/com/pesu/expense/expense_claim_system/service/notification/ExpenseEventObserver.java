package com.pesu.expense.expense_claim_system.service.notification;

import com.pesu.expense.expense_claim_system.model.Expense;

// Owner: Student 3 (Minor) - Observer Pattern
public interface ExpenseEventObserver {
    void onStatusChange(Expense expense, String message);
}
