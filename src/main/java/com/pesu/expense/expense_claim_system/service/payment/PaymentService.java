package com.pesu.expense.expense_claim_system.service.payment;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {
    
    private final Map<String, PaymentStrategy> paymentStrategies;
    
    @Autowired
    public PaymentService(Map<String, PaymentStrategy> paymentStrategies) {
        this.paymentStrategies = paymentStrategies;
    }
    
    public void processPayment(Expense expense) {
        String method = expense.getPaymentMethod();
        if(method == null || method.isEmpty()) method = "DirectDeposit"; // default
        
        PaymentStrategy strategy = paymentStrategies.get(method);
        if(strategy != null) {
            strategy.pay(expense);
        } else {
            System.err.println("No matching payment strategy found for: " + method);
        }
    }
}
