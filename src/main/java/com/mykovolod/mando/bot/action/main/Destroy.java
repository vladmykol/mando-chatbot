package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.BotEntityService;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.service.LangBundleService;
import com.mykovolod.mando.service.TelegramInlineKeyboard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Destroy implements MainBotAction {
    private static final String YES_COMMAND = "Yes";
    private final BotService botService;
    private final BotEntityService botEntityService;
    private final LangBundleService langBundleService;

    @Override
    public String getName() {
        return DESTROY;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        if (botUpdate.hasCommandParams()) {
            final var commandFromUser = botUpdate.getFirstCommandParam();

            if (commandFromUser.equals(YES_COMMAND)) {
                final var supportBotByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                if (supportBotByOwner != null) {
                    botService.deleteBot(supportBotByOwner.getId());
                    botUpdate.addOutEditMessage("Your bot is deleted \uD83D\uDE35" +
                            "\n\nPress /start to return to beginning");
                } else {
                    botUpdate.addOutEditMessage("Bot is not found");
                }

            } else {
                botUpdate.addOutEditMessage("Do nothing than");
            }

        } else {
            final var supportBotByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
            if (supportBotByOwner != null) {
                final var reconnectTelegramKeyBoard = new TelegramInlineKeyboard(getName());
                reconnectTelegramKeyBoard.addButton("Hell no!", "no");
                reconnectTelegramKeyBoard.addRow().addButton("❌  Yes, I'm sure", YES_COMMAND);
                reconnectTelegramKeyBoard.addRow().addButton("Cancel", "no");
                final var outMessage = botUpdate.addOutEditMessage("Are you sure you want to delete your bot @" + supportBotByOwner.getBotName() + "?" +
                        "\n\n<i>Bot will be stopped and all settings removed</i>");
                outMessage.setKeyBoard(reconnectTelegramKeyBoard.getMarkup());
                outMessage.setHtmlMode();
            } else {
                var newBotText = langBundleService.getMessage("bot.main.dont_have_bot"
                        , new Object[]{NEW_BOT}
                        , botUpdate.getUser().getLang());

                botUpdate.addOutEditMessage(newBotText);
            }
        }
    }
}
