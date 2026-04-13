package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.User;

public abstract class ApprovalHandler {
    protected ApprovalHandler nextHandler;

    public ApprovalHandler setNextHandler(ApprovalHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    public abstract boolean supports(Expense expense);

    public abstract void approve(Expense expense, User approver);

    protected void forward(Expense expense, User approver) {
        if (nextHandler != null) {
            nextHandler.approve(expense, approver);
        }
    }
}
