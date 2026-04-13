package com.pesu.expense.expense_claim_system;

import com.pesu.expense.expense_claim_system.model.DepartmentBudget;
import com.pesu.expense.expense_claim_system.model.User;
import com.pesu.expense.expense_claim_system.repository.DepartmentBudgetRepository;
import com.pesu.expense.expense_claim_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, DepartmentBudgetRepository budgetRepo) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User(null, "Alice (Employee)", "alice@corp.com", "EMPLOYEE", "Engineering"));
                userRepository.save(new User(null, "Bob (Manager)", "bob@corp.com", "MANAGER", "Engineering"));
                
                budgetRepo.save(new DepartmentBudget(null, "Engineering", 50000.0, 0.0));
                budgetRepo.save(new DepartmentBudget(null, "Marketing", 20000.0, 0.0));
                
                System.out.println("PRELOADED DATABASE WITH DEFAULT USERS AND BUDGETS!");
            }
        };
    }
}
