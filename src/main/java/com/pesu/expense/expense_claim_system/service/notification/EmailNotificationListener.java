package com.pesu.expense.expense_claim_system.service.notification;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener implements ExpenseEventObserver {
    @Override
    public void onStatusChange(Expense expense, String message) {
        System.out.println(">>> [EMAIL NOTIFICATION] to " + 
            (expense.getEmployee() != null ? expense.getEmployee().getEmail() : "Unknown") 
            + ": Expense #" + expense.getId() + " - " + message);
    }
}
