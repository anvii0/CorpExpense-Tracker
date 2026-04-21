# CorpExpense Tracker

CorpExpense Tracker is a Spring Boot web application for submitting, approving, and reimbursing employee expense claims.

It demonstrates core object-oriented design patterns in a practical workflow:
- Builder Pattern for creating expense claims
- Adapter Pattern for currency conversion to USD
- Chain of Responsibility for multi-level approval
- Strategy Pattern for reimbursement method selection
- Observer Pattern for status notifications
- Template Method Pattern for CSV report generation

## Tech Stack

- Java (JDK 21+ recommended)
- Spring Boot 4
- Spring Web + Thymeleaf
- Spring Data JPA
- H2 in-memory database
- Gradle Wrapper

## Features

- Submit expense claims with:
  - employee
  - title
  - original amount and currency
  - description
  - optional receipt upload (file name stored)
  - reimbursement method (Direct Deposit or Cheque)
- Automatic conversion to USD using fixed adapter rates
- Approval chain by USD value:
  - <= $1,000: Team Lead approval required
  - $1,001 - $5,000: Department Head approval required
  - > $5,000: CFO approval required
- Reimburse approved claims using selected payment strategy
- Dashboard to view all claims and actions
- CSV report export for all expenses
- Seeded sample users and department budgets at startup

## Project Structure

- `src/main/java/com/pesu/expense/expense_claim_system/controller` - MVC controllers
- `src/main/java/com/pesu/expense/expense_claim_system/model` - JPA entities
- `src/main/java/com/pesu/expense/expense_claim_system/repository` - Spring Data repositories
- `src/main/java/com/pesu/expense/expense_claim_system/service` - business logic and pattern implementations
- `src/main/resources/templates` - Thymeleaf views
- `src/main/resources/static/css` - UI styles

## How To Run

1. Clone the repository

```bash
git clone https://github.com/anvii0/CorpExpense-Tracker.git
cd CorpExpense-Tracker
```

2. Start the app with Gradle Wrapper

```bash
./gradlew bootRun
```

3. Open in browser

- App dashboard: http://localhost:8081/
- Submit claim page: http://localhost:8081/submit
- H2 console: http://localhost:8081/h2-console

H2 console connection values:
- JDBC URL: `jdbc:h2:mem:expensedb;DB_CLOSE_DELAY=-1`
- Username: `sa`
- Password: *(empty)*

## Useful Endpoints

- `GET /` - Dashboard with all expenses
- `GET /submit` - Claim submission form
- `POST /submit` - Create a new expense claim
- `POST /approve/{id}` - Trigger approval chain for an expense
- `POST /reimburse/{id}` - Reimburse an approved expense
- `GET /report` - Download CSV report

## Default Seed Data

At startup, the app seeds data when users table is empty:
- Alice (Employee) - Engineering
- Bob (Manager) - Engineering
- Department budgets for Engineering and Marketing

## Notes

- Database is in-memory (`create-drop`), so data resets each run.
- SQL logging is enabled in development configuration.
- Thymeleaf caching is disabled for easier local UI iteration.

## Run Tests

```bash
./gradlew test
```
