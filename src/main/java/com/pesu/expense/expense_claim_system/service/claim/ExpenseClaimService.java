package com.pesu.expense.expense_claim_system.service.claim;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pesu.expense.expense_claim_system.model.EntryCategory;
import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;
import com.pesu.expense.expense_claim_system.repository.ExpenseEntryRepository;
import com.pesu.expense.expense_claim_system.repository.ExpenseRepository;
import com.pesu.expense.expense_claim_system.repository.UserRepository;
import com.pesu.expense.expense_claim_system.service.approval.ApprovalService;
import com.pesu.expense.expense_claim_system.service.audit.AuditTrailService;
import com.pesu.expense.expense_claim_system.service.budget.BudgetService;
import com.pesu.expense.expense_claim_system.service.currency.CurrencyService;
import com.pesu.expense.expense_claim_system.service.notification.NotificationService;
import com.pesu.expense.expense_claim_system.service.payment.PaymentService;
import com.pesu.expense.expense_claim_system.service.policy.PolicyStrategy;
import com.pesu.expense.expense_claim_system.service.policy.PolicyStrategyFactory;
import com.pesu.expense.expense_claim_system.service.receipt.ReceiptOCRService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ExpenseClaimService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseEntryRepository expenseEntryRepository;
    private final CurrencyService currencyService;
    private final ReceiptOCRService receiptOCRService;
    private final PolicyStrategyFactory policyStrategyFactory;
    private final ApprovalService approvalService;
    private final PaymentService paymentService;
    private final BudgetService budgetService;
    private final AuditTrailService auditTrailService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public ExpenseClaimService(
            ExpenseRepository expenseRepository,
            ExpenseEntryRepository expenseEntryRepository,
            CurrencyService currencyService,
            ReceiptOCRService receiptOCRService,
            PolicyStrategyFactory policyStrategyFactory,
            ApprovalService approvalService,
            PaymentService paymentService,
            BudgetService budgetService,
            AuditTrailService auditTrailService,
            NotificationService notificationService,
            UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseEntryRepository = expenseEntryRepository;
        this.currencyService = currencyService;
        this.receiptOCRService = receiptOCRService;
        this.policyStrategyFactory = policyStrategyFactory;
        this.approvalService = approvalService;
        this.paymentService = paymentService;
        this.budgetService = budgetService;
        this.auditTrailService = auditTrailService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public Expense createDraft(String title, String description, String paymentMethod, User employee) {
        Expense expense = Expense.builder()
                .title(title)
                .description(description)
                .paymentMethod(paymentMethod)
                .employee(employee)
                .submitDate(LocalDate.now())
                .lastUpdatedDate(LocalDate.now())
                .status(ExpenseStatus.DRAFT)
                .build();
        return expenseRepository.save(expense);
    }

    public ExpenseEntry addEntry(Long claimId, EntryCategory category, Double amount, String currency, String description, String department, MultipartFile receipt) {
        Expense claim = findClaim(claimId);
        receiptOCRService.extractData(receipt);
        double converted = currencyService.convertToUsd(amount, currency);

        ExpenseEntry entry = new ExpenseEntry();
        entry.setCategory(category);
        entry.setAmount(amount);
        entry.setCurrency(currency.toUpperCase());
        entry.setConvertedAmountUsd(converted);
        entry.setDescription(description);
        entry.setReceiptImagePath(receipt != null ? receipt.getOriginalFilename() : null);
        entry.setDepartment(department);
        
        PolicyStrategy policy = policyStrategyFactory.getPolicy(category);
        entry.setPolicyCompliant(policy != null && policy.isValid(entry));

        claim.addEntry(entry);
        expenseRepository.save(claim);
        return expenseEntryRepository.save(entry);
    }

    public Expense submitClaim(Long claimId) {
        Expense claim = findClaim(claimId);
        
        if (claim.getEntries().isEmpty()) {
            throw new IllegalStateException("Claim is empty.");
        }
        
        claim.setStatus(ExpenseStatus.PENDING_TEAM_LEAD); 
        claim.setSubmitDate(LocalDate.now());

        // FIX: Matches required signature (User, String, Expense, String)
        auditTrailService.logAction(claim.getEmployee(), "SUBMITTED", claim, "Claim submitted for approval");

        approvalService.submitForApproval(claim);
        
        return expenseRepository.save(claim);
    }

    public Expense approveClaim(Long claimId, User approver) {
        Expense claim = findClaim(claimId);
        
        if (claim.getEmployee().getEmail().equals(approver.getEmail())) {
            throw new RuntimeException("You cannot approve your own claim");
        }
        
        approvalService.processApproval(claim, approver);

        // FIX: Matches required signature (User, String, Expense, String)
        auditTrailService.logAction(approver, "APPROVED", claim, "Approved at current stage");

        Expense saved = expenseRepository.save(claim);

        // If the claim just reached FULLY_APPROVED, deduct budgets per-department
        // but do NOT process payment (leave reimbursement as a separate step).
        if (saved.getStatus() == ExpenseStatus.FULLY_APPROVED) {
            java.util.Map<String, Double> deptTotals = new java.util.HashMap<>();
            for (ExpenseEntry e : saved.getEntries()) {
                String dept = e.getDepartment() != null && !e.getDepartment().isEmpty()
                        ? e.getDepartment()
                        : saved.getEmployee().getDepartment();
                deptTotals.put(dept, deptTotals.getOrDefault(dept, 0.0)
                        + (e.getConvertedAmountUsd() != null ? e.getConvertedAmountUsd() : 0.0));
            }

            // Check availability for all departments
            for (var entry : deptTotals.entrySet()) {
                if (!budgetService.checkAvailability(entry.getValue(), entry.getKey())) {
                    throw new IllegalStateException("Budget exceeded for department: " + entry.getKey());
                }
            }

            // Deduct budgets (persisted) so dashboard reflects updated values
            for (var entry : deptTotals.entrySet()) {
                budgetService.deductBudget(entry.getValue(), entry.getKey());
            }

            // Log budget deduction event (keeps status as FULLY_APPROVED)
            auditTrailService.logAction(approver, "BUDGET_DEDUCTED", saved, "Budgets deducted on approval");
            saved = expenseRepository.save(saved);
        }

        return saved;
    }

    public Expense rejectClaim(Long claimId) {
        Expense claim = findClaim(claimId);
        approvalService.reject(claim);
        return expenseRepository.save(claim);
    }

    public Expense reimburseClaim(Long claimId) {
        Expense claim = findClaim(claimId);
        if (claim.getStatus() != ExpenseStatus.FULLY_APPROVED) {
            throw new IllegalStateException("Claim must be FULLY_APPROVED before reimbursement.");
        }

        // Aggregate amounts per department from entries
        java.util.Map<String, Double> deptTotals = new java.util.HashMap<>();
        for (ExpenseEntry e : claim.getEntries()) {
            String dept = e.getDepartment() != null && !e.getDepartment().isEmpty() ? e.getDepartment() : claim.getEmployee().getDepartment();
            deptTotals.put(dept, deptTotals.getOrDefault(dept, 0.0) + (e.getConvertedAmountUsd() != null ? e.getConvertedAmountUsd() : 0.0));
        }

        // Check availability for all departments
        for (var entry : deptTotals.entrySet()) {
            if (!budgetService.checkAvailability(entry.getValue(), entry.getKey())) {
                throw new IllegalStateException("Budget exceeded for department: " + entry.getKey());
            }
        }

        // Deduct budgets
        for (var entry : deptTotals.entrySet()) {
            budgetService.deductBudget(entry.getValue(), entry.getKey());
        }

        // Process payment and mark as PAID
        paymentService.processPayment(claim);
        claim.setStatus(ExpenseStatus.PAID);
        auditTrailService.logAction(null, "PAID", claim, "Claim paid after reimbursement");
        return expenseRepository.save(claim);
    }

    public Expense findClaim(Long id) {
        return expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Claim not found"));
    }

    public List<Expense> allClaims() {
        return expenseRepository.findAll();
    }
}

