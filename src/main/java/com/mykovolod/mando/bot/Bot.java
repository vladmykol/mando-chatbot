package com.mykovolod.mando.bot;

import com.mykovolod.mando.bot.action.BotAction;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.BotInfo;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.entity.MessageEntity;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.utils.StringParamUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.doccat.DocumentCategorizerME;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public abstract class Bot extends TelegramLongPollingBot {
    final BotService botService;
    @Setter
    private Map<String, BotAction> actions = new HashMap<>();
    private BotInfo info;
    @Getter
    private Map<LangEnum, DocumentCategorizerME> categorizers;

    public void setDebugMode(Boolean debugMode) {
        info.setDebugMode(debugMode);
    }

    public void setUseGpt3(Boolean useGpt3) {
        info.setUseGpt3(useGpt3);
    }

    public void initBot(BotInfo botInfo) {
        this.info = botInfo;
        train();
    }

    public void addBotAction(List<? extends BotAction> actionList) {
        actionList.forEach(action -> {
            actions.put(action.getName(), action);
        });
    }

    public void train() {
        categorizers = botService.trainCategorizers(info.getBotId());
    }

    @Override
    public String getBotUsername() {
        return info.getName();
    }

    @Override
    public String getBotToken() {
        return info.getKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        BotUpdate botUpdate = saveAndBuildBotUpdate(update);
        if (botUpdate != null) {
            if (botUpdate.isCommand()) {
                botService.removePendingCommand(botUpdate.getChat());
                processCommand(botUpdate);
            } else if (botUpdate.hasPendingCommand()) {
                processCommand(botUpdate);
            } else {
                processTextMessage(botUpdate);
            }

            saveUpdateResponse(botUpdate);
            respondWith(botUpdate);
        }
    }

    private void saveUpdateResponse(BotUpdate botUpdate) {
        final var messageEntity = botService.getSavedMessage(botUpdate.getMessageId());
        botService.updateMessageWithResponse(messageEntity, botUpdate.getOutMessages());
    }

    private void respondWith(BotUpdate botUpdate) {
        if (botUpdate.getCallbackQueryId() != null) {
            final var answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setText(null);
            answerCallbackQuery.setShowAlert(false);
            answerCallbackQuery.setCallbackQueryId(botUpdate.getCallbackQueryId());
            try {
                execute(answerCallbackQuery);
            } catch (TelegramApiException e) {
                log.error("Cannot answer callback query", e);
            }
        }

        botUpdate.getOutMessages().forEach(message -> {
            try {
                execute(message.get());
            } catch (TelegramApiException e) {
                log.error("Cannot reply to telegram message {}", botUpdate.getInMessage(), e);
            }
        });
        botUpdate.getOutPhoto().forEach(message -> {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Cannot reply with photo to telegram message {}", botUpdate.getInMessage(), e);
            }
        });
        botUpdate.getOutDocument().forEach(message -> {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Cannot reply with document to telegram message {}", botUpdate.getInMessage(), e);
            }
        });
    }

    private BotUpdate saveAndBuildBotUpdate(Update update) {
        String telegramChatId;
        User user;
        String messageText;
        Integer callbackMessageId = null;
        String callbackQueryId = null;
        String replayMessageText = null;
        if (update.hasCallbackQuery()) {
            telegramChatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            user = update.getCallbackQuery().getFrom();
            messageText = update.getCallbackQuery().getData().trim();
            callbackMessageId = update.getCallbackQuery().getMessage().getMessageId();
            callbackQueryId = update.getCallbackQuery().getId();
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {
                final var replyToMessage = update.getMessage().getReplyToMessage();
                if (replyToMessage != null) {
                    replayMessageText = replyToMessage.getText();
                }
                telegramChatId = String.valueOf(update.getMessage().getChatId());
                user = update.getMessage().getFrom();
                messageText = update.getMessage().getText().trim();
                final var startParam = StringParamUtil.extractStartParam(messageText);
                if (startParam != null) {
                    messageText = startParam;
                }
            } else {
                return null;
            }
        }
        final var savedUser = botService.saveUser(user);
        final var chat = botService.saveChat(telegramChatId, getBotId());

        final var messageEntity = MessageEntity.builder()
                .chatId(chat.getId())
                .inMessage(messageText)
                .userId(savedUser.getId())
                .build();
        final var savedMessageEntity = botService.saveMessage(messageEntity);

        return BotUpdate.builder()
                .botInfo(info)
                .user(savedUser)
                .chat(chat)
                .messageId(savedMessageEntity.getId())
                .telegramChatId(chat.getTelegramChatId())
                .inMessage(messageText)
                .callbackMessageId(callbackMessageId)
                .callbackQueryId(callbackQueryId)
                .replayMessageText(replayMessageText)
                .build();
    }

    protected abstract void processSharedContact(Contact contact);

    protected abstract void processTextMessage(BotUpdate botUpdate);

    public void processCommand(BotUpdate botUpdate) {
        final var botAction = actions.get(botUpdate.getCommandName());
        if (botAction != null) {
            botAction.handle(botUpdate);
        } else {
            botService.addNotFoundCommandMsg(botUpdate);
        }
    }

    public String getBotId() {
        return info.getBotId();
    }


    public void sendMessageToChat(String chatId, String text) {
        final var sendMessage = new SendMessage(chatId, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Cannot send message to chat {}", chatId, e);
        }
    }
}
