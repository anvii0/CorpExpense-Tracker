package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;

public class DirectorApprovalHandler extends ApprovalHandler {
    @Override
    public void approve(Expense expense) {
        if (expense.getConvertedAmountUsd() > 5000) {
            expense.setStatus("APPROVED_BY_DIRECTOR");
            System.out.println("Expense " + expense.getId() + " approved by Director.");
        } else {
            expense.setStatus("REJECTED");
        }
    }
}
