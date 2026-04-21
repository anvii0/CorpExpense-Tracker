package com.pesu.expense.expense_claim_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense_claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "total_amount_usd")
    @Builder.Default
    private Double convertedAmountUsd = 0.0;

    private LocalDate submitDate;
    private LocalDate lastUpdatedDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Builder.Default
    private String paymentMethod = "DirectDeposit";

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<ExpenseEntry> entries = new ArrayList<>();

    public void addEntry(ExpenseEntry entry) {
        entry.setClaim(this);
        entries.add(entry);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.convertedAmountUsd = entries.stream()
                .map(ExpenseEntry::getConvertedAmountUsd)
                .filter(value -> value != null)
                .reduce(0.0, Double::sum);
        this.lastUpdatedDate = LocalDate.now();
    }

    public boolean allEntriesCompliant() {
        return !entries.isEmpty() && entries.stream().allMatch(ExpenseEntry::isSubmissionReady);
    }
}
