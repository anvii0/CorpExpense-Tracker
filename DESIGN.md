Design & Architecture — CorpExpense Tracker

Overview

This document summarizes the architecture, design principles, and where key design patterns are implemented in the codebase.

Is this an MVC app?

Yes — the project follows a classic MVC structure implemented with Spring MVC + Thymeleaf:
- Controller (web layer): `src/main/java/com/pesu/expense/expense_claim_system/controller` — handles HTTP requests, session, and view model population.
- Model (domain/entities): `src/main/java/com/pesu/expense/expense_claim_system/model` — JPA entities such as `Expense`, `ExpenseEntry`, `User`, `DepartmentBudget`, `ExpenseStatus`.
- View (presentation): `src/main/resources/templates` — Thymeleaf templates (dashboard, claim-form, claim-detail, reports).

Other layers:
- Service layer: `src/main/java/com/pesu/expense/expense_claim_system/service` — business logic, policy checks, currency conversion, approval chain, budget handling, payment processing.
- Repository layer: `src/main/java/com/pesu/expense/expense_claim_system/repository` — Spring Data JPA interfaces.

Design Principles Used

- Separation of Concerns: Controllers handle web logic; services handle business rules; repositories handle persistence; templates handle UI.
- Single Responsibility: Classes have focused responsibilities (e.g., `BudgetService` manages budgets only).
- Dependency Injection / Inversion of Control: Spring `@Service`, `@Component`, and constructor injection are used across services and controllers.
- Open/Closed: Strategy and factory classes allow adding new policies or report formats without modifying existing classes.
- Tell, Don't Ask: Services perform logic (approve, reimburse) rather than exposing raw internals to controllers.

Design Patterns & Where They Are Used

- Builder Pattern
  - Purpose: Construct complex `Expense` draft objects in a clear way.
  - Location: `Expense` entity (uses Lombok `@Builder`), created in `ExpenseClaimService.createDraft(...)`.

- Adapter Pattern
  - Purpose: Convert amounts from various currencies to USD via a single interface.
  - Location: `CurrencyService` and `CurrencyAdapter` implementations in `service/currency`.

- Chain of Responsibility
  - Purpose: Multi-level approval chain (team lead → dept head → CFO) where responsibility passes to next handler when needed.
  - Location: `service/approval` package:
    - `ApprovalService` builds the chain (wires `TeamLeadApprovalHandler`, `DepartmentHeadApprovalHandler`, `CfoApprovalHandler`).
    - Handlers extend `ApprovalHandler` and implement `approve(...)` and `supports(...)`.

- Strategy Pattern
  - Purpose: Select reimbursement method (Direct Deposit, Cheque) and policy checks per entry category.
  - Location:
    - Payment strategies: `service/payment` (implementations used by `PaymentService`).
    - Policy strategies: `service/policy` and `PolicyStrategyFactory` mapping `EntryCategory` → `PolicyStrategy` implementations (`TravelPolicy`, `MealsPolicy`, `OfficeSuppliesPolicy`, `SoftwareSubscriptionPolicy`).

- Observer Pattern (event/notification-like)
  - Purpose: Notify users / audit trail when status changes.
  - Location: `NotificationService` and `AuditTrailService` are used across services to record and notify state changes.

- Template Method
  - Purpose: Share common report-generation flow while allowing different output formats (CSV, Excel, PDF).
  - Location: `service/report` package (e.g., `ReportGenerator` base + `CsvReportGenerator`, `ExcelReportGenerator`, `PdfReportGenerator`).

Key Flows and Relevant Files

- Create claim (controller → service):
  - `ExpenseController.createClaim(...)` → `ExpenseClaimService.createDraft(...)` → `ExpenseRepository`.
- Add entry and policy check:
  - `ExpenseController.addEntry(...)` → `ExpenseClaimService.addEntry(...)` → `PolicyStrategyFactory.getPolicy(...)` → sets `policyCompliant` on `ExpenseEntry`.
- Submit & approval chain:
  - `ExpenseController.submitClaim(...)` → `ExpenseClaimService.submitClaim(...)` → `ApprovalService.submitForApproval(...)` (sets initial pending status based on amount).
  - Approvals: `ExpenseController.approveClaim(...)` → `ExpenseClaimService.approveClaim(...)` → `ApprovalService.processApproval(...)` which routes to the appropriate handler.
- Budgeting & Reimbursement:
  - `BudgetService` exposes `checkAvailability(...)`, `deductBudget(...)`, `allBudgets()`.
  - `ExpenseClaimService.reimburseClaim(...)` performs per-department aggregation and deductions before calling `PaymentService.processPayment(...)`.

Where to look to change behavior

- Policy thresholds: change implementations in `service/policy/*Policy.java` (e.g., `TravelPolicy.isValid(...)`).
- Approval thresholds/roles: see `service/approval/*Handler.java` and `ApprovalService.submitForApproval(...)`.
- Budget amounts/seed data: see `DataSeeder.java` and `model/DepartmentBudget`.
- UI templates: `src/main/resources/templates/*` (Thymeleaf files). Dashboard shows budgets by reading `BudgetService.allBudgets()`.

Notes & Recommendations

- The app is organized with clear MVC separation — controllers handle HTTP + view data, services handle business logic, repositories handle persistence.
- For production, separate the approval and payment flows more strictly (we currently deduct budgets when an approved claim reaches `FULLY_APPROVED`, and reimbursement is explicit via the `reimburse` action).
- Add unit tests around `ApprovalService`, `BudgetService`, and `ExpenseClaimService.reimburseClaim(...)` to protect budget logic.

Files worth opening now

- Controller layer: `src/main/java/com/pesu/expense/expense_claim_system/controller/ExpenseController.java`
- Approval chain: `src/main/java/com/pesu/expense/expense_claim_system/service/approval/ApprovalService.java` and the handler classes
- Policy strategies: `src/main/java/com/pesu/expense/expense_claim_system/service/policy`
- Budget service & model: `src/main/java/com/pesu/expense/expense_claim_system/service/budget/BudgetService.java` and `src/main/java/com/pesu/expense/expense_claim_system/model/DepartmentBudget.java`

If you'd like, I can:
- Add this as a `Design` section into the main `README.md` instead of a separate file (your call),
- Generate a simple diagram (ASCII or Mermaid) showing MVC layers and approval flow.

---
Generated by assistant — file: [DESIGN.md](DESIGN.md)
