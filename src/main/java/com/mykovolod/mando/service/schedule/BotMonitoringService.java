package com.mykovolod.mando.service.schedule;

import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.repository.ChatRepository;
import com.mykovolod.mando.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotMonitoringService {
    public static final int MS_PER_MIN = 60000;
    private final int ONCE_A_DAY = 24 * 60 * 60000;
    private final BotEntityService botEntityService;
    private final WhatsNewService whatsNewService;
    private final BotFatherService botFatherService;
    private final BotService botService;
    private final ChatRepository chatRepository;
    private final ChatWithBotOwnerService chatWithBotOwnerService;


    @Scheduled(initialDelay = 2 * MS_PER_MIN, fixedDelay = Long.MAX_VALUE)
    public void runOnceAfterStart() {
        whatsNewService.sendUpdateToMainBotUsers();
    }

    @Scheduled(initialDelay = 3 * MS_PER_MIN, fixedRate = ONCE_A_DAY)
    public void sendHelpRequestIfBotOffline() {
        final var allBotsExceptMain = botEntityService.findAllBotsExceptMain();
        allBotsExceptMain.forEach(botEntity -> {
            if (botEntity.getStatus() != BotStatus.FROZEN) {
                final var botRunningStatusById = botFatherService.getBotRunningStatusById(botEntity.getId());
                if (botRunningStatusById != null && !botRunningStatusById.equals("online")) {
                    chatWithBotOwnerService.sendNeedHelpMsg(botEntity.getOwnerId(), botEntity.getId());
                }
            }
        });
    }

    @Scheduled(initialDelay = 5 * MS_PER_MIN, fixedRate = ONCE_A_DAY)
    public void removeBlocked() {
        final var allMainBotChats = chatRepository.findByBotId(BotConst.MAIN_BOT_ID);
        allMainBotChats.forEach(chat -> {
            if (chat.getChatError() != null) {
                log.info("Chat with id {} was removed as was blocked: {}", chat.getId(), chat.getChatError());
                chatRepository.delete(chat);
                final var supportBotByOwner = botEntityService.findSupportBotByOwner(chat.getTelegramChatId());
                if (supportBotByOwner != null) {
                    log.warn("Bot with name {} was removed as chat with owner was removed", supportBotByOwner.getBotName());
                    botService.deleteBot(supportBotByOwner.getId());
                }
            }
        });
    }

    @Scheduled(initialDelay = 10 * MS_PER_MIN, fixedRate = 10 * ONCE_A_DAY)
    public void notifyOwnerOfNotPopularBot() {
        final var allBotsExceptMain = botEntityService.findAllBotsExceptMain();
        allBotsExceptMain.forEach(botEntity -> {
            if (botEntity.getStatus() != BotStatus.FROZEN) {
                if (!botService.isBotInUse(botEntity.getId())) {
                    log.info("Bot {} was FROZEN as it is not used", botEntity.getBotName());
                    botFatherService.freezeBot(botEntity);
                    chatWithBotOwnerService.sendBotFrozenMsg(botEntity.getOwnerId(), botEntity.getId());
                }
            }
        });
    }
}
