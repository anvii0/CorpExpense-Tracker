package com.pesu.expense.expense_claim_system.repository;

import com.pesu.expense.expense_claim_system.model.ExpenseEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, Long> {
    List<ExpenseEntry> findByClaimId(Long claimId);
}
