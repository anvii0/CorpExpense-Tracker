package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;

// Owner: Student 2 (Chain of Responsibility)
public abstract class ApprovalHandler {
    protected ApprovalHandler nextHandler;

    public void setNextHandler(ApprovalHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract void approve(Expense expense);
}
