package com.pesu.expense.expense_claim_system.service.approval;

import org.springframework.stereotype.Component;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;

@Component
public class TeamLeadApprovalHandler extends ApprovalHandler {

    private static final double LIMIT = 1000.0;

    @Override
    public boolean supports(Expense expense) {
        return expense.getConvertedAmountUsd() <= LIMIT;
    }

    @Override
    public void approve(Expense expense, User approver) {
        // Validate that approver is a Team Lead
        if (!"TEAM_LEAD".equals(approver.getRole())) {
            throw new IllegalArgumentException("Only Team Lead can approve at this stage. Current role: " + approver.getRole());
        }

        if (supports(expense)) {
            // Team Lead is the final approver for amounts within the TL limit
            expense.setStatus(ExpenseStatus.FULLY_APPROVED);
        } else {
            expense.setStatus(ExpenseStatus.PENDING_DEPT_HEAD);
            forward(expense, approver);
        }
    }
}
