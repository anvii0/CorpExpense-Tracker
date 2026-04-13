package com.pesu.expense.expense_claim_system.service.report;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("CsvReport")
public class CsvReportGenerator extends ReportGenerator {
    
    @Override
    protected String generateHeader() {
        return "ID,Title,Amount(USD),Status\n";
    }

    @Override
    protected String generateBody(List<Expense> expenses) {
        StringBuilder body = new StringBuilder();
        for(Expense exp : expenses) {
            body.append(exp.getId()).append(",")
                .append(exp.getTitle()).append(",")
                .append(exp.getConvertedAmountUsd()).append(",")
                .append(exp.getStatus()).append("\n");
        }
        return body.toString();
    }

    @Override
    protected String generateFooter() {
        return "--- End of Report ---\n";
    }
}
