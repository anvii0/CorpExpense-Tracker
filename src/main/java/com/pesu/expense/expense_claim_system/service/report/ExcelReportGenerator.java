package com.pesu.expense.expense_claim_system.service.report;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Component("ExcelReport")
public class ExcelReportGenerator implements ReportGenerator {

    @Override
    public byte[] generateReport(List<Expense> expenses, LocalDate from, LocalDate to) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Expense Report");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Employee");
            header.createCell(3).setCellValue("Amount USD");
            header.createCell(4).setCellValue("Status");

            int rowIndex = 1;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(expense.getId());
                row.createCell(1).setCellValue(expense.getTitle());
                row.createCell(2).setCellValue(expense.getEmployee() != null ? expense.getEmployee().getName() : "NA");
                row.createCell(3).setCellValue(expense.getConvertedAmountUsd());
                row.createCell(4).setCellValue(expense.getStatus().name());
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate Excel report", exception);
        }
    }

    @Override
    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String fileName() {
        return "monthly-expenses.xlsx";
    }
}
