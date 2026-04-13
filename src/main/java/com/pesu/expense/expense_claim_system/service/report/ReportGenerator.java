package com.pesu.expense.expense_claim_system.service.report;

import com.pesu.expense.expense_claim_system.model.Expense;
import java.util.List;

// Owner: Student 4 - Template Method Pattern
public abstract class ReportGenerator {
    
    // Template method defining the skeleton of generating a report
    public final String generateReport(List<Expense> expenses) {
        StringBuilder report = new StringBuilder();
        report.append(generateHeader());
        report.append(generateBody(expenses));
        report.append(generateFooter());
        return report.toString();
    }
    
    protected abstract String generateHeader();
    protected abstract String generateBody(List<Expense> expenses);
    protected abstract String generateFooter();
}
