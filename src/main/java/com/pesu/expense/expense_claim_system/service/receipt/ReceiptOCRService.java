package com.pesu.expense.expense_claim_system.service.receipt;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReceiptOCRService {

    public ReceiptExtractionResult extractData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ReceiptExtractionResult(null, "No receipt", true);
        }

        String filename = file.getOriginalFilename() == null ? "receipt" : file.getOriginalFilename().toLowerCase();
        double extractedAmount = 25.0 + (filename.length() * 3.5);
        String merchant = filename.contains("hotel") ? "Hotel Vendor"
                : filename.contains("flight") ? "Travel Desk"
                : filename.contains("meal") ? "Restaurant"
                : "Scanned Merchant";
        return new ReceiptExtractionResult(extractedAmount, merchant, false);
    }

    public boolean verifyAmountMatches(Double extractedAmount, Double userAmount) {
        if (extractedAmount == null || userAmount == null) {
            return false;
        }
        double tolerance = Math.max(2.0, userAmount * 0.1);
        return Math.abs(extractedAmount - userAmount) <= tolerance;
    }
}
