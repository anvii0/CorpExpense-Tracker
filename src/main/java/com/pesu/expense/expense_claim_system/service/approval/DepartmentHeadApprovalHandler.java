package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;
import org.springframework.stereotype.Component;

@Component
public class DepartmentHeadApprovalHandler extends ApprovalHandler {

    private static final double LIMIT = 5000.0;

    @Override
    public boolean supports(Expense expense) {
        return expense.getConvertedAmountUsd() <= LIMIT;
    }

    @Override
    public void approve(Expense expense, User approver) {
        if (supports(expense)) {
            expense.setStatus(ExpenseStatus.APPROVED_BY_DH);
        } else {
            expense.setStatus(ExpenseStatus.PENDING_CFO);
            forward(expense, approver);
        }
    }
}
