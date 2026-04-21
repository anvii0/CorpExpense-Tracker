package com.pesu.expense.expense_claim_system.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pesu.expense.expense_claim_system.model.EntryCategory;
import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.ExpenseStatus;
import com.pesu.expense.expense_claim_system.model.User;
import com.pesu.expense.expense_claim_system.repository.ExpenseRepository;
import com.pesu.expense.expense_claim_system.repository.UserRepository;
import com.pesu.expense.expense_claim_system.service.audit.AuditTrailService;
import com.pesu.expense.expense_claim_system.service.budget.BudgetService;
import com.pesu.expense.expense_claim_system.service.claim.ExpenseClaimService;
import com.pesu.expense.expense_claim_system.service.report.ReportGenerator;
import com.pesu.expense.expense_claim_system.service.report.ReportStrategyFactory;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class ExpenseController {

    private final ExpenseClaimService expenseClaimService;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetService budgetService;
    private final AuditTrailService auditTrailService;
    private final ReportStrategyFactory reportStrategyFactory;

    public ExpenseController(
            ExpenseClaimService expenseClaimService,
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            BudgetService budgetService,
            AuditTrailService auditTrailService,
            ReportStrategyFactory reportStrategyFactory) {
        this.expenseClaimService = expenseClaimService;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.budgetService = budgetService;
        this.auditTrailService = auditTrailService;
        this.reportStrategyFactory = reportStrategyFactory;
    }

    private User currentUser(HttpSession session) {
        String email = (String) session.getAttribute("currentUserEmail");
        if (email == null) email = "employee@corp.com";
        return userRepository.findByEmail(email).orElse(null);
    }

    @PostMapping("/switch-user")
    public String switchUser(@RequestParam String email,
                             @RequestParam(defaultValue = "/") String redirectTo,
                             HttpSession session) {
        session.setAttribute("currentUserEmail", email);
        return "redirect:" + redirectTo;
    }

    @GetMapping("/")
    public String dashboard(Model model, HttpSession session) {
        model.addAttribute("user", currentUser(session));
        model.addAttribute("claims", expenseClaimService.allClaims());
        model.addAttribute("budgets", budgetService.allBudgets());
        model.addAttribute("logs", auditTrailService.latestLogs());
        return "dashboard";
    }

    @GetMapping("/claims/new")
    public String newClaimForm(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "claim-form";
    }

    @PostMapping("/claims")
    public String createClaim(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String paymentMethod,
            @RequestParam Long userId,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElseThrow();
        Expense claim = expenseClaimService.createDraft(title, description, paymentMethod, user);
        redirectAttributes.addFlashAttribute("message", "Claim draft created.");
        return "redirect:/claims/" + claim.getId();
    }

    @GetMapping("/claims/{id}")
    public String viewClaim(@PathVariable Long id, Model model, HttpSession session) {
    Expense claim = expenseClaimService.findClaim(id);
    User user = currentUser(session);
    
    model.addAttribute("user", user);
    model.addAttribute("claim", claim);
    model.addAttribute("categories", EntryCategory.values());
    // Provide department names to the view for entry selection
    model.addAttribute("departments", budgetService.allBudgets().stream().map(b -> b.getDepartmentName()).toList());

    boolean canApprove = false;
    // Since getRole() returns a String, we compare it directly
    String role = user.getRole(); 

    if (claim.getStatus() == ExpenseStatus.PENDING_TEAM_LEAD && "TEAM_LEAD".equals(role)) {
        canApprove = true;
    } else if (claim.getStatus() == ExpenseStatus.PENDING_DEPT_HEAD && "DEPT_HEAD".equals(role)) {
        canApprove = true;
    } else if (claim.getStatus() == ExpenseStatus.PENDING_CFO && "CFO".equals(role)) {
        canApprove = true;
    }
    
    model.addAttribute("canApprove", canApprove);
    return "claim-detail";
}

    @PostMapping("/claims/{id}/entries")
    public String addEntry(
            @PathVariable Long id,
            @RequestParam EntryCategory category,
            @RequestParam Double amount,
            @RequestParam String currency,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "") String department,
            @RequestParam(required = false) MultipartFile receipt,
            RedirectAttributes redirectAttributes) {
        expenseClaimService.addEntry(id, category, amount, currency, description, department, receipt);
        redirectAttributes.addFlashAttribute("message", "Expense entry added.");
        return "redirect:/claims/" + id;
    }

    @PostMapping("/claims/{id}/submit")
    public String submitClaim(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expenseClaimService.submitClaim(id);
            redirectAttributes.addFlashAttribute("message", "Claim submitted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/claims/" + id;
    }

@PostMapping("/claims/{id}/approve")
public String approveClaim(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
    try {
        User user = currentUser(session);
        expenseClaimService.approveClaim(id, user);
        redirectAttributes.addFlashAttribute("message", "Claim approved by " + user.getName());
    } catch (Exception e) {
        // This prevents the 500 Error page from showing
        redirectAttributes.addFlashAttribute("error", "Approval Failed: " + e.getMessage());
    }
    return "redirect:/claims/" + id;
}

    @PostMapping("/claims/{id}/reject")
    public String rejectClaim(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        expenseClaimService.rejectClaim(id);
        redirectAttributes.addFlashAttribute("message", "Claim rejected.");
        return "redirect:/claims/" + id;
    }

    @PostMapping("/claims/{id}/reimburse")
    public String reimburseClaim(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expenseClaimService.reimburseClaim(id);
            redirectAttributes.addFlashAttribute("message", "Reimbursed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/claims/" + id;
    }

    @GetMapping("/reports")
    public String reportDashboard(Model model) {
        model.addAttribute("claims", expenseRepository.findAll());
        return "report-dashboard";
    }

    @GetMapping("/reports/download")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate end = to != null ? to : LocalDate.now();
        List<Expense> expenses = expenseRepository.findBySubmitDateBetween(start, end);
        ReportGenerator generator = reportStrategyFactory.get(format);
        byte[] body = generator.generateReport(expenses, start, end);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + generator.fileName() + "\"")
                .contentType(MediaType.parseMediaType(generator.contentType()))
                .body(body);
    }
}