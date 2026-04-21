package com.pesu.expense.expense_claim_system.repository;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByEmployeeId(Long employeeId);
    List<Expense> findBySubmitDateBetween(LocalDate from, LocalDate to);
    List<Expense> findByStatus(ExpenseStatus status);
}
