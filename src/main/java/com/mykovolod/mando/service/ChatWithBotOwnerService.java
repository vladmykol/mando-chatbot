package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatWithBotOwnerService {
    private final ChatRepository chatRepository;
    private final BotService botService;
    private final LangBundleService langBundleService;

    private void messageToBotOwner(String ownerId, String botId, String messageCode) {
        var chat = chatRepository.findByTelegramChatIdAndBotId(ownerId, BotConst.MAIN_BOT_ID);
        if (chat != null) {
            final var optionalUser = botService.findUserById(ownerId);
            optionalUser.ifPresent(user -> {
                final var needHelpMsg = langBundleService.getMessage(messageCode,
                        user.getLang());
                try {
                    botService.resendSimpleMessageToBotOwner(needHelpMsg, botId);
                    chat.setChatError(null);
                } catch (Exception e) {
                    if (e instanceof TelegramApiRequestException) {
                        final var apiResponse = ((TelegramApiRequestException) e).getApiResponse();
                        log.error("Not possible to send '" + messageCode + "' message to chat with {} with error {}", user.getFullName(), apiResponse, e);
                        chat.setChatError(apiResponse);
                    } else {
                        log.error("Not possible to send '" + messageCode + "' message to chat with {}", user.getFullName(), e);
                        chat.setChatError(e.getMessage());
                    }
                } finally {
                    chatRepository.save(chat);
                }
            });
        }
    }

    public void sendNeedHelpMsg(String ownerId, String botId) {
        messageToBotOwner(ownerId, botId, "bot.main.need-help");
    }

    public void sendBotFrozenMsg(String ownerId, String botId) {
        messageToBotOwner(ownerId, botId, "bot.main.bot-frozen");
    }
}
