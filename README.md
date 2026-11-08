# SpendWise – Personal Expense Tracker (Java)

SpendWise is a Java desktop application for tracking personal income and expenses. It uses Java Swing for the graphical user interface, JDBC for database connectivity, and MySQL for persistent storage.

## Features

- User registration and login
- Password hashing with PBKDF2
- Dashboard with income, expenses and balance summary
- Add and search income/expense transactions
- Delete selected transactions
- Category-wise monthly budgets
- Budget usage and remaining amount
- Expense analytics with a category bar chart
- MySQL persistence through JDBC

## Tech stack

- Java 17+
- Java Swing
- MySQL 8+
- JDBC / MySQL Connector/J
- Maven

## Project structure

```text
SpendWise-Java-Project/
├── database/
│   └── spendwise.sql
├── src/main/java/com/spendwise/
│   ├── Main.java
│   ├── Database.java
│   ├── FinanceRepository.java
│   └── PasswordUtil.java
├── pom.xml
└── README.md
```

## Requirements

1. Install JDK 17 or later.
2. Install MySQL Server 8 or later.
3. Install Maven.
4. Use IntelliJ IDEA, Eclipse, or another Java IDE.

## Database setup

1. Start MySQL Server.
2. Open MySQL Workbench or the MySQL command line.
3. Run the SQL file `database/spendwise.sql`.
4. The application also creates its tables on startup if they do not exist.

Default local connection settings in `Database.java`:

- Database: `spendwise`
- Username: `root`
- Password: `root`

**Change the password to match your local MySQL installation.** For safer configuration, set environment variables instead:

```bash
SPENDWISE_DB_URL=jdbc:mysql://localhost:3306/spendwise?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
SPENDWISE_DB_USER=root
SPENDWISE_DB_PASSWORD=your_mysql_password
```

Do not commit real database passwords or credentials to GitHub.

## Run the project

From the project root:

```bash
mvn clean compile
mvn exec:java
```

Or open the folder as a Maven project in IntelliJ IDEA / Eclipse and run:

`com.spendwise.Main`

## How to use

1. Register with your name, email and password.
2. Sign in to open the dashboard.
3. Select **Transactions** to add income or expenses.
4. Select **Budgets** to set monthly category limits.
5. Select **Reports** to view the recent category-wise expense chart.

## Important notes

- This repository is a Java Swing desktop implementation of the SpendWise personal-finance feature set.
- The supplied hosted website was not available as readable page content during project preparation. Therefore, this code implements the features described for SpendWise, but it is not guaranteed to be a pixel-perfect copy of that website.
- The app is intended for learning and demonstration. Before production use, add stronger account recovery, database migration management, automated tests, and deployment-specific security configuration.

## Future improvements

- Edit transactions
- Export reports to PDF or CSV
- Date-range and monthly reports
- Recurring transactions
- Notifications when a budget is exceeded
- Backup and restore

## License

Add a license file if you intend to publish this project for reuse.
