package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;

public class ManagerApprovalHandler extends ApprovalHandler {
    @Override
    public void approve(Expense expense) {
        if (expense.getConvertedAmountUsd() <= 1000) {
            expense.setStatus("APPROVED_BY_MANAGER");
            System.out.println("Expense " + expense.getId() + " approved by Manager.");
        } else if (nextHandler != null) {
            expense.setStatus("PENDING_FINANCE_APPROVAL");
            nextHandler.approve(expense);
        }
    }
}
