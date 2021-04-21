package com.mykovolod.mando.bot;

import com.mykovolod.mando.bot.action.main.MainBotAction;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.utils.StringParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@Slf4j
public class MainBot extends Bot {
    public MainBot(BotService botService,
                   List<MainBotAction> mainBotActions) {
        super(botService);
        addBotAction(mainBotActions);
    }

    @Override
    protected void processSharedContact(Contact contact) {
    }

    @Override
    protected void processTextMessage(BotUpdate botUpdate) {
        if (botUpdate.getReplayMessageText() != null) {
            final var msgId = StringParamUtil.extractIdFromMsg(botUpdate.getReplayMessageText());
            if (msgId != null) {
                botService.replayToUser(msgId, botUpdate);
            }
        } else {
            botService.removePendingCommand(botUpdate.getChat());
            botService.addNotFoundCommandMsg(botUpdate);
            botService.addOutMessageWithDefaultCommands(botUpdate);
        }
    }

    public void sendMessageFromUser(String chatId, String text) {
        sendMessageFromUser(chatId, text, null);
    }

    public void sendSimpleMessage(String chatId, String text) throws TelegramApiException {
        final var sendMessage = new SendMessage(chatId, text);
        sendMessage.setParseMode("html");
        final var removeKeyboard = new ReplyKeyboardRemove(true);
        sendMessage.setReplyMarkup(removeKeyboard);
        execute(sendMessage);
    }

    public void sendMessageFromUser(String chatId, String text, SendDocument document) {
        final var sendMessage = new SendMessage(chatId, text);
        sendMessage.setDisableWebPagePreview(true);
        final var forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setForceReply(true);
        sendMessage.setReplyMarkup(forceReplyKeyboard);
        if (document != null) {
            document.setChatId(chatId);
        }
        try {
            execute(sendMessage);
            if (document != null) {
                execute(document);
            }
        } catch (TelegramApiException e) {
            log.error("Cannot send message to chat {}", chatId, e);
        }
    }

}
