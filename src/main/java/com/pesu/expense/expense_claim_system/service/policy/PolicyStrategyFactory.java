package com.pesu.expense.expense_claim_system.service.policy;

import com.pesu.expense.expense_claim_system.model.EntryCategory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PolicyStrategyFactory {

    private final Map<String, PolicyStrategy> strategies;

    public PolicyStrategyFactory(Map<String, PolicyStrategy> strategies) {
        this.strategies = strategies;
    }

    public PolicyStrategy getPolicy(EntryCategory category) {
        return strategies.get(category.name());
    }
}
