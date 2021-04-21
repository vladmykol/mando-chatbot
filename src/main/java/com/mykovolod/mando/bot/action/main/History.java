package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.service.BotEntityService;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.service.ChatService;
import com.mykovolod.mando.service.LangBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class History implements MainBotAction {
    private final BotEntityService botEntityService;
    private final BotService botService;
    private final ChatService chatService;
    private final LangBundleService langBundleService;

    @Override
    public String getName() {
        return HISTORY;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        final var optionalBotEntity = botEntityService.findBotById(botUpdate.getBotId());
        optionalBotEntity.ifPresent(thisBotEntity -> {
            var responseText = langBundleService.getMessage("bot.main.questions.recent"
                    , botUpdate.getUser().getLang());

            if (thisBotEntity.getOwnerId().equals(botUpdate.getUser().getId())) {
                final var allBots = botEntityService.findAllByOrderByStatus();
                botUpdate.addOutMessage(responseText);

                allBots.forEach(botEntity -> {
                    if (botEntity.getStatus().equals(BotStatus.ACTIVE)) {
                        final var lastBotMessages = chatService.getLastBotMessages(botEntity.getId());
                        botService.resendLastBotMessages(botUpdate, lastBotMessages, botEntity.getBotName());
                    }
                });

            } else {
                final var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                if (botByOwner != null) {
                    botUpdate.addOutMessage(responseText);
                    final var lastBotMessages = chatService.getLastBotMessages(botByOwner.getId());
                    botService.resendLastBotMessages(botUpdate, lastBotMessages, botByOwner.getBotName());
                } else {
                    responseText = langBundleService.getMessage("bot.main.dont_have_bot"
                            , new Object[]{NEW_BOT}
                            , botUpdate.getUser().getLang());
                    botUpdate.addOutMessage(responseText);
                }
            }
        });
    }

}
