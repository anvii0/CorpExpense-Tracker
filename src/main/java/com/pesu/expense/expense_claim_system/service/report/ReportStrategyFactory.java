package com.pesu.expense.expense_claim_system.service.report;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReportStrategyFactory {

    private final Map<String, ReportGenerator> reportGenerators;

    public ReportStrategyFactory(Map<String, ReportGenerator> reportGenerators) {
        this.reportGenerators = reportGenerators;
    }

    public ReportGenerator get(String format) {
        return switch (format.toLowerCase()) {
            case "pdf" -> reportGenerators.get("PdfReport");
            case "excel" -> reportGenerators.get("ExcelReport");
            default -> reportGenerators.get("CsvReport");
        };
    }
}
