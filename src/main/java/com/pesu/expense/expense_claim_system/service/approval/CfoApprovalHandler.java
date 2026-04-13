package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;
import org.springframework.stereotype.Component;

@Component
public class CfoApprovalHandler extends ApprovalHandler {

    @Override
    public boolean supports(Expense expense) {
        return expense.getConvertedAmountUsd() > 5000.0;
    }

    @Override
    public void approve(Expense expense, User approver) {
        expense.setStatus(ExpenseStatus.FULLY_APPROVED);
    }
}
