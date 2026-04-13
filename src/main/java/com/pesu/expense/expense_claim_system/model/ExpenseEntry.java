package com.pesu.expense.expense_claim_system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "expense_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EntryCategory category;

    private Double amount;
    private String currency;
    private Double convertedAmountUsd;
    private String description;
    private String merchant;
    private LocalDate expenseDate;
    private String receiptImagePath;
    private boolean receiptVerified;
    private boolean policyCompliant;
    private boolean manualReviewRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Expense claim;

    public boolean isSubmissionReady() {
        return policyCompliant && (receiptVerified || manualReviewRequired || receiptImagePath == null);
    }
}
