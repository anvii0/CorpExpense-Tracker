package com.pesu.expense.expense_claim_system.service.budget;

import com.pesu.expense.expense_claim_system.model.DepartmentBudget;
import com.pesu.expense.expense_claim_system.repository.DepartmentBudgetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final DepartmentBudgetRepository departmentBudgetRepository;

    public BudgetService(DepartmentBudgetRepository departmentBudgetRepository) {
        this.departmentBudgetRepository = departmentBudgetRepository;
    }

    public boolean checkAvailability(Double amount, String department) {
        return departmentBudgetRepository.findByDepartmentName(department)
                .map(budget -> budget.canApprove(amount))
                .orElse(false);
    }

    public void deductBudget(Double amount, String department) {
        departmentBudgetRepository.findByDepartmentName(department).ifPresent(budget -> {
            budget.deduct(amount);
            departmentBudgetRepository.save(budget);
        });
    }

    public List<DepartmentBudget> allBudgets() {
        return departmentBudgetRepository.findAll();
    }

    public String budgetSummary(String department) {
        return departmentBudgetRepository.findByDepartmentName(department)
                .map(budget -> {
                    if (!budget.canApprove(0.0)) {
                        return "Budget exhausted";
                    }
                    if (budget.isNearThreshold()) {
                        return "Budget threshold exceeded warning";
                    }
                    return "Budget available";
                })
                .orElse("No budget configured");
    }
}
