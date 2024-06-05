package org.example.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class UserDatabaseManager implements UserDAO {
    private final HikariDataSource dataSource;

    public UserDatabaseManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl( "jdbc:postgresql://localhost:5432/leacture");
        config.setUsername("postgres");
        config.setPassword("admin");
        this.dataSource = new HikariDataSource(config);
    }

    public boolean isUserRegistered(long userId) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE user_id = ?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void registerUser(long userId, String role) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (user_id, role) VALUES (?, ?)");
            stmt.setLong(1, userId);
            stmt.setString(2, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveQuestion(long userId, int slideId, String question_text) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO questions (user_id, slide_id, question_text) VALUES (?, ?, ?)");
            stmt.setLong(1, userId);
            stmt.setInt(2, slideId);
            stmt.setString(3, question_text);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRating(long userId, int slide_number, boolean rating) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO ratings (user_id, slide_number, rating) VALUES (?, ?, ?)");
            stmt.setLong(1, userId);
            stmt.setInt(2, slide_number);
            stmt.setBoolean(3, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAnswer(long userId, int slide_num, String answer) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO answers (user_id, slide_number, answer_text) VALUES (?, ?, ?)");
            stmt.setLong(1, userId);
            stmt.setInt(2, slide_num);
            stmt.setString(3, answer);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String generateReport(int slideId) {
        StringBuilder report = new StringBuilder();
        try (Connection conn = dataSource.getConnection()) {
            // Получаем информацию о слайде
            PreparedStatement slideStmt = conn.prepareStatement("SELECT slide_number, topic FROM slides WHERE slide_number = ?");
            slideStmt.setInt(1, slideId);
            ResultSet slideRs = slideStmt.executeQuery();
            report.append("Информация о слайде №").append(slideId).append(":\n");
            if (slideRs.next()) {
                report.append("Тема: ").append(slideRs.getString("topic")).append("\n");
            }

            // Получаем вопросы к слайду
            PreparedStatement questionStmt = conn.prepareStatement("SELECT q.id, q.question_text, u.username " +
                                                                       "FROM questions q JOIN users u ON q.user_id = u.user_id" +
                                                                        " WHERE q.slide_id = ?;");
            questionStmt.setInt(1, slideId);
            ResultSet questionRs = questionStmt.executeQuery();
            report.append("\nВопросы к слайду №").append(slideId).append(":\n");
            Map<Integer, String> questions = new HashMap<>();
            while (questionRs.next()) {
                int questionId = questionRs.getInt("id");
                String questionText = questionRs.getString("question_text");
                String userId = questionRs.getString("username");
                questions.put(questionId, questionText);
                report.append(userId).append(" - ").append(questionText).append("\n");
            }

            // Получаем ответы на вопросы
            report.append("\nОтветы:\n");
            PreparedStatement answerStmt = conn.prepareStatement(
                    "SELECT answer_text, u.username " +
                            "FROM answers a JOIN users u ON a.user_id = u.user_id " +
                            "WHERE a.slide_number = ?;"
            );
            answerStmt.setInt(1, slideId);
            ResultSet answerRs = answerStmt.executeQuery();
            while (answerRs.next()) {
                String answerText = answerRs.getString("answer_text");
                String username = answerRs.getString("username");
                report.append(username).append(" - ").append(answerText).append("\n");
            }



            // Получаем оценки
            PreparedStatement ratingStmt = conn.prepareStatement("SELECT rating, COUNT(*) FROM ratings WHERE slide_number = ? GROUP BY rating");
            ratingStmt.setInt(1, slideId);
            ResultSet ratingRs = ratingStmt.executeQuery();
            report.append("\nОценки:\n");
            while (ratingRs.next()) {
                report.append(ratingRs.getBoolean("rating") ? "Понятно: " : "Непонятно: ")
                        .append(ratingRs.getInt("count")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка генерации отчёта.";
        }
        return report.toString();
    }



}
