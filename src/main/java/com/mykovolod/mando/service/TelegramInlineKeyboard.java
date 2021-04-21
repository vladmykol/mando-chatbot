package com.mykovolod.mando.service;

import com.mykovolod.mando.utils.StringParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TelegramInlineKeyboard {
    private static final String BACK_BUTTON_NAME = "« Back";
    private static final String DELETE_BUTTON_NAME = "❌ Delete";
    List<InlineKeyboardButton> keyboardButtonsRow;
    List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
    private String commandName;

    public TelegramInlineKeyboard(String commandName) {
        command(commandName);
        addRow();
    }

    public TelegramInlineKeyboard() {
        addRow();
    }

    public TelegramInlineKeyboard command(String commandName) {
        this.commandName = commandName;
        return this;
    }

    public TelegramInlineKeyboard addButton(String buttonText) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(StringParamUtil.constructCommand(commandName, new String[0]));

        keyboardButtonsRow.add(button);
        return this;
    }

    public TelegramInlineKeyboard addUrlButton(String buttonText, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setUrl(url);
        button.setText(buttonText);

        keyboardButtonsRow.add(button);
        return this;
    }

    public TelegramInlineKeyboard addButton(String buttonText, String... params) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(StringParamUtil.constructCommand(commandName, params));

        keyboardButtonsRow.add(button);
        return this;
    }

    public TelegramInlineKeyboard addBackButton(String... params) {
        return addButton(BACK_BUTTON_NAME, params);
    }

    public TelegramInlineKeyboard addBackToCommandButton(String commandName) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(BACK_BUTTON_NAME);
        button.setCallbackData(StringParamUtil.constructCommand(commandName, new String[0]));

        keyboardButtonsRow.add(button);
        return this;
    }

    public TelegramInlineKeyboard addDeleteButton(String... params) {
        return addButton(DELETE_BUTTON_NAME, params);
    }


    public TelegramInlineKeyboard addRow() {
        keyboardButtonsRow = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        return this;
    }

    public InlineKeyboardMarkup getMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }


}
