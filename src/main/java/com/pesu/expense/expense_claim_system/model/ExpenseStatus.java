package com.pesu.expense.expense_claim_system.model;

public enum ExpenseStatus {
    DRAFT,
    PENDING_TEAM_LEAD,
    APPROVED_BY_TL,
    PENDING_DEPT_HEAD,
    APPROVED_BY_DH,
    PENDING_CFO,
    FULLY_APPROVED,
    REJECTED,
    PAID
}
