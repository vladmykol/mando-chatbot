package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsNewService {
    private final ChatRepository chatRepository;
    private final BotService botService;
    private final BotFatherService botFatherService;
    private final LangBundleService langBundleService;

    public void sendUpdateToMainBotUsers() {
        final var allMainBotChats = chatRepository.findByBotId(BotConst.MAIN_BOT_ID);

        allMainBotChats.forEach(chat -> {
            final var optionalUser = botService.findUserById(chat.getTelegramChatId());

            optionalUser.ifPresent(user -> {
                final var whatsNewMsgVersion = langBundleService.getMessage("bot.main.whats_new", LangEnum.ENG);
                final var whatsNewMsgToSend = langBundleService.getMessage("bot.main.whats_new",
                        user.getLang());

                if (!whatsNewMsgVersion.equals(chat.getWhatsNewMsg())) {
                    try {
                        chat.setWhatsNewMsg(whatsNewMsgVersion);
                        botFatherService.sendSimpleMessageByMainBot(user.getId(), whatsNewMsgToSend);
                        chat.setChatError(null);
                    } catch (Exception e) {
                        if (e instanceof TelegramApiRequestException) {
                            final var apiResponse = ((TelegramApiRequestException) e).getApiResponse();
                            log.error("Not possible to send WhatsNew message to chat with {} with error {}", user.getFullName(), apiResponse, e);
                            chat.setChatError(apiResponse);
                        } else {
                            log.error("Not possible to send WhatsNew message to chat with {}", user.getFullName(), e);
                            chat.setChatError(e.getMessage());
                        }
                    } finally {
                        chatRepository.save(chat);
                    }
                }
            });
        });
    }
}
