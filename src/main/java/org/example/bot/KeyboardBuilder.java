package org.example.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardBuilder {

    public ReplyKeyboardMarkup getMainAdminKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/NewPost"));
        row1.add(new KeyboardButton("/addAdmin"));
        keyboard.add(row1);
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("/most_popular_post"));
        keyboard.add(row3);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/report"));
        keyboard.add(row2);


        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainUserKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/register"));;
        keyboard.add(row1);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/info"));
        keyboard.add(row2);


        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
}
