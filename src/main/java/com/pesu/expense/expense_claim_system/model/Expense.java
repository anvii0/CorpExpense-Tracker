package com.pesu.expense.expense_claim_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Owner: Student 1 (Builder Pattern implementation)
public class Expense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private Double amount;
    private String currency; // Default USD
    private Double convertedAmountUsd; 
    
    private LocalDate submitDate;
    private String status; // SUBMITTED, PENDING_MANAGER_APPROVAL, PENDING_FINANCE_APPROVAL, PENDING_DIRECTOR_APPROVAL, APPROVED, REJECTED, PAID
    
    private String receiptImagePath; // Minor Feature: Receipt Attachment
    
    private String paymentMethod; // Direct Deposit, Cheque
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;
}
