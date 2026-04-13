package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;

public class FinanceApprovalHandler extends ApprovalHandler {
    @Override
    public void approve(Expense expense) {
        if (expense.getConvertedAmountUsd() > 1000 && expense.getConvertedAmountUsd() <= 5000) {
            expense.setStatus("APPROVED_BY_FINANCE");
            System.out.println("Expense " + expense.getId() + " approved by Finance.");
        } else if (nextHandler != null) {
            expense.setStatus("PENDING_DIRECTOR_APPROVAL");
            nextHandler.approve(expense);
        }
    }
}
