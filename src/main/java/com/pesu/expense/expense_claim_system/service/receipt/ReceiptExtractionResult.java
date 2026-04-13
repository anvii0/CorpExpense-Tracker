package com.pesu.expense.expense_claim_system.service.receipt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiptExtractionResult {
    private Double extractedAmount;
    private String merchant;
    private boolean manualEntryRequired;
}
