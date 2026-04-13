package com.pesu.expense.expense_claim_system.repository;

import com.pesu.expense.expense_claim_system.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop20ByOrderByTimestampDesc();
}
