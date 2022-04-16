package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.BotType;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.entity.User;
import com.mykovolod.mando.repository.BotEntityRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class BotEntityService {
    private final BotEntityRepository botEntityRepository;
    private final BotFatherService botFatherService;
    private final IntentService intentService;
    private final Gpt3Service gpt3Service;
    @Value("${telegram.bot.owner.userid}")
    String mainBotOwnerUserId;
    private String remoteHelpWithBotId;
    @Value("${telegram.bot.name}")
    @Getter
    private String mainBotName;

    public void saveBot(BotEntity newBotEntity) {
        final var optionalBotEntity = botEntityRepository.findById(newBotEntity.getId());

        BotEntity botEntity;
        if (optionalBotEntity.isPresent()) {
            botEntity = optionalBotEntity.get();
            botEntity.setBotName(newBotEntity.getBotName());
            botEntity.setBotToken(newBotEntity.getBotToken());
            botEntity.setOwnerId(newBotEntity.getOwnerId());
            botEntityRepository.save(botEntity);
        } else {
            botEntity = newBotEntity;
        }

        botEntityRepository.save(botEntity);
    }

    public Optional<BotEntity> findBotById(String botId) {
        return botEntityRepository.findById(botId);
    }

    public List<BotEntity> findAllByOrderByStatus() {
        return botEntityRepository.findAllByOrderByStatus();
    }

    public BotEntity findSupportBotByOwner(String ownerUserId) {
        BotEntity botEntity = null;
        log.info("Find support bot by owner id =" + ownerUserId + " and remoteHelpWithBotId = " + remoteHelpWithBotId);
        if (ownerUserId.equals(mainBotOwnerUserId) && remoteHelpWithBotId != null) {
            botEntity = botEntityRepository.findByIdAndBotType(remoteHelpWithBotId, BotType.SUPPORT);
        }

        if (botEntity == null) {
            return botEntityRepository.findByOwnerIdAndBotType(ownerUserId, BotType.SUPPORT);
        } else {
            return botEntity;
        }
    }

    public BotEntity newBotTelegramApiKey(String user, String botApiKey) {
        final var botByOwner = findSupportBotByOwner(user);
        if (botByOwner != null) {
            botByOwner.setBotToken(botApiKey);
            return botEntityRepository.save(botByOwner);
        }
        return null;
    }


    public void newBotOrUpdateExisting(User user, String botName) {
        var botByOwner = findSupportBotByOwner(user.getId());
        if (botByOwner != null) {
            botByOwner.setBotName(botName);
            botEntityRepository.save(botByOwner);
        } else {
            Set<LangEnum> defaultLangEnumSet = new HashSet<>();
            defaultLangEnumSet.add(user.getLang());

            var newBotEntity = BotEntity.builder()
                    .botName(botName)
                    .ownerId(user.getId())
                    .status(BotStatus.ACTIVE)
                    .supportedLang(defaultLangEnumSet)
                    .botType(BotType.SUPPORT)
                    .debugMode(false)
                    .useGpt3(true)
                    .build();
            final var botEntity = botEntityRepository.save(newBotEntity);
            intentService.copyDefaultSettingForNewBot(botEntity.getId());
            intentService.presetConnectWithOperatorButton(botEntity);
            intentService.presetHiButtons(botEntity);
        }
    }

    public boolean setBotDebugMode(String botId) {
        final var optionalBotEntity = findBotById(botId);
        boolean debugMode = false;
        if (optionalBotEntity.isPresent()) {
            debugMode = optionalBotEntity.get().isDebugMode();
            debugMode = !debugMode;
            optionalBotEntity.get().setDebugMode(debugMode);
            botEntityRepository.save(optionalBotEntity.get());
        }
        botFatherService.setBotDebugMode(botId, debugMode);

        return debugMode;
    }

    public Boolean setUseGpt3(String botId) {
        final var optionalBotEntity = findBotById(botId);
        boolean useGpt3 = false;
        if (optionalBotEntity.isPresent()) {
            if (gpt3Service.isNotRateLimited(botId)) {
                useGpt3 = optionalBotEntity.get().getUseGpt3();
                useGpt3 = !useGpt3;
                optionalBotEntity.get().setUseGpt3(useGpt3);
                botEntityRepository.save(optionalBotEntity.get());
                botFatherService.setUseGpt3(botId, useGpt3);
            } else {
                return null;
            }
        }

        return useGpt3;
    }

    public List<BotEntity> findAllBotsExceptMain() {
        return botEntityRepository.findAllByBotTypeIsNot(BotType.MAIN);
    }

    public void addOrRemoveSupportedLang(BotEntity botEntity, LangEnum langEnum) {
        if (!botEntity.getSupportedLang().remove(langEnum)) {
            botEntity.getSupportedLang().add(langEnum);
        }

        if (botEntity.getSupportedLang().size() == 0) {
            botEntity.getSupportedLang().add(LangEnum.ENG);
        }
        botEntityRepository.save(botEntity);
    }

    public void setTempRemoteHelpWithBot(String botId) {
        remoteHelpWithBotId = botId;
    }
}
