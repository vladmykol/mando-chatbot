package com.mykovolod.mando.bot;

import com.mykovolod.mando.bot.action.support.SupportBotAction;
import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.dto.Intent;
import com.mykovolod.mando.dto.OutMessage;
import com.mykovolod.mando.entity.MessageEntity;
import com.mykovolod.mando.service.BotService;
import com.mykovolod.mando.service.TelegramKeyboard;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Contact;

import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SupportBot extends Bot {
    public SupportBot(BotService botService,
                      List<SupportBotAction> intents) {
        super(botService);
        addBotAction((intents));
    }

    @Override
    protected void processSharedContact(Contact contact) {
    }

    @Override
    protected void processTextMessage(BotUpdate botUpdate) {
        final var messageEntity = botService.getSavedMessage(botUpdate.getMessageId());
        if (botUpdate.getChat().getIsOperatorConnected() != null && botUpdate.getChat().getIsOperatorConnected()) {
            botService.resendMessageToBotOwner(messageEntity, botUpdate.getBotId());
        } else {
            intentBasedResponse(botUpdate, messageEntity);
        }
    }

    private void intentBasedResponse(BotUpdate botUpdate, MessageEntity messageEntity) {
        OutMessage lastOutMessage = new OutMessage();
        Intent lastIntent = new Intent(LangEnum.ENG);

        final var intentsPerSentence = botService.detectIntent(botUpdate, getCategorizers());

        for (Intent intent : intentsPerSentence) {
            lastIntent = intent;
            botService.updateMessageWithIntent(messageEntity, lastIntent);
            if (botUpdate.getBotInfo().isDebugMode()) {
                botService.resendMessageToBotOwner(messageEntity, botUpdate.getBotId());
            }


            final var response = lastIntent.getResponse();
            if (response == null) {
                lastOutMessage = botUpdate.addOutMessage("Response is not set for Intent - " + lastIntent.getName());
            } else {
                lastOutMessage = botUpdate.addOutMessage(response).setHtmlMode();
            }
        }

        if (lastIntent.getResponseButtons() != null && !lastIntent.getResponseButtons().isEmpty()) {
            var telegramKeyboard = new TelegramKeyboard();
            int buttonsInRow = 0;
            for (String buttonName : lastIntent.getResponseButtons()) {
                telegramKeyboard.addButton(buttonName);
                buttonsInRow++;
                if (buttonsInRow >= 2) {
                    telegramKeyboard.addRow();
                    buttonsInRow = 0;
                }
            }
            lastOutMessage.setKeyBoard(telegramKeyboard.getMarkup());
        } else {
            var removeKeyboard = TelegramKeyboard.removeKeyBoard();
            lastOutMessage.setKeyBoard(removeKeyboard);
        }

        if (BotConst.CONNECT_OPERATOR_INTENT_ID.equalsIgnoreCase(lastIntent.getName())) {
            botService.connectOperatorToChat(botUpdate);
        }
    }

}
