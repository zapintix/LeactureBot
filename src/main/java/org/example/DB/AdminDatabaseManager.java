package org.example.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class AdminDatabaseManager implements AdminDAO{
    private final HikariDataSource dataSource;

    public AdminDatabaseManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl( "jdbc:postgresql://localhost:5432/leacture");
        config.setUsername("postgres");
        config.setPassword("admin");
        this.dataSource = new HikariDataSource(config);
    }

    public boolean isAdmin(long userId) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE user_id = ?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "admin".equals(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUser(long userId, String role) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (user_id, role) VALUES (?, ?)");
            stmt.setLong(1, userId);
            stmt.setString(2, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Long> getAllUserIds() {
        List<Long> userIds = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users where role = 'user'");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                userIds.add(rs.getLong("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }

    public void createSlide(int slideNumber, String topic) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO slides (slide_number, topic) VALUES (?, ?)");
            stmt.setInt(1, slideNumber);
            stmt.setString(2, topic);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // SQL state for unique violation
                throw new SQLException("Слайд с номером " + slideNumber + " уже существует.", e);
            } else {
                throw e;
            }
        }
    }

    public List<Integer> getAllSlideIds() {
        List<Integer> slideNumbers = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT slide_number, topic FROM slides")) {
            while (resultSet.next()) {
                slideNumbers.add(resultSet.getInt("slide_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return slideNumbers;
    }


    public String getMostPopularSlide() {
        String mostPopularSlide = "";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT s.slide_number, s.topic,\n" +
                             "       COALESCE(q.question_count, 0) AS question_count,\n" +
                             "       COALESCE(a.answer_count, 0) AS answer_count,\n" +
                             "       COALESCE(r.ratings_count, 0) AS ratings_count,\n" +
                             "       (COALESCE(q.question_count, 0) + COALESCE(a.answer_count, 0) + COALESCE(r.ratings_count, 0)) AS total_interactions\n" +
                             "FROM slides s\n" +
                             "LEFT JOIN (\n" +
                             "    SELECT slide_id, COUNT(*) AS question_count\n" +
                             "    FROM questions\n" +
                             "    GROUP BY slide_id\n" +
                             ") q ON s.slide_number = q.slide_id\n" +
                             "LEFT JOIN (\n" +
                             "    SELECT q.slide_id, COUNT(DISTINCT a.id) AS answer_count\n" +
                             "    FROM questions q\n" +
                             "    JOIN answers a ON q.slide_id = a.slide_number\n" +
                             "    GROUP BY q.slide_id\n" +
                             ") a ON s.slide_number = a.slide_id\n" +
                             "LEFT JOIN (\n" +
                             "    SELECT slide_number, COUNT(*) AS ratings_count\n" +
                             "    FROM ratings\n" +
                             "    GROUP BY slide_number\n" +
                             ") r ON s.slide_number = r.slide_number\n" +
                             "ORDER BY total_interactions DESC\n" +
                             "LIMIT 1\n")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int slideNumber = resultSet.getInt("slide_number");
                String topic = resultSet.getString("topic");
                int questionCount = resultSet.getInt("question_count");
                int answerCount = resultSet.getInt("answer_count");
                int ratingsCount = resultSet.getInt("ratings_count");
                int totalInteractions = resultSet.getInt("total_interactions");
                mostPopularSlide = "Номер слайда: " + slideNumber + "\n" +
                        "Тема: " + topic + "\n" +
                        "Количество вопросов: " + questionCount + "\n" +
                        "Количество ответов: " + answerCount + "\n" +
                        "Количество реакций: " + ratingsCount + "\n" +
                        "Общее количество взаимодействий: " + totalInteractions;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mostPopularSlide;
    }


}
