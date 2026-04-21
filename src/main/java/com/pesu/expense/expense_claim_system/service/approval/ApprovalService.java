package com.pesu.expense.expense_claim_system.service.approval;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;

import jakarta.annotation.PostConstruct;

@Service
public class ApprovalService {

    private final TeamLeadApprovalHandler teamLeadApprovalHandler;
    private final DepartmentHeadApprovalHandler departmentHeadApprovalHandler;
    private final CfoApprovalHandler cfoApprovalHandler;

    @Autowired
    public ApprovalService(
            TeamLeadApprovalHandler teamLeadApprovalHandler,
            DepartmentHeadApprovalHandler departmentHeadApprovalHandler,
            CfoApprovalHandler cfoApprovalHandler) {
        this.teamLeadApprovalHandler = teamLeadApprovalHandler;
        this.departmentHeadApprovalHandler = departmentHeadApprovalHandler;
        this.cfoApprovalHandler = cfoApprovalHandler;
    }

    @PostConstruct
    void buildChain() {
        teamLeadApprovalHandler.setNextHandler(departmentHeadApprovalHandler)
                .setNextHandler(cfoApprovalHandler);
    }

    public void submitForApproval(Expense expense) {
        // Route to appropriate approval level based on USD amount
        double amountUsd = expense.getConvertedAmountUsd();
        
        if (amountUsd <= 1000.0) {
            expense.setStatus(ExpenseStatus.PENDING_TEAM_LEAD);
        } else if (amountUsd <= 5000.0) {
            expense.setStatus(ExpenseStatus.PENDING_DEPT_HEAD);
        } else {
            expense.setStatus(ExpenseStatus.PENDING_CFO);
        }
    }

    public void processApproval(Expense expense, User approver) {
        // Route to appropriate handler based on current expense status
        if (expense.getStatus() == ExpenseStatus.PENDING_TEAM_LEAD) {
            teamLeadApprovalHandler.approve(expense, approver);
        } else if (expense.getStatus() == ExpenseStatus.PENDING_DEPT_HEAD) {
            departmentHeadApprovalHandler.approve(expense, approver);
        } else if (expense.getStatus() == ExpenseStatus.PENDING_CFO) {
            cfoApprovalHandler.approve(expense, approver);
        } else {
            throw new IllegalStateException("Expense cannot be approved at current status: " + expense.getStatus());
        }
    }

    public void reject(Expense expense) {
        expense.setStatus(ExpenseStatus.REJECTED);
    }
}
