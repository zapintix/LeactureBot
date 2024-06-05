package org.example.DB;

import java.util.List;

public interface UserDAO {
    boolean isUserRegistered(long userId);
    void registerUser(long userId, String role);
    void saveQuestion(long userId, int slideId, String questionText);
    void saveRating(long userId, int slideNumber, boolean rating);
    void saveAnswer(long userId, int questionId, String answer);
    String generateReport(int slideId);


}
