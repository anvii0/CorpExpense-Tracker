package com.pesu.expense.expense_claim_system.service.approval;

import org.springframework.stereotype.Component;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;

@Component
public class CfoApprovalHandler extends ApprovalHandler {

    @Override
    public boolean supports(Expense expense) {
        return expense.getConvertedAmountUsd() > 5000.0;
    }

    @Override
    public void approve(Expense expense, User approver) {
        // Validate that approver is a CFO
        if (!"CFO".equals(approver.getRole())) {
            throw new IllegalArgumentException("Only CFO can approve claims over $5000. Current role: " + approver.getRole());
        }

        expense.setStatus(ExpenseStatus.FULLY_APPROVED);
    }
}
