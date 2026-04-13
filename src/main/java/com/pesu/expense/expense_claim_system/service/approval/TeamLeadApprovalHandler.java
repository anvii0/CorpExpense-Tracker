package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;
import org.springframework.stereotype.Component;

@Component
public class TeamLeadApprovalHandler extends ApprovalHandler {

    private static final double LIMIT = 500.0;

    @Override
    public boolean supports(Expense expense) {
        return expense.getConvertedAmountUsd() <= LIMIT;
    }

    @Override
    public void approve(Expense expense, User approver) {
        if (supports(expense)) {
            expense.setStatus(ExpenseStatus.APPROVED_BY_TL);
        } else {
            expense.setStatus(ExpenseStatus.PENDING_DEPT_HEAD);
            forward(expense, approver);
        }
    }
}
