package com.mykovolod.mando.service;

import com.mykovolod.mando.bot.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TelegramService {
    private final Map<String, BotSession> botTelegramSessions = new HashMap<>();
    private final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(SmartBotSession.class);

    public TelegramService() throws TelegramApiException {
    }

    public boolean isRunningById(String botId) {
        final var botSession = botTelegramSessions.get(botId);
        return (botSession != null && botSession.isRunning());
    }

    public void startTelegramBot(Bot bot) {
        stopBot(bot.getBotId());

        try {
            final var newBotSession = (SmartBotSession) telegramBotsApi.registerBot(bot);
            botTelegramSessions.put(bot.getBotId(), newBotSession);
        } catch (TelegramApiException apiException) {
            int errorCode = 0;
            String errorMsg;
            if (apiException.getCause() != null && apiException.getCause() instanceof TelegramApiRequestException) {
                errorCode = ((TelegramApiRequestException) apiException.getCause()).getErrorCode();
            }
            if (errorCode == 401) {
                errorMsg = "error - wrong ApiKey";
            } else {
                errorMsg = apiException.getMessage();
            }
            log.error("Error when registering telegram bot {}", bot.getBotUsername(), apiException);
            throw new RuntimeException(errorMsg);
        }
    }

    public void stopBot(String botId) {
        final var botSession = botTelegramSessions.remove(botId);
        if (botSession != null) {
            try {
                botSession.stop();
            } catch (Exception ignore) {
            }
        }
    }

    @PreDestroy
    public void stopBots() {
        for (BotSession botSession : botTelegramSessions.values()) {
            try {
                botSession.stop();
            } catch (Exception ignore) {
            }
        }
    }
}
