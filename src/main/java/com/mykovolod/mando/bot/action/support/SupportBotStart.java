package com.mykovolod.mando.bot.action.support;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.ChatService;
import com.mykovolod.mando.service.IntentService;
import com.mykovolod.mando.service.ResponseButtonsService;
import com.mykovolod.mando.service.TelegramKeyboard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupportBotStart implements SupportBotAction {
    private final IntentService intentService;
    private final ChatService chatService;
    private final ResponseButtonsService responseButtonsService;

    @Override
    public String getName() {
        return START;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        final var onStartIntentData = intentService.getOnStartIntentData(botUpdate);

        if (onStartIntentData.isPresent()) {
            final var buttonNames = responseButtonsService.findButtonNamesByIntentDataId(onStartIntentData.get().getId());

            final var randomResponse = intentService.getRandomItem(onStartIntentData.get().getResponses());

            final var outMessage = botUpdate.addOutMessage(randomResponse).setHtmlMode();

            if (buttonNames != null && !buttonNames.isEmpty()) {
                var telegramKeyboard = new TelegramKeyboard();
                int buttonsInRow = 0;
                for (String buttonName : buttonNames) {
                    telegramKeyboard.addButton(buttonName);
                    buttonsInRow++;
                    if (buttonsInRow >= 2) {
                        telegramKeyboard.addRow();
                        buttonsInRow = 0;
                    }
                }
                outMessage.setKeyBoard(telegramKeyboard.getMarkup());
            }
        }

        chatService.resetChat(botUpdate.getChat().getId());
    }

}
