package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        expense.setStatus(ExpenseStatus.PENDING_TEAM_LEAD);
    }

    public void processApproval(Expense expense, User approver) {
        teamLeadApprovalHandler.approve(expense, approver);

        if (expense.getStatus() == ExpenseStatus.APPROVED_BY_TL
                || expense.getStatus() == ExpenseStatus.APPROVED_BY_DH) {
            expense.setStatus(ExpenseStatus.FULLY_APPROVED);
        }
    }

    public void reject(Expense expense) {
        expense.setStatus(ExpenseStatus.REJECTED);
    }
}
