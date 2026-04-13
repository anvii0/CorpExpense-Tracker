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
                userRepository.save(new User(null, "Aarav Employee", "employee@corp.com", "EMPLOYEE", "Engineering"));
                userRepository.save(new User(null, "Priya Team Lead", "teamlead@corp.com", "TEAM_LEAD", "Engineering"));
                userRepository.save(new User(null, "Neha Dept Head", "depthead@corp.com", "DEPT_HEAD", "Engineering"));
                userRepository.save(new User(null, "Raj CFO", "cfo@corp.com", "CFO", "Finance"));
                userRepository.save(new User(null, "Maya Finance", "finance@corp.com", "FINANCE", "Finance"));

                budgetRepo.save(new DepartmentBudget(null, "Engineering", 50000.0, 5000.0));
                budgetRepo.save(new DepartmentBudget(null, "Marketing", 20000.0, 2000.0));
                budgetRepo.save(new DepartmentBudget(null, "Finance", 75000.0, 10000.0));

                System.out.println("PRELOADED DATABASE WITH DEFAULT USERS AND BUDGETS!");
            }
        };
    }
}
