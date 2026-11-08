package com.spendwise;

import java.sql.*;

public final class Database {
    private Database() {}

    private static final String URL = System.getenv().getOrDefault(
        "SPENDWISE_DB_URL",
        "jdbc:mysql://localhost:3306/spendwise?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata");
    private static final String USER = System.getenv().getOrDefault("SPENDWISE_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("SPENDWISE_DB_PASSWORD", "root");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initialize() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  full_name VARCHAR(100) NOT NULL,
                  email VARCHAR(150) NOT NULL UNIQUE,
                  password_hash VARCHAR(512) NOT NULL,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  user_id INT NOT NULL,
                  transaction_type ENUM('INCOME','EXPENSE') NOT NULL,
                  category VARCHAR(60) NOT NULL,
                  amount DECIMAL(12,2) NOT NULL,
                  transaction_date DATE NOT NULL,
                  description VARCHAR(255),
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_transactions_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                  INDEX idx_transactions_user_date(user_id, transaction_date)
                )""");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS budgets (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  user_id INT NOT NULL,
                  category VARCHAR(60) NOT NULL,
                  budget_month CHAR(7) NOT NULL,
                  limit_amount DECIMAL(12,2) NOT NULL,
                  UNIQUE KEY uq_user_category_month(user_id, category, budget_month),
                  CONSTRAINT fk_budgets_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )""");
        }
    }
}
