package com.mykovolod.mando.service;

import com.mykovolod.mando.bot.Bot;
import com.mykovolod.mando.bot.MainBot;
import com.mykovolod.mando.bot.SupportBot;
import com.mykovolod.mando.conts.BotType;
import com.mykovolod.mando.dto.BotInfo;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.repository.BotEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public abstract class BotFatherService {
    private final TelegramService telegramService;
    private final BotEntityRepository botEntityRepository;
    Map<String, Bot> botMap = new HashMap<>();
    @Value("${telegram.bot.owner.userid}")
    String mainBotOwnerUserId;

    public void initBots() {
        final var allBots = botEntityRepository.findByStatus(BotStatus.ACTIVE);
        startBots(allBots);
        notifyMainBotOwner("Mando was started. Total existing bots: " + allBots.size());
    }

    public void sendMessageByMainBot(String chatId, String text) {
        getMainBot().sendMessageFromUser(chatId, text);
    }

    public void sendSimpleMessageByMainBot(String chatId, String text) throws TelegramApiException {
        getMainBot().sendSimpleMessage(chatId, text);
    }

    public void notifyMainBotOwner(String text) {
        try {
            getMainBot().sendSimpleMessage(mainBotOwnerUserId, text);
        } catch (Exception e) {
            log.error("Was not able to notify MainBotOwner. Notification - {}", text, e);
        }
    }

    public void sendMessageByMainBot(String chatId, String text, SendDocument document) {
        getMainBot().sendMessageFromUser(chatId, text, document);
    }

    public void sendMessageBySupportBot(String botId, String telegramChatId, String text) {
        botMap.get(botId).sendMessageToChat(telegramChatId, text);
    }

    public String getBotRunningStatusById(String botId) {
        String status = "offline";
        final var bot = botMap.get(botId);
        if (bot != null) {
            if (telegramService.isRunningById(botId)) {
                status = "online";
            }
        }

        final var botEntityOptional = botEntityRepository.findById(botId);
        if (botEntityOptional.isPresent()) {
            var initError = botEntityOptional.get().getInitError();
            if (initError != null) {
                status += " " + initError;
            }
        }
        return status;
    }

    public void freezeBot(BotEntity botEntity) {
        stopBot(botEntity.getId());
        botEntity.setStatus(BotStatus.FROZEN);
        botEntityRepository.save(botEntity);
    }

    public void unFreezeBot(BotEntity botEntity) {
        botEntity.setStatus(BotStatus.ACTIVE);
        startBot(botEntity);
        botEntityRepository.save(botEntity);
    }

    public void setBotDebugMode(String botId, Boolean debugMode) {
        botMap.get(botId).setDebugMode(debugMode);
    }

    public void trainBotById(String botId) {
        botMap.get(botId).train();
    }

    public void startBot(BotEntity botEntity) {
        Bot bot;
        String initError = null;
        if (botEntity.getBotType() == BotType.MAIN) {
            bot = getMainBot();
        } else {
            bot = newSupportBot();
        }
        try {
            bot.initBot(new BotInfo(botEntity.getId(), botEntity.getBotName(), botEntity.getBotToken(), botEntity.isDebugMode()));
            botMap.put(bot.getBotId(), bot);
        } catch (Exception e) {
            initError = "❗️" + e.getMessage();
            log.error("@{} initialization error: {}", botEntity.getBotName(), e.getMessage());
        }

        if (initError == null) {
            try {
                telegramService.startTelegramBot(bot);
            } catch (Exception e) {
                initError = "❗️Telegram " + e.getMessage();
            }
        }
        botEntity.setInitError(initError);
        botEntityRepository.save(botEntity);
    }

    public void startBots(List<BotEntity> bots) {
        for (BotEntity botEntity : bots) {
            startBot(botEntity);
        }
    }

    public void stopBot(String botId) {
        botMap.remove(botId);
        telegramService.stopBot(botId);
    }

    @Lookup
    public abstract SupportBot newSupportBot();

    @Lookup
    public abstract MainBot getMainBot();


}
