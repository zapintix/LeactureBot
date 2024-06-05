package org.example.bot;
import org.example.DB.AdminDatabaseManager;
import org.example.DB.UserDatabaseManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.HashMap;

public class LectureBot extends TelegramLongPollingBot {
    private final UserDatabaseManager userDatabaseManager = new UserDatabaseManager();
    private final AdminDatabaseManager adminDatabaseManager = new AdminDatabaseManager();
    private final KeyboardBuilder keyboardBuilder = new KeyboardBuilder();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            if (messageText.startsWith("/")) {
                try {
                    handleCommand(messageText, chatId, userId, update);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                User user = new User(this, userId);
                user.handleMessage(messageText, chatId, userId, userDatabaseManager);
            }
        }else if (update.hasCallbackQuery()){
            handleCallback(update);
        }
    }

    private void handleCommand(String messageText, long chatId, long userId, Update update) throws SQLException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        String HelloName = update.getMessage().getFrom().getFirstName();
        if ("/start".equals(messageText)) {
            sendMessage.setText("Добро пожаловать, "+HelloName+ "! Выберите команду.");
            if(adminDatabaseManager.isAdmin(userId)) {
                sendMessage.setReplyMarkup(keyboardBuilder.getMainAdminKeyboard()); // Используем объект keyboardBuilder для получения клавиатуры
                sendMessage(sendMessage); // Передаем объект SendMessage
            }else {
                sendMessage.setReplyMarkup(keyboardBuilder.getMainUserKeyboard()); // Используем объект keyboardBuilder для получения клавиатуры
                sendMessage(sendMessage); // Передаем объект SendMessage
            }
        }else if("/register".equals(messageText)){
            if(userDatabaseManager.isUserRegistered(userId)){
                sendMessage.setText("Ошибка. Вы уже зарегистрированы");
                sendMessage(sendMessage);
            }else {
                userDatabaseManager.registerUser(userId, "user");
                sendMessage.setText("Вы успешно зарегистрированы");
                sendMessage(sendMessage);
            }

        } else if (messageText.startsWith("/report")) {
            if(adminDatabaseManager.isAdmin(userId)) {
                Admin admin = new Admin(userId, this);
                admin.showSlidesForReport(adminDatabaseManager, chatId);
            }else {
                sendMessage.setText("У вас нет прав на просмотр данной информации.");
                sendMessage(sendMessage);
            }
        } else if (messageText.startsWith("/NewPost")) {
            if (adminDatabaseManager.isAdmin(userId)) {
                Admin admin = new Admin(userId, this);
                admin.createSlide(adminDatabaseManager, messageText, chatId);
            } else {
                sendMessage.setText("У вас нет прав для публикации слайдов.");
                sendMessage(sendMessage);
            }
        } else if (messageText.startsWith("/addAdmin")) {
            if (adminDatabaseManager.isAdmin(userId)) {
                Admin admin = new Admin(userId, this);
                admin.addAdmin(adminDatabaseManager, messageText, chatId);
            } else {
                sendMessage.setText("У вас нет прав для добавления администраторов.");
                sendMessage(sendMessage);
            }
        }else if(messageText.startsWith("/most_popular_post")) {
            if (adminDatabaseManager.isAdmin(userId)) {
                Admin admin = new Admin(userId, this);
                admin.sendMostPopularPost(adminDatabaseManager, chatId);
            } else {
                sendMessage.setText("У вас нет прав для публикации слайдов.");
                sendMessage(sendMessage);
            }
        }else if(messageText.startsWith("/info")) {
            User user = new User(this, userId);
            user.info(chatId);


        } else {
            sendMessage.setText("Неизвестная команда.");
            sendMessage(sendMessage);
        }
    }



    private void handleCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long userId = update.getCallbackQuery().getFrom().getId();

        if (callbackData.startsWith("report_")) {
            int slideId = Integer.parseInt(callbackData.split("_")[1]);
            Admin admin = new Admin(userId, this);
            admin.generateReport(userDatabaseManager, chatId, "/report " + slideId);
        }
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }




    @Override
    public String getBotUsername() {
        return "LeactureBot_bot";
    }

    @Override
    public String getBotToken() {
        return "7194500025:AAEKlmIjjhZ9ynYMaCFa_gZXUG292pByeTk";
    }
}