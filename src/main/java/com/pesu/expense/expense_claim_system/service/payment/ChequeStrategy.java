package com.pesu.expense.expense_claim_system.service.payment;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.springframework.stereotype.Component;

@Component("Cheque")
public class ChequeStrategy implements PaymentStrategy {
    @Override
    public void pay(Expense expense) {
        System.out.println("Processing Physical Cheque for Expense ID: " + expense.getId() + " Amount: $" + expense.getConvertedAmountUsd());
        expense.setStatus("PAID");
    }
}
