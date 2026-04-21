package com.pesu.expense.expense_claim_system.service.notification;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    
    private final List<ExpenseEventObserver> observers;
    
    @Autowired
    public NotificationService(List<ExpenseEventObserver> observers) {
        this.observers = observers;
    }
    
    public void notify(Expense expense, String message) {
        for(ExpenseEventObserver observer : observers) {
            observer.onStatusChange(expense, message);
        }
    }
}
