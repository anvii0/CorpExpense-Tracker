package com.pesu.expense.expense_claim_system.service.currency;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

// Owner: Student 2 (Minor) - Adapter Pattern (Adapts external logic to our system's double format logic)
@Service
public class CurrencyAdapter {
    
    // Simulates an external 3rd party API map
    private static final Map<String, Double> externalFixedRatesToUSD = new HashMap<>();
    static {
        externalFixedRatesToUSD.put("EUR", 1.08);
        externalFixedRatesToUSD.put("GBP", 1.25);
        externalFixedRatesToUSD.put("INR", 0.012);
        externalFixedRatesToUSD.put("JPY", 0.0066);
        externalFixedRatesToUSD.put("USD", 1.0);
    }

    public Double convertToUSD(Double amount, String currencyCode) {
        Double rate = externalFixedRatesToUSD.getOrDefault(currencyCode.toUpperCase(), 1.0);
        return amount * rate;
    }
}
