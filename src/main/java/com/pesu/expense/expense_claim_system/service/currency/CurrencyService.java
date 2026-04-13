package com.pesu.expense.expense_claim_system.service.currency;

import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private final CurrencyAdapter currencyAdapter;

    public CurrencyService(CurrencyAdapter currencyAdapter) {
        this.currencyAdapter = currencyAdapter;
    }

    public double convert(double amount, String fromCurrency, String toCurrency) {
        double usdAmount = currencyAdapter.convertToUSD(amount, fromCurrency);
        if ("USD".equalsIgnoreCase(toCurrency)) {
            return usdAmount;
        }
        return currencyAdapter.convertFromUsd(usdAmount, toCurrency);
    }

    public double convertToUsd(double amount, String fromCurrency) {
        return currencyAdapter.convertToUSD(amount, fromCurrency);
    }
}
