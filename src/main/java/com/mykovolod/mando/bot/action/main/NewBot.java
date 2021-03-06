package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.BotEntityService;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.service.LangBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewBot implements MainBotAction {
    private final BotEntityService botEntityService;
    private final LangBundleService langBundleService;
    private final BotService botService;

    @Override
    public String getName() {
        return NEW_BOT;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        final var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
        if (botByOwner != null) {
            final var botExistsMsg = langBundleService.getMessage("bot.main.newbot.exists",
                    new Object[]{botByOwner.getBotName()},
                    botUpdate.getUser().getLang());

            botUpdate.addOutMessage(botExistsMsg);
            botService.addOutMessageWithDefaultCommands(botUpdate);
        } else {
            final var newBotMsg = langBundleService.getMessage("bot.main.newbot",
                    new Object[]{MainBotAction.SET},
                    botUpdate.getUser().getLang());

            botUpdate.addOutMessage(newBotMsg)
                    .disableWebPagePreview()
                    .setHtmlMode();
        }
    }
}
