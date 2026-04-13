package com.pesu.expense.expense_claim_system.service.report;

import com.pesu.expense.expense_claim_system.model.Expense;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Component("PdfReport")
public class PdfReportGenerator implements ReportGenerator {

    @Override
    public byte[] generateReport(List<Expense> expenses, LocalDate from, LocalDate to) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                content.newLineAtOffset(50, 780);
                content.showText("CorpExpense Report: " + from + " to " + to);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                int y = 760;
                for (Expense expense : expenses) {
                    content.newLineAtOffset(0, -20);
                    y -= 20;
                    if (y < 80) {
                        content.endText();
                        break;
                    }
                    content.showText(String.format(
                            "#%d | %s | %s | $%.2f | %s",
                            expense.getId(),
                            expense.getTitle(),
                            expense.getEmployee() != null ? expense.getEmployee().getName() : "NA",
                            expense.getConvertedAmountUsd(),
                            expense.getStatus()
                    ));
                }
                content.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate PDF report", exception);
        }
    }

    @Override
    public String contentType() {
        return "application/pdf";
    }

    @Override
    public String fileName() {
        return "monthly-expenses.pdf";
    }
}
