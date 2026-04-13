package com.pesu.expense.expense_claim_system.service.payment;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import org.springframework.stereotype.Component;

@Component("DirectDeposit")
public class DirectDepositStrategy implements PaymentStrategy {
    @Override
    public void pay(Expense expense) {
        System.out.println("Processing Direct Deposit for Expense ID: " + expense.getId() + " Amount: $" + expense.getConvertedAmountUsd());
        expense.setStatus(ExpenseStatus.PAID);
    }
}
