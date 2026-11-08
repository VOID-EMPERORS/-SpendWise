package com.spendwise;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class FinanceRepository {
    public record Summary(BigDecimal income, BigDecimal expenses) {
        public BigDecimal balance() { return income.subtract(expenses); }
    }
    public record Transaction(long id, LocalDate date, String type, String category,
                              BigDecimal amount, String description) {}
    public record Budget(String category, BigDecimal limit, BigDecimal spent) {
        public BigDecimal remaining() { return limit.subtract(spent); }
    }

    public int register(String name, String email, String password) throws Exception {
        String sql = "INSERT INTO users(full_name,email,password_hash) VALUES(?,?,?)";
        try (Connection c=Database.getConnection();
             PreparedStatement p=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1,name); p.setString(2,email); p.setString(3,PasswordUtil.hash(password));
            p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getInt(1);}
        }
    }

    public User login(String email, String password) throws Exception {
        try(Connection c=Database.getConnection();
            PreparedStatement p=c.prepareStatement("SELECT id,full_name,email,password_hash FROM users WHERE email=?")) {
            p.setString(1,email);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next() || !PasswordUtil.verify(password,r.getString("password_hash"))) return null;
                return new User(r.getInt("id"),r.getString("full_name"),r.getString("email"));
            }
        }
    }
    public record User(int id, String name, String email) {}

    public Summary summary(int userId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(CASE WHEN transaction_type='INCOME' THEN amount ELSE 0 END),0),
                   COALESCE(SUM(CASE WHEN transaction_type='EXPENSE' THEN amount ELSE 0 END),0)
            FROM transactions WHERE user_id=?""";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,userId);try(ResultSet r=p.executeQuery()){r.next();return new Summary(r.getBigDecimal(1),r.getBigDecimal(2));}
        }
    }

    public List<Transaction> transactions(int userId, String search) throws SQLException {
        List<Transaction> list=new ArrayList<>();
        String sql="""
            SELECT id,transaction_date,transaction_type,category,amount,description
            FROM transactions WHERE user_id=? AND
            (category LIKE ? OR COALESCE(description,'') LIKE ? OR transaction_type LIKE ?)
            ORDER BY transaction_date DESC,id DESC""";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            String q="%"+(search==null?"":search.trim())+"%";
            p.setInt(1,userId);p.setString(2,q);p.setString(3,q);p.setString(4,q);
            try(ResultSet r=p.executeQuery()){while(r.next())list.add(new Transaction(
                r.getLong(1),r.getDate(2).toLocalDate(),r.getString(3),r.getString(4),
                r.getBigDecimal(5),r.getString(6)));}
        }
        return list;
    }

    public void addTransaction(int userId,String type,String category,BigDecimal amount,
                               LocalDate date,String description) throws SQLException {
        String sql="INSERT INTO transactions(user_id,transaction_type,category,amount,transaction_date,description) VALUES(?,?,?,?,?,?)";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,userId);p.setString(2,type);p.setString(3,category);p.setBigDecimal(4,amount);
            p.setDate(5,java.sql.Date.valueOf(date));p.setString(6,description);p.executeUpdate();
        }
    }

    public void deleteTransaction(int userId,long transactionId) throws SQLException {
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(
            "DELETE FROM transactions WHERE id=? AND user_id=?")) {
            p.setLong(1,transactionId);p.setInt(2,userId);p.executeUpdate();
        }
    }

    public void saveBudget(int userId,String category,YearMonth month,BigDecimal limit) throws SQLException {
        String sql = """
            INSERT INTO budgets(user_id,category,budget_month,limit_amount) VALUES(?,?,?,?)
            ON DUPLICATE KEY UPDATE limit_amount=VALUES(limit_amount)
            """;
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,userId);p.setString(2,category);p.setString(3,month.toString());p.setBigDecimal(4,limit);p.executeUpdate();
        }
    }

    public List<Budget> budgets(int userId,YearMonth month) throws SQLException {
        List<Budget> list=new ArrayList<>();
        String sql="""
            SELECT b.category,b.limit_amount,COALESCE(SUM(t.amount),0)
            FROM budgets b LEFT JOIN transactions t
              ON t.user_id=b.user_id AND t.category=b.category AND t.transaction_type='EXPENSE'
             AND DATE_FORMAT(t.transaction_date,'%Y-%m')=b.budget_month
            WHERE b.user_id=? AND b.budget_month=?
            GROUP BY b.id,b.category,b.limit_amount ORDER BY b.category""";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,userId);p.setString(2,month.toString());
            try(ResultSet r=p.executeQuery()){while(r.next())list.add(new Budget(r.getString(1),r.getBigDecimal(2),r.getBigDecimal(3)));}
        }
        return list;
    }

    public Map<String,BigDecimal> expenseByCategory(int userId) throws SQLException {
        Map<String,BigDecimal> map=new LinkedHashMap<>();
        String sql="SELECT category,SUM(amount) FROM transactions WHERE user_id=? AND transaction_type='EXPENSE' AND transaction_date>=DATE_SUB(CURDATE(),INTERVAL 30 DAY) GROUP BY category ORDER BY SUM(amount) DESC";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())map.put(r.getString(1),r.getBigDecimal(2));}
        }
        return map;
    }
}
