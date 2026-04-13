package com.pesu.expense.expense_claim_system.service.report;

import com.pesu.expense.expense_claim_system.model.Expense;

import java.time.LocalDate;
import java.util.List;

public interface ReportGenerator {
    byte[] generateReport(List<Expense> expenses, LocalDate from, LocalDate to);
    String contentType();
    String fileName();
}
