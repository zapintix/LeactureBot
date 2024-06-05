package org.example.bot;

import org.example.DB.AdminDatabaseManager;
import org.example.DB.UserDatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Admin extends User {

    private final LectureBot lectureBot;

    public Admin(long id, LectureBot lectureBot) {
        super(lectureBot, id);
        this.lectureBot = lectureBot;
    }

    public void addAdmin(AdminDatabaseManager dbManager, String messageText, long chatId) {
        String[] parts = messageText.split("\\s+");
        if (parts.length != 2) {
            sendMessage(chatId, "Неправильный формат команды. Используйте: /addAdmin <user_id>");
        } else {
            long newAdminId = Long.parseLong(parts[1]);
            dbManager.addUser(newAdminId, "admin");
            sendMessage(chatId, "Пользователь " + newAdminId + " теперь администратор.");
        }
    }

    public void createSlide(AdminDatabaseManager dbManager, String messageText, long chatId){
        String[] parts = messageText.split("\\s+", 3);
        if (parts.length < 3) {
            sendMessage(chatId, "Неправильный формат команды. Используйте: /NewPost <номер_слайда> <тема>");
        } else {
            int slideNumber = Integer.parseInt(parts[1]);
            String topic = parts[2];
            try {
                dbManager.createSlide(slideNumber, topic);
                sendMessage(chatId, "Слайд опубликован: №" + slideNumber + " - " + topic);
                broadcastMessageToAllUsers(dbManager, "Новый слайд опубликован: №" +slideNumber + " - "+ topic);
            }catch (SQLException e) {
                if (e.getMessage().contains("Слайд с номером")) {
                    sendMessage(chatId, e.getMessage());
                } else {
                    sendMessage(chatId, "Ошибка при создании слайда. Пожалуйста, попробуйте снова.");
                }
            }
        }
    }

    private void broadcastMessageToAllUsers(AdminDatabaseManager dbManager, String messageText) {
        List<Long> userIds = dbManager.getAllUserIds();
        for (Long userId : userIds) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(userId));
            message.setText(messageText);
            try{
                lectureBot.execute(message);
            }catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
    }
    public void showSlidesForReport(AdminDatabaseManager adminDatabaseManager, long chatId) {
        List<Integer> slideIds = adminDatabaseManager.getAllSlideIds();
        if (slideIds.isEmpty()) {
            sendMessage(chatId, "Нет доступных слайдов.");
            return;
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите слайд для получения отчета:");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int slideId : slideIds) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Слайд " + slideId);
            button.setCallbackData("report_" + slideId);
            row.add(button);
            buttons.add(row);
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(keyboardMarkup);

        try {
            lectureBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void generateReport(UserDatabaseManager dbManager, long chatId, String messageText) {
        String[] parts = messageText.split("\\s+");
        if (parts.length == 2) {
            int slideId = Integer.parseInt(parts[1]);
            String report = dbManager.generateReport(slideId);
            sendMessage(chatId, report);
        } else {
            sendMessage(chatId, "Введите номер слайда для получения отчета: /report <номер_слайда>");
        }
    }

    public void sendMostPopularPost(AdminDatabaseManager adminDatabaseManager, long chatId){
        String mostPopularPost = adminDatabaseManager.getMostPopularSlide();
        sendMessage(chatId, mostPopularPost);
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




