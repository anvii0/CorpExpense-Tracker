package com.pesu.expense.expense_claim_system.service.report;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Component("CsvReport")
public class CsvReportGenerator implements ReportGenerator {

    @Override
    public byte[] generateReport(List<Expense> expenses, LocalDate from, LocalDate to) {
        StringBuilder body = new StringBuilder("ID,Title,Employee,Amount(USD),Status,Submitted\n");
        for(Expense exp : expenses) {
            body.append(exp.getId()).append(",")
                .append(exp.getTitle()).append(",")
                .append(exp.getEmployee() != null ? exp.getEmployee().getName() : "NA").append(",")
                .append(exp.getConvertedAmountUsd()).append(",")
                .append(exp.getStatus()).append(",")
                .append(exp.getSubmitDate()).append("\n");
        }
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String contentType() {
        return "text/csv";
    }

    @Override
    public String fileName() {
        return "monthly-expenses.csv";
    }
}
