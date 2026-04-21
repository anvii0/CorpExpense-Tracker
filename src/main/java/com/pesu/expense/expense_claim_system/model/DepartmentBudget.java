package com.pesu.expense.expense_claim_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "department_budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentBudget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String departmentName;
    private Double totalAllocated;
    private Double currentlySpent;

    public boolean canApprove(Double amount) {
        return (currentlySpent + amount) <= totalAllocated;
    }

    public Double getRemainingBudget() {
        return totalAllocated - currentlySpent;
    }

    public boolean isNearThreshold() {
        return totalAllocated > 0 && getRemainingBudget() <= (totalAllocated * 0.2);
    }

    public void deduct(Double amount) {
        this.currentlySpent += amount;
    }
}
