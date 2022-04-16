package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.LangBundleService;
import com.mykovolod.mando.service.TelegramInlineKeyboard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class About implements MainBotAction {
    private final LangBundleService langBundleService;

    @Override
    public String getName() {
        return ABOUT;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        final var aboutMsg = langBundleService.getMessage("bot.main.about", new Object[]{"\uD83D\uDCA1"}, botUpdate.getUser().getLang());
        final var sendMessage = botUpdate.addOutMessage(aboutMsg);
        sendMessage.disableWebPagePreview();
        sendMessage.setHtmlMode();

        botUpdate.addSendPhoto("bot_logic.jpg", "static" + File.separator + "bot_logic.jpg");
    }
}
