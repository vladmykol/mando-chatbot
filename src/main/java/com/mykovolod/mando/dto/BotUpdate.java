package com.mykovolod.mando.dto;

import com.mykovolod.mando.entity.Chat;
import com.mykovolod.mando.entity.User;
import com.mykovolod.mando.utils.StringParamUtil;
import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class BotUpdate {
    private final BotInfo botInfo;
    private final User user;
    private final Chat chat;
    private final String messageId;
    private final String telegramChatId;
    private final String inMessage;
    private final Integer callbackMessageId;
    private final String callbackQueryId;
    private final String replayMessageText;
    private final List<OutMessage> outMessages = new ArrayList<>();
    private final List<SendPhoto> outPhoto = new ArrayList<>();
    private final List<SendDocument> outDocument = new ArrayList<>();

    public OutMessage addOutMessage(String messageText) {
        final var outMessage = new OutMessage();
        outMessage.setSendMessage(new SendMessage(telegramChatId, messageText));
        outMessages.add(outMessage);
        return outMessage;
    }

    public OutMessage addOutEditMessage(String messageText) {
        final var outMessage = new OutMessage();

        if (callbackMessageId != null) {
            final var editMessageText = new EditMessageText();
            editMessageText.setText(messageText);
            editMessageText.setMessageId(callbackMessageId);
            editMessageText.setChatId(telegramChatId);

            outMessage.setEditMessageText(editMessageText);
        } else {
            final var sendMessage = new SendMessage(telegramChatId, messageText);
            outMessage.setSendMessage(sendMessage);
        }
        outMessages.add(outMessage);
        return outMessage;
    }

    public void addSendPhoto(String name, String filePath) {

        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(filePath);

//        try (InputStream inputStream = this.getClass()
//                .getClassLoader()
//                .getResourceAsStream(filePath)) {
        final var inputFile = new InputFile(inputStream, name);
        final var sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setChatId(telegramChatId);
        outPhoto.add(sendPhoto);
//        } catch (IOException e) {
//            addOutMessage("file " + filePath + " is not found");
//        }
    }

    public boolean isCommand() {
        return StringParamUtil.isCommand(inMessage);
    }

    public String getFullCommand() {
        return StringParamUtil.joinWithSeparator(chat.getPendingCommand(), inMessage);
    }

    public String getCommandName() {
        if (hasPendingCommand()) {
            return StringParamUtil.extractCommandName(getFullCommand());
        } else {
            return StringParamUtil.extractCommandName(inMessage);
        }
    }

    public String[] getCommandParams() {
        if (hasPendingCommand()) {
            return StringParamUtil.extractCommandParam(getFullCommand());
        } else {
            return StringParamUtil.extractCommandParam(inMessage);
        }
    }

    public String getFirstCommandParam() {
        return getCommandParam(0);
    }

    public String getSecondCommandParam() {
        return getCommandParam(1);
    }

    public String getThirdCommandParam() {
        return getCommandParam(2);
    }

    public String getCommandParam(int paramNumber) {
        final var commandParams = getCommandParams();
        if (commandParams != null && commandParams.length > paramNumber && commandParams[paramNumber] != null) {
            return commandParams[paramNumber];
        } else {
            return "";
        }
    }

    public boolean hasCommandParams() {
        if (hasPendingCommand()) {
            return StringParamUtil.hasCommandParams(getFullCommand());
        } else {
            return StringParamUtil.hasCommandParams(inMessage);
        }
    }

    public String getBotId() {
        return botInfo.getBotId();
    }

    public void addOutDocument(SendDocument document) {
        document.setChatId(telegramChatId);
        outDocument.add(document);
    }

    public boolean hasPendingCommand() {
        return chat.getPendingCommand() != null && !chat.getPendingCommand().isEmpty();
    }

    public boolean hasOutMessages() {
        return outMessages.stream().map(inMessage -> inMessage.getText().trim())
                .noneMatch(String::isEmpty);
    }
}
