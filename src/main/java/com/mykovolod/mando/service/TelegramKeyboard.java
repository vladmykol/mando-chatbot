package com.mykovolod.mando.service;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TelegramKeyboard {

    KeyboardRow row = new KeyboardRow();
    List<KeyboardRow> rowList = new ArrayList<>();

    public TelegramKeyboard() {
        addRow();
    }

    public static ReplyKeyboardRemove removeKeyBoard() {
        return new ReplyKeyboardRemove(true);
    }

    public TelegramKeyboard addButton(String buttonText) {
        row.add(buttonText);
        return this;
    }

    public TelegramKeyboard addRow() {
        row = new KeyboardRow();
        rowList.add(row);
        return this;
    }

    public ReplyKeyboardMarkup getMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setKeyboard(rowList);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

}
