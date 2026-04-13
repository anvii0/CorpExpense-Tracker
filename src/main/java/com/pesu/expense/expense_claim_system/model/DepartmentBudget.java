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
}
