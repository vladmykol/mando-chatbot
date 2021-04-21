package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.service.ChatService;
import com.mykovolod.mando.service.LangBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MainBotStop implements MainBotAction {
    private final ChatService chatService;
    private final BotService botService;
    private final LangBundleService langBundleService;

    @Override
    public String getName() {
        return STOP;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        chatService.removePendingCommand(botUpdate.getChat());

        final var message = langBundleService.getMessage("bot.main.stop",
                botUpdate.getUser().getLang());
        botUpdate.addOutMessage(message);

        botService.addOutMessageWithDefaultCommands(botUpdate);
    }

}
