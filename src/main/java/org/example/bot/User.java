package org.example.bot;
import org.example.DB.UserDatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class User {

    private final LectureBot lectureBot;
    private final long id;

    public User(LectureBot lectureBot, long id) {
        this.lectureBot = lectureBot;
        this.id = id;
    }

    public void handleMessage(String messageText, long chatId, long userId, UserDatabaseManager dbManager) {
        if (messageText.matches("^\\d+\\s+Ответ:.+$")) {
            processAnswer(messageText, chatId, userId, dbManager);
        } else if (messageText.matches("^\\d+\\s+(понятно|непонятно)$")) {
            processRating(messageText, chatId, userId, dbManager);
        } else if (messageText.matches("^\\d+\\s+.+$")) {
            processQuestion(messageText, chatId, userId, dbManager);
        } else {
            sendMessage(chatId, "Неправильный формат сообщения.");
        }
    }

    public void processRating(String ratingText, long chatId, long userId, UserDatabaseManager dbManager) {
        // Разбиваем текст сообщения на части
        String[] parts = ratingText.split("\\s+");
        if (parts.length != 2) {
            sendMessage(chatId, "Неправильный формат. Используйте: <номер слайда> <понятно|непонятно>");
            return;
        }
        int slideNumber;
        boolean isUnderstandable;
        try {
            slideNumber = Integer.parseInt(parts[0]);
            isUnderstandable = parts[1].equalsIgnoreCase("понятно");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неправильный формат номера слайда.");
            return;
        }
        dbManager.saveRating(userId, slideNumber, isUnderstandable);
        sendMessage(chatId, "Ваша оценка сохранена.");
    }


    private void processQuestion(String questionText, long chatId, long userId, UserDatabaseManager userDatabaseManager) {
        String[] parts = questionText.split("\\s", 2);
        if (parts.length < 2) {
            sendMessage(chatId, "Неправильный формат. Используйте: <номер слайда> <вопрос>");
            return;
        }
        try {
            int slideId = Integer.parseInt(parts[0].trim());
            String question = parts[1].trim();
            userDatabaseManager.saveQuestion(userId,slideId, question);
            sendMessage(chatId, "Ваш вопрос сохранен.");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неправильный формат номера слайда.");
        }
    }


    public void processAnswer(String answerText, long chatId, long userId, UserDatabaseManager userDatabaseManager) {
        // Обработка ответа
        String[] parts = answerText.split("\\s+", 2);
        if (parts.length < 2) {
            sendMessage(chatId, "Неправильный формат. Используйте:<id вопроса> Ответ: <ответ>");
            return;
        }
        try {
            int questionId = Integer.parseInt(parts[0].trim());
            String answer = parts[1].trim();
            userDatabaseManager.saveAnswer(userId, questionId, answer);
            sendMessage(chatId, "Ваш ответ сохранен.");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неправильный формат номера слайда или ID вопроса.");
        }
    }
    public void info(long chatId){
        sendMessage(chatId, "Команды пользователя:\n");
        sendMessage(chatId, "Написать вопрос:\n" +
                "<номер поста> <текст вопроса>\n" +
                "\nОставить реакцию:\n" +
                "<номер поста> <понятно|непонятно>\n"+
                "\nНаписать ответ:\n" +
                "<id вопроса> Ответ:<Текст ответа>");

    }


    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            lectureBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}