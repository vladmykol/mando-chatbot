package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.conts.BotType;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.IntentProperties;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.entity.IntentData;
import com.mykovolod.mando.entity.IntentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DbInitService {
    private final BotEntityService botEntityService;
    private final IntentService intentService;
    @Value("${telegram.bot.name}")
    String mainBotName;
    @Value("${telegram.bot.key}")
    String mainBotKey;
    @Value("${telegram.bot.owner.userid}")
    String mainBotOwnerUserId;

    public void presetInitialBotConfig() throws IOException {
        saveMainBotConfig();

        saveIntentsFromProperties(BotConst.MAIN_BOT_ID);
        saveIntentsFromProperties(BotConst.NEW_BOT);
    }

    private void saveMainBotConfig() {
        final var newBotEntity = BotEntity.builder()
                .id(BotConst.MAIN_BOT_ID)
                .botName(mainBotName)
                .botToken(mainBotKey)
                .ownerId(mainBotOwnerUserId)
                .botType(BotType.MAIN)
                .status(BotStatus.ACTIVE)
                .build();

        botEntityService.saveBot(newBotEntity);
    }

    public void saveIntentsFromProperties(String botId) throws IOException {
        saveIntentsFromProperties(botId, botId);
    }

    public void saveIntentsFromProperties(String folder, String botId) throws IOException {
        final var filePath = "nlp" + File.separator + folder + File.separator + "intent.yaml";

        Yaml yaml = new Yaml(new Constructor(IntentProperties.class));
        try (InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(filePath)) {

            final var intentList = yaml.loadAll(inputStream);
            for (Object object : intentList) {
                IntentProperties intentProperties = (IntentProperties) object;

                final var intentName = intentProperties.getIntentName();
                final var intent = IntentEntity.builder()
                        .name(intentName)
                        .botId(botId)
                        .build();
                var intentId = intentService.saveIntent(intent);

                intentProperties.getDataPerLang().keySet().forEach(intentLang -> {
                    final var langEnum = LangEnum.getEnumOrDefault(intentLang.toUpperCase());
                    final var examples = intentProperties.getDataPerLang().get(intentLang).getExamples();
                    final var responses = intentProperties.getDataPerLang().get(intentLang).getResponses();

                    final var intentData = IntentData.builder()
                            .intentId(intentId)
                            .lang(langEnum)
                            .samples(examples)
                            .responses(responses)
                            .build();
                    intentService.saveIntentData(intentData);
                });
            }
        }
    }
}
