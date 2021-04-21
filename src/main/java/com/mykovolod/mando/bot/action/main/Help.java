package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.LangBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Help implements MainBotAction {
    private final LangBundleService langBundleService;

    @Override
    public String getName() {
        return HELP;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        final var aboutMsg = langBundleService.getMessage("bot.main.help", botUpdate.getUser().getLang());
        final var sendMessage = botUpdate.addOutMessage(aboutMsg);
        sendMessage.disableWebPagePreview();
        sendMessage.setHtmlMode();
    }
}
