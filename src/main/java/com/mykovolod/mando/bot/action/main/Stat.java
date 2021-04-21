package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.entity.Chat;
import com.mykovolod.mando.repository.*;
import com.mykovolod.mando.service.BotEntityService;
import com.mykovolod.mando.service.BotFatherService;
import com.mykovolod.mando.service.LangBundleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Stat implements MainBotAction {
    private final BotEntityRepository botEntityRepository;
    private final BotEntityService botEntityService;
    private final BotFatherService botFatherService;
    private final ChatRepository chatRepository;
    private final IntentRepository intentRepository;
    private final UserRepository userRepository;
    private final MessageEntityRepository messageEntityRepository;
    private final LangBundleService langBundleService;

    @Override
    public String getName() {
        return STAT;
    }

    @Override
    public void handle(BotUpdate botUpdate) {

        final var optionalMainBotEntity = botEntityRepository.findById(botUpdate.getBotId());
        optionalMainBotEntity.ifPresent(mainBotEntity -> {
            if (isMainBotOwner(botUpdate, mainBotEntity)) {
                final var allBots = botEntityRepository.findAllByOrderByStatus();

                botUpdate.addOutDocument(getSendDocument(allBots));

            } else {
                final var supportBotByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                if (supportBotByOwner != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    addBotInfo(stringBuilder, supportBotByOwner, false);

                    botUpdate.addOutEditMessage(stringBuilder.toString());
                } else {
                    var newBotText = langBundleService.getMessage("bot.main.dont_have_bot"
                            , new Object[]{NEW_BOT}
                            , botUpdate.getUser().getLang());

                    botUpdate.addOutEditMessage(newBotText);
                }
            }
        });
    }

    private SendDocument getSendDocument(List<BotEntity> allBots) {
        StringBuilder stringBuilder = new StringBuilder();
        allBots.forEach(botEntity -> {
            if (botEntity.getStatus().equals(BotStatus.ACTIVE)) {
                addBotInfo(stringBuilder, botEntity, true);

                stringBuilder.append("\r\n\r\n");
            }
        });

        final var inputFile = new InputFile();
        final var inputStream = IOUtils.toInputStream(stringBuilder.toString(), StandardCharsets.UTF_8);
        inputFile.setMedia(inputStream, "all-bots.txt");
        final var sendDocument = new SendDocument();
        sendDocument.setDocument(inputFile);
        return sendDocument;
    }

    private boolean isMainBotOwner(BotUpdate botUpdate, BotEntity mainBotEntity) {
        return mainBotEntity.getOwnerId().equals(botUpdate.getUser().getId());
    }

    private void addBotInfo(StringBuilder stringBuilder, BotEntity botEntity, boolean isAddBotOwnerInfo) {
        stringBuilder.append("Bot: @").append(botEntity.getBotName());
        final var botStatus = botFatherService.getBotRunningStatusById(botEntity.getId());
        stringBuilder.append("\r\nStatus: ").append(botStatus);

        if (isAddBotOwnerInfo) {
            final var optionalUser = userRepository.findById(botEntity.getOwnerId());
            String userName = "unknown";
            String account = null;
            if (optionalUser.isPresent()) {
                userName = optionalUser.get().getFirstName() + " " + optionalUser.get().getLastName();
                account = optionalUser.get().getUserName();
                final var chatWithOwner = chatRepository.findByTelegramChatIdAndBotId(optionalUser.get().getId(),
                        botEntity.getId());

                if (chatWithOwner != null && chatWithOwner.getChatError() != null) {
                    stringBuilder.append("\r\nChat with owner: ").append(chatWithOwner.getChatError());
                }
            }
            stringBuilder.append("\r\nOwner: ").append(userName);
            if (account != null) {
                stringBuilder.append(" @").append(account);
            }
        }

        final var allIntents = intentRepository.findByBotId(botEntity.getId());
        stringBuilder.append("\r\nIntents count: ").append(allIntents.size());

        final var allChats = chatRepository.findByBotId(botEntity.getId());
        stringBuilder.append("\r\nTotal users count: ").append(allChats.size());

        final var chatIds = allChats.stream().map(Chat::getId).collect(Collectors.toList());
        final var totalMessage = messageEntityRepository.countAllByChatIdIn(chatIds);
        stringBuilder.append("\r\nTotal messages count: ").append(totalMessage);

        stringBuilder.append("\r\nLast 1/5/15 days messages count: ")
                .append(getMsgCountForLastDays(chatIds, 1)).append("/")
                .append(getMsgCountForLastDays(chatIds, 5)).append("/")
                .append(getMsgCountForLastDays(chatIds, 15));
    }

    private long getMsgCountForLastDays(List<String> chatIds, int days) {
        Date dateFrom = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days));
        return messageEntityRepository.countAllByChatIdInAndCreateDateGreaterThan(chatIds, dateFrom);
    }
}
