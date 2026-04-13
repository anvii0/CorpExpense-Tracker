package com.pesu.expense.expense_claim_system.service.claim;

import com.pesu.expense.expense_claim_system.model.EntryCategory;
import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;
import com.pesu.expense.expense_claim_system.repository.ExpenseEntryRepository;
import com.pesu.expense.expense_claim_system.repository.ExpenseRepository;
import com.pesu.expense.expense_claim_system.service.approval.ApprovalService;
import com.pesu.expense.expense_claim_system.service.audit.AuditTrailService;
import com.pesu.expense.expense_claim_system.service.budget.BudgetService;
import com.pesu.expense.expense_claim_system.service.currency.CurrencyService;
import com.pesu.expense.expense_claim_system.service.notification.NotificationService;
import com.pesu.expense.expense_claim_system.service.payment.PaymentService;
import com.pesu.expense.expense_claim_system.service.policy.PolicyStrategy;
import com.pesu.expense.expense_claim_system.service.policy.PolicyStrategyFactory;
import com.pesu.expense.expense_claim_system.service.receipt.ReceiptExtractionResult;
import com.pesu.expense.expense_claim_system.service.receipt.ReceiptOCRService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

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
            NotificationService notificationService) {
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
        Expense saved = expenseRepository.save(expense);
        auditTrailService.logAction(employee, "CLAIM_DRAFT_CREATED", saved, "Claim draft created");
        return saved;
    }

    public ExpenseEntry addEntry(
            Long claimId,
            EntryCategory category,
            Double amount,
            String currency,
            String description,
            MultipartFile receipt) {
        Expense claim = findClaim(claimId);
        ReceiptExtractionResult extractionResult = receiptOCRService.extractData(receipt);
        boolean verified = receipt != null && !receipt.isEmpty()
                && receiptOCRService.verifyAmountMatches(extractionResult.getExtractedAmount(), amount);
        double converted = currencyService.convertToUsd(amount, currency);

        ExpenseEntry entry = new ExpenseEntry();
        entry.setCategory(category);
        entry.setAmount(amount);
        entry.setCurrency(currency.toUpperCase());
        entry.setConvertedAmountUsd(converted);
        entry.setDescription(description);
        entry.setMerchant(extractionResult.getMerchant());
        entry.setExpenseDate(LocalDate.now());
        entry.setReceiptImagePath(receipt != null && !receipt.isEmpty() ? receipt.getOriginalFilename() : null);
        entry.setReceiptVerified(verified);
        entry.setManualReviewRequired(extractionResult.isManualEntryRequired() || !verified);

        PolicyStrategy policy = policyStrategyFactory.getPolicy(category);
        entry.setPolicyCompliant(policy != null && policy.isValid(entry));

        claim.addEntry(entry);
        expenseRepository.save(claim);
        auditTrailService.logAction(claim.getEmployee(), "RECEIPT_UPLOADED", claim,
                "Entry added in " + category + " category. OCR merchant=" + extractionResult.getMerchant());
        return expenseEntryRepository.save(entry);
    }

    public Expense submitClaim(Long claimId) {
        Expense claim = findClaim(claimId);
        if (!claim.allEntriesCompliant()) {
            throw new IllegalStateException("All entries must be compliant before submission.");
        }
        approvalService.submitForApproval(claim);
        claim.setSubmitDate(LocalDate.now());
        auditTrailService.logAction(claim.getEmployee(), "SUBMITTED", claim, "Claim submitted for approval");
        notificationService.notify(claim, "Claim submitted for approval.");
        return expenseRepository.save(claim);
    }

    public Expense approveClaim(Long claimId) {
        Expense claim = findClaim(claimId);
        approvalService.processApproval(claim, claim.getEmployee());
        auditTrailService.logAction(null, "APPROVED", claim, "Claim moved to " + claim.getStatus());
        notificationService.notify(claim, "Claim status changed to " + claim.getStatus());
        return expenseRepository.save(claim);
    }

    public Expense rejectClaim(Long claimId) {
        Expense claim = findClaim(claimId);
        approvalService.reject(claim);
        auditTrailService.logAction(null, "REJECTED", claim, "Claim rejected in approval workflow");
        notificationService.notify(claim, "Claim rejected.");
        return expenseRepository.save(claim);
    }

    public Expense reimburseClaim(Long claimId) {
        Expense claim = findClaim(claimId);
        if (!budgetService.checkAvailability(claim.getConvertedAmountUsd(), claim.getEmployee().getDepartment())) {
            throw new IllegalStateException("Department budget is insufficient for reimbursement.");
        }
        budgetService.deductBudget(claim.getConvertedAmountUsd(), claim.getEmployee().getDepartment());
        paymentService.processPayment(claim);
        auditTrailService.logAction(null, "REPORT_EXPORTED", claim, "Budget checked and reimbursement processed");
        notificationService.notify(claim, "Claim reimbursed using " + claim.getPaymentMethod());
        return expenseRepository.save(claim);
    }

    public Expense findClaim(Long id) {
        return expenseRepository.findById(id).orElseThrow();
    }

    public List<Expense> allClaims() {
        return expenseRepository.findAll();
    }
}
