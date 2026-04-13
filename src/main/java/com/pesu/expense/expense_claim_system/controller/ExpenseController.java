package com.pesu.expense.expense_claim_system.controller;

import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.User;
import com.pesu.expense.expense_claim_system.repository.ExpenseRepository;
import com.pesu.expense.expense_claim_system.repository.UserRepository;
import com.pesu.expense.expense_claim_system.service.approval.ApprovalService;
import com.pesu.expense.expense_claim_system.service.currency.CurrencyAdapter;
import com.pesu.expense.expense_claim_system.service.notification.NotificationService;
import com.pesu.expense.expense_claim_system.service.payment.PaymentService;
import com.pesu.expense.expense_claim_system.service.report.ReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ExpenseController {

    @Autowired private ExpenseRepository expenseRepo;
    @Autowired private UserRepository userRepo;
    
    // Services using Patterns
    @Autowired private CurrencyAdapter currencyAdapter;
    @Autowired private ApprovalService approvalService;
    @Autowired private PaymentService paymentService;
    @Autowired private NotificationService notificationService;
    
    @Autowired 
    @Qualifier("CsvReport")
    private ReportGenerator reportGenerator;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("expenses", expenseRepo.findAll());
        return "dashboard";
    }

    @GetMapping("/submit")
    public String submitForm(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "submit-claim";
    }

    @PostMapping("/submit")
    public String processSubmit(
            @RequestParam("title") String title,
            @RequestParam("amount") Double amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description,
            @RequestParam("userId") Long userId,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "receipt", required = false) MultipartFile receipt) {
        
        User employee = userRepo.findById(userId).orElseThrow();
        Double mappedAmountUsd = currencyAdapter.convertToUSD(amount, currency);
        
        String receiptPath = (receipt != null && !receipt.isEmpty()) ? receipt.getOriginalFilename() : null;

        // Builder Pattern
        Expense newExpense = Expense.builder()
                .title(title)
                .amount(amount)
                .currency(currency.toUpperCase())
                .convertedAmountUsd(mappedAmountUsd)
                .description(description)
                .employee(employee)
                .submitDate(LocalDate.now())
                .status("SUBMITTED")
                .receiptImagePath(receiptPath)
                .paymentMethod(paymentMethod)
                .build();
        
        expenseRepo.save(newExpense);
        notificationService.notify(newExpense, "Claim Submitted Successfully. Awaiting Manager Approval.");
        
        return "redirect:/";
    }

    @PostMapping("/approve/{id}")
    public String approveExpense(@PathVariable Long id) {
        Expense expense = expenseRepo.findById(id).orElseThrow();
        approvalService.processApproval(expense);
        expenseRepo.save(expense);
        notificationService.notify(expense, "Expense progressed in Approval Chain. Status: " + expense.getStatus());
        return "redirect:/";
    }

    @PostMapping("/reimburse/{id}")
    public String reimburseExpense(@PathVariable Long id) {
        Expense expense = expenseRepo.findById(id).orElseThrow();
        paymentService.processPayment(expense);
        expenseRepo.save(expense);
        notificationService.notify(expense, "Reimbursement dispatched using " + expense.getPaymentMethod());
        return "redirect:/";
    }
    
    @GetMapping(value = "/report", produces = "text/csv")
    @ResponseBody
    public ResponseEntity<String> downloadReport() {
        List<Expense> expenses = expenseRepo.findAll();
        String csvData = reportGenerator.generateReport(expenses);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"monthly-expenses.csv\"")
                .body(csvData);
    }
}
