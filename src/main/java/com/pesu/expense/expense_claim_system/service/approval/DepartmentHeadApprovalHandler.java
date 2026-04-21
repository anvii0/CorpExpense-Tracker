package com.pesu.expense.expense_claim_system.service.approval;

import org.springframework.stereotype.Component;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;

@Component
public class DepartmentHeadApprovalHandler extends ApprovalHandler {

    private static final double MIN_LIMIT = 1000.01;
    private static final double MAX_LIMIT = 5000.0;

    @Override
    public boolean supports(Expense expense) {
        double amount = expense.getConvertedAmountUsd();
        return amount > MIN_LIMIT - 0.01 && amount <= MAX_LIMIT;
    }

    @Override
    public void approve(Expense expense, User approver) {
        // Validate that approver is a Department Head
        if (!"DEPT_HEAD".equals(approver.getRole())) {
            throw new IllegalArgumentException("Only Department Head can approve at this stage. Current role: " + approver.getRole());
        }

        if (supports(expense)) {
            // Department Head is final approver for amounts within DH limit
            expense.setStatus(ExpenseStatus.FULLY_APPROVED);
        } else {
            expense.setStatus(ExpenseStatus.PENDING_CFO);
            forward(expense, approver);
        }
    }
}
