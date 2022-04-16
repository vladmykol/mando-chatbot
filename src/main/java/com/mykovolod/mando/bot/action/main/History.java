package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class History implements MainBotAction {
    private final BotEntityService botEntityService;
    private final BotService botService;
    private final ChatService chatService;
    private final LangBundleService langBundleService;
    private static final String LOAD_FULL = "Load full";

    @Override
    public String getName() {
        return HISTORY;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        var loadAllText = langBundleService.getMessage("bot.main.intent.history.all"
                , botUpdate.getUser().getLang());
        var loadFullButtonText = langBundleService.getMessage("bot.main.intent.history.yes.full"
                , botUpdate.getUser().getLang());

        final var optionalBotEntity = botEntityService.findBotById(botUpdate.getBotId());
        optionalBotEntity.ifPresent(thisBotEntity -> {
            if (botUpdate.hasCommandParams()) {
                final var firstCommandParam = botUpdate.getFirstCommandParam();

                if (firstCommandParam.equals(LOAD_FULL)) {
                    if (thisBotEntity.getOwnerId().equals(botUpdate.getUser().getId())) {
                        final var allBots = botEntityService.findAllByOrderByStatus();

                        for (BotEntity botEntity : allBots) {
                            if (botEntity.getStatus().equals(BotStatus.ACTIVE)) {
                                final var lastBotMessages = chatService.getLastAllBotMessages(botEntity.getId());
                                botService.resendLastBotMessages(botUpdate, lastBotMessages, botEntity.getBotName(), "_all");
                            }
                        }
                    } else {
                        final var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                        if (botByOwner != null) {
                            final var lastBotMessages = chatService.getLastAllBotMessages(botByOwner.getId());
                            botService.resendLastBotMessages(botUpdate, lastBotMessages, botByOwner.getBotName(), "_all");
                        }
                    }
                } else {
                    final var noSuchMsg = langBundleService.getMessage("bot.main.command_param.not_found"
                            , botUpdate.getUser().getLang());
                    chatService.removePendingCommand(botUpdate.getChat());
                    botUpdate.addOutEditMessage(noSuchMsg);
                }

            } else {
                log.info("requesting history responseText");
                var responseText = langBundleService.getMessage("bot.main.questions.recent"
                        , botUpdate.getUser().getLang());

                if (thisBotEntity.getOwnerId().equals(botUpdate.getUser().getId())) {
                    final var allBots = botEntityService.findAllByOrderByStatus();
                    botUpdate.addOutMessage(responseText);
                    log.info("requesting history for all bots");

                    final var pageSize = 60;
                    for (BotEntity botEntity : allBots) {
                        if (botEntity.getStatus().equals(BotStatus.ACTIVE)) {
                            final var lastBotMessages = chatService.getLastBotMessages(botEntity.getId(), pageSize);
                            botService.resendLastBotMessages(botUpdate, lastBotMessages, botEntity.getBotName(), "");
                        }
                    }

                    var loadMoreKeyboard = new TelegramInlineKeyboard(getName())
                            .addButton(loadFullButtonText, LOAD_FULL);
                    botUpdate
                            .addOutEditMessage(loadAllText)
                            .setKeyBoard(loadMoreKeyboard.getMarkup());

                } else {
                    final var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                    if (botByOwner != null) {
                        log.info("requesting history for one bot "+ botByOwner.getBotName());
                        botUpdate.addOutMessage(responseText);
                        final var pageSize = 60;
                        final var lastBotMessages = chatService.getLastBotMessages(botByOwner.getId(), pageSize);
                        botService.resendLastBotMessages(botUpdate, lastBotMessages, botByOwner.getBotName(), "");
                        if (lastBotMessages.size() == pageSize) {
                            var loadMoreKeyboard = new TelegramInlineKeyboard(getName())
                                    .addButton(loadFullButtonText, LOAD_FULL);
                            botUpdate
                                    .addOutEditMessage(loadAllText)
                                    .setKeyBoard(loadMoreKeyboard.getMarkup());
                        }
                    } else {
                        responseText = langBundleService.getMessage("bot.main.dont_have_bot"
                                , new Object[]{NEW_BOT}
                                , botUpdate.getUser().getLang());
                        botUpdate.addOutMessage(responseText);
                    }
                }
            }
        });
    }

}
