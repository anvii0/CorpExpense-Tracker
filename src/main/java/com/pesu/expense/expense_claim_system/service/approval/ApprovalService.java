package com.pesu.expense.expense_claim_system.service.approval;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.springframework.stereotype.Service;

@Service
public class ApprovalService {
    
    public void processApproval(Expense expense) {
        ApprovalHandler manager = new ManagerApprovalHandler();
        ApprovalHandler finance = new FinanceApprovalHandler();
        ApprovalHandler director = new DirectorApprovalHandler();
        
        manager.setNextHandler(finance);
        finance.setNextHandler(director);
        
        // Start the chain
        manager.approve(expense);
    }
}
