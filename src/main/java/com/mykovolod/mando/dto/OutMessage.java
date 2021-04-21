package com.mykovolod.mando.dto;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Data
public class OutMessage {
    private SendMessage sendMessage;
    private EditMessageText editMessageText;

    public void setKeyBoard(InlineKeyboardMarkup replyKeyboard) {
        if (editMessageText != null) {
            editMessageText.setReplyMarkup(replyKeyboard);
        } else {
            sendMessage.setReplyMarkup(replyKeyboard);
        }
    }

    public void setKeyBoard(ReplyKeyboard replyKeyboard) {
        sendMessage.setReplyMarkup(replyKeyboard);
    }

    public BotApiMethod<?> get() {
        if (editMessageText != null) {
            return editMessageText;
        } else {
            return sendMessage;
        }
    }

    public String getText() {
        if (editMessageText != null) {
            return editMessageText.getText();
        } else {
            return sendMessage.getText();
        }
    }


    public OutMessage disableWebPagePreview() {
        if (editMessageText != null) {
            editMessageText.disableWebPagePreview();
        } else {
            sendMessage.disableWebPagePreview();
        }
        return this;
    }

    public OutMessage setHtmlMode() {
        if (editMessageText != null) {
            editMessageText.setParseMode("html");
        } else {
            sendMessage.setParseMode("html");
        }
        return this;
    }

}
