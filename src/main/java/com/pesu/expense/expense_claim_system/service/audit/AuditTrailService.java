package com.pesu.expense.expense_claim_system.service.audit;

import com.pesu.expense.expense_claim_system.model.AuditLog;
import com.pesu.expense.expense_claim_system.model.Expense;
import com.pesu.expense.expense_claim_system.model.User;
import com.pesu.expense.expense_claim_system.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditTrailService {

    private final AuditLogRepository auditLogRepository;

    public AuditTrailService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User user, String action, Expense claim, String details) {
        AuditLog log = new AuditLog(
                null,
                user != null ? user.getId() : null,
                user != null ? user.getName() : "System",
                action,
                claim != null ? claim.getId() : null,
                details,
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }

    public List<AuditLog> latestLogs() {
        return auditLogRepository.findTop20ByOrderByTimestampDesc();
    }
}
