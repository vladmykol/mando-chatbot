package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.BotEntityService;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.service.LangBundleService;
import com.mykovolod.mando.service.TelegramInlineKeyboard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MainBotStart implements MainBotAction {
    private final LangBundleService langBundleService;
    private final BotService botService;
    private final BotEntityService botEntityService;

    @Override
    public String getName() {
        return START;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        if (botUpdate.hasCommandParams()) {
            final var lang = LangEnum.getEnumOrDefault(botUpdate.getFirstCommandParam());
            botService.setUserPreferredLang(botUpdate.getUser().getId(), lang);

            final var message = langBundleService.getMessage("bot.main.desc", lang);

            botUpdate.addOutEditMessage(message)
                    .setKeyBoard(keyBoardWithSelectLang(lang));
        } else {
            final var message = langBundleService.getMessage("bot.main.desc", botUpdate.getUser().getLang());
            botEntityService.setTempRemoteHelpWithBot(null);
            botUpdate.addOutMessage(message)
                    .setKeyBoard(keyBoardWithSelectLang(botUpdate.getUser().getLang()));
        }
    }

    private InlineKeyboardMarkup keyBoardWithSelectLang(LangEnum currentLang) {
        final var telegramInlineKeyboard = new TelegramInlineKeyboard(START);
        for (String value : Arrays.asList("UKR", "RUS", "ENG")) {
            if (!currentLang.name().equalsIgnoreCase(value)) {
                telegramInlineKeyboard.addButton(value, value);
            }
        }
        return telegramInlineKeyboard.getMarkup();
    }
}
