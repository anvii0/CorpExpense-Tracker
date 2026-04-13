package com.pesu.expense.expense_claim_system.service.payment;

import com.pesu.expense.expense_claim_system.model.Expense;

// Owner: Student 3 (Strategy Pattern)
public interface PaymentStrategy {
    void pay(Expense expense);
}
