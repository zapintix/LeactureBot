package org.example.DB;

import java.sql.SQLException;
import java.util.List;

public interface AdminDAO {
    boolean isAdmin(long userId);
    void addUser(long userId, String role);
    String getMostPopularSlide();
    List<Long> getAllUserIds();
    List<Integer> getAllSlideIds();
    void createSlide(int slideNumber, String topic) throws SQLException;
}
