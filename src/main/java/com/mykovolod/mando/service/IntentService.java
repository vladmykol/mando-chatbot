package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.dto.Intent;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.IntentData;
import com.mykovolod.mando.entity.IntentEntity;
import com.mykovolod.mando.entity.ResponseButton;
import com.mykovolod.mando.repository.BotEntityRepository;
import com.mykovolod.mando.repository.IntentDataRepository;
import com.mykovolod.mando.repository.IntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.doccat.DocumentCategorizerME;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentService {
    private final IntentRepository intentRepository;
    private final BotEntityRepository botEntityRepository;
    private final ResponseButtonsService responseButtonsService;
    private final IntentDataRepository intentDataRepository;
    private final CategorizationService categorizationService;
    private final SentenceDetectService sentenceDetectService;

    public void copyDefaultSettingForNewBot(String newBotId) {
        final var botIntentsToCopy = intentRepository.findByBotId(BotConst.NEW_BOT);
        botIntentsToCopy.forEach(intent -> {
            final var intentDataList = intentDataRepository.findByIntentId(intent.getId());

            intent.setId(null);
            intent.setBotId(newBotId);
            final var newIntent = intentRepository.save(intent);

            intentDataList.forEach(intentData -> {
                intentData.setId(null);
                intentData.setIntentId(newIntent.getId());
            });
            intentDataRepository.saveAll(intentDataList);
        });
    }

    public void presetHiButtons(BotEntity botEntity) {
        final var optionalTargetIntentEntity = intentRepository.findByBotIdAndName(botEntity.getId(), "hi");
        final var optionalAssignToIntentEntity = intentRepository.findByBotIdAndName(botEntity.getId(), BotConst.ON_START_INTENT_ID);

        optionalTargetIntentEntity.ifPresent(targetIntentEntity -> {
            optionalAssignToIntentEntity.ifPresent(assignToIntentEntity -> {

                Map<LangEnum, String> langButtonIdMap = new HashMap<>();
                for (LangEnum value : LangEnum.values()) {
                    String connectButtonId;
                    if (value == LangEnum.FRA) {
                        connectButtonId = responseButtonsService.createNewButton(botEntity.getId(), "\uD83C\uDDEB\uD83C\uDDF7 Bonjour");
                    } else if (value == LangEnum.RUS) {
                        connectButtonId = responseButtonsService.createNewButton(botEntity.getId(), "\uD83C\uDDF7\uD83C\uDDFA Привет");
                    } else if (value == LangEnum.POR) {
                        connectButtonId = responseButtonsService.createNewButton(botEntity.getId(), "\uD83C\uDDF5\uD83C\uDDF9 Olá");
                    } else if (value == LangEnum.ENG) {
                        connectButtonId = responseButtonsService.createNewButton(botEntity.getId(), "\uD83C\uDDFA\uD83C\uDDF8 Hi there");
                    } else {
                        continue;
                    }
                    langButtonIdMap.put(value, connectButtonId);
                }

                presetButtonDetails(targetIntentEntity, assignToIntentEntity, langButtonIdMap);
            });
        });
    }

    private void presetButtonDetails(IntentEntity targetIntentEntity, IntentEntity assignToIntentEntity, Map<LangEnum, String> langButtonIdMap) {
        for (LangEnum langEnum : langButtonIdMap.keySet()) {
            final var optionalTargetIntentData = intentDataRepository.findByIntentIdAndLang(targetIntentEntity.getId(), langEnum);
            Optional<IntentData> optionalAssignToIntentData;
            if (assignToIntentEntity.getName().equals(BotConst.ON_START_INTENT_ID)) {
                optionalAssignToIntentData = intentDataRepository.findByIntentIdAndLang(assignToIntentEntity.getId(), LangEnum.ENG);
            } else {
                optionalAssignToIntentData = intentDataRepository.findByIntentIdAndLang(assignToIntentEntity.getId(), langEnum);
            }
            final var buttonId = langButtonIdMap.get(langEnum);

            optionalTargetIntentData.ifPresent(intentData -> {
                responseButtonsService.setTargetIntentDataId(buttonId, intentData.getId());
            });

            optionalAssignToIntentData.ifPresent(intentData -> {
                responseButtonsService.assignButtonToIntentData(buttonId, intentData.getId());
            });
        }
    }

    public void presetConnectWithOperatorButton(BotEntity botEntity) {
        final var optionalTargetIntentEntity = intentRepository.findByBotIdAndName(botEntity.getId(), BotConst.CONNECT_OPERATOR_INTENT_ID);
        final var optionalAssignToIntentEntity = intentRepository.findByBotIdAndName(botEntity.getId(), BotConst.DEFAULT_RESPONSES_INTENT_ID);

        optionalTargetIntentEntity.ifPresent(targetIntentEntity -> {
            optionalAssignToIntentEntity.ifPresent(assignToIntentEntity -> {

                Map<LangEnum, String> langButtonIdMap = new HashMap<>();
                for (LangEnum value : LangEnum.values()) {
                    String connectButtonId;
                    if (value == LangEnum.UKR) {
                        connectButtonId = responseButtonsService.createNewButton(botEntity.getId(), "Підключити оператора");
                    } else if (value == LangEnum.RUS) {
                        connectButtonId = responseButtonsService.createNewButton(botEntity.getId(), "Подключить оператора");
                    } else {
                        connectButtonId = responseButtonsService.createNewButton(botEntity.getId(), "Connect operator");
                    }
                    langButtonIdMap.put(value, connectButtonId);
                }

                presetButtonDetails(targetIntentEntity, assignToIntentEntity, langButtonIdMap);
            });
        });
    }

    public void updateIntentSamples(IntentData intentData, List<String> samples) {
        intentData.setSamples(samples);
        intentDataRepository.save(intentData);
    }

    public void updateIntentResponses(IntentData intentData, List<String> responses) {
        intentData.setResponses(responses);
        intentDataRepository.save(intentData);
    }

    public IntentData saveIntentData(IntentData intentData) {
        final var optionalIntentData = intentDataRepository.findByIntentIdAndLang(intentData.getIntentId(), intentData.getLang());

        if (optionalIntentData.isPresent()) {
            optionalIntentData.get().setSamples(intentData.getSamples());
            optionalIntentData.get().setResponses(intentData.getResponses());
            return intentDataRepository.save(optionalIntentData.get());
        } else {
            return intentDataRepository.save(intentData);
        }
    }

    public String saveIntent(IntentEntity intentEntity) {
        final var optionalIntent = intentRepository.findByBotIdAndName(intentEntity.getBotId(), intentEntity.getName());

        if (optionalIntent.isPresent()) {
            return optionalIntent.get().getId();
        } else {
            return intentRepository.save(intentEntity).getId();
        }
    }

    public Map<LangEnum, DocumentCategorizerME> trainModel(BotEntity bot) {
        var startTime = Instant.now();
        final var intentsByBot = intentRepository.findByBotId(bot.getId());
        if (intentsByBot.isEmpty()) {
            log.warn("No intents found for bot {}", bot.getBotName());
            return new HashMap<>();
        }
        final var intentIds = intentsByBot.stream()
                .map(IntentEntity::getId)
                .collect(Collectors.toList());

        final var langEnumDocumentCategorizerMEMap = categorizationService.trainModel(intentIds, bot.getSupportedLang());

        Duration timeElapsed = Duration.between(startTime, Instant.now());
        log.info("Training bot <{}> took: {}", bot.getBotName(), DurationFormatUtils.formatDurationHMS(timeElapsed.toMillis()));

        return langEnumDocumentCategorizerMEMap;
    }

    public SortedMap<Double, Set<String>> sortedScoreMapToWithNames(SortedMap<Double, Set<String>> map) {
        SortedMap<Double, Set<String>> intents = new TreeMap<>();
        if (map != null)
            map.forEach((score, intentDataIdList) -> {
                Set<String> categoryNames = new HashSet<>();
                intentDataIdList.forEach(intentDataId -> {
                    final var optionalIntentData = intentDataRepository.findById(intentDataId);
                    optionalIntentData.ifPresent(intentData -> {
                        final var optionalIntentEntity = intentRepository.findById(intentData.getIntentId());
                        optionalIntentEntity.ifPresent(intentEntity -> {
                            categoryNames.add(intentEntity.getName());
                        });
                    });
                });
                intents.put(score, categoryNames);
            });
        return intents;
    }


    public Intent[] detectIntent(BotUpdate botUpdate, LangEnum detectedLang, Map<LangEnum, DocumentCategorizerME> categorizers) {
        final var sentences = sentenceDetectService.detect(botUpdate.getInMessage());
        Intent[] intents = new Intent[sentences.length];

        for (int i = 0; i < sentences.length; i++) {
            Intent intent = detectIntentBySentence(sentences[i], botUpdate, detectedLang, categorizers);
            intents[i] = intent;
        }

        return intents;
    }

    public Optional<ResponseButton> getResponseButton(String message, String botId) {
        return responseButtonsService.getButtonByNameAndBotId(message, botId);
    }

    private Intent detectIntentBySentence(String sentence, BotUpdate botUpdate, LangEnum detectedLang, Map<LangEnum, DocumentCategorizerME> categorizers) {
        final var optionalResponseButton = getResponseButton(sentence, botUpdate.getBotId());

        Intent intent = null;
        if (optionalResponseButton.isPresent()) {
            intent = new Intent(optionalResponseButton.get().getTargetIntentDataId(), detectedLang);
        } else {
            final var intentByName = intentRepository.findByBotIdAndName(botUpdate.getBotId(), sentence);
            if (intentByName.isPresent()) {
                var intentDataOptional = intentDataRepository.findByIntentIdAndLang(intentByName.get().getId(), botUpdate.getUser().getLang());
                if (intentDataOptional.isPresent()) {
                    intent = new Intent(intentDataOptional.get().getId(), botUpdate.getUser().getLang());
                } else {
                    final var optionalBotEntity = botEntityRepository.findById(botUpdate.getBotId());
                    if (optionalBotEntity.isPresent()) {
                        for (LangEnum lang : optionalBotEntity.get().getSupportedLang()) {
                            intentDataOptional = intentDataRepository.findByIntentIdAndLang(intentByName.get().getId(), lang);
                            if (intentDataOptional.isPresent()) {
                                intent = new Intent(intentDataOptional.get().getId(), detectedLang);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (intent == null) {
            final var categorizer = categorizers.get(detectedLang);

            if (categorizer == null) {
                intent = new Intent(detectedLang);
            } else {
                final var allCategories = categorizationService.categorize(categorizer, sentence);
                if (log.isDebugEnabled()) {
                    final var sortedMap = sortedScoreMapToWithNames(allCategories);
                    log.debug("categorize string <{}> \n{}", sentence, sortedMap);
                }
                intent = new Intent(allCategories, detectedLang);
            }
        }

        fulfillResponseByIntent(botUpdate.getBotId(), detectedLang, intent);
        return intent;
    }

    private void fulfillResponseByIntent(String botId, LangEnum detectedLang, Intent intent) {
        Optional<IntentData> optionalIntentData;
        if (intent.getBestIntentDataId() != null) {
            optionalIntentData = getIntentDataById(intent.getBestIntentDataId());
        } else {
            optionalIntentData = getDefaultResponses(botId, detectedLang);
        }

        if (optionalIntentData.isPresent()) {
            final var buttonNames = responseButtonsService.findButtonNamesByIntentDataId(optionalIntentData.get().getId());
            intent.setResponseButtons(buttonNames);

            final var intentName = intentRepository.findById(optionalIntentData.get().getIntentId()).get().getName();
            intent.setName(intentName);

            final var randomResponse = getRandomItem(optionalIntentData.get().getResponses());
            intent.setResponse(randomResponse);
        }
    }

    private Optional<IntentData> getDefaultResponses(String botId, LangEnum langEnum) {
        final var optionalIntentEntity = intentRepository.findByBotIdAndName(botId, BotConst.DEFAULT_RESPONSES_INTENT_ID);

        if (optionalIntentEntity.isPresent()) {
            final var byIntentIdAndLang = intentDataRepository.findByIntentIdAndLang(optionalIntentEntity.get().getId(), langEnum);
            if (byIntentIdAndLang.isEmpty()) {
                final var defaultEngResponse = intentDataRepository.findByIntentIdAndLang(optionalIntentEntity.get().getId(), LangEnum.ENG);

                if (defaultEngResponse.isEmpty()) {
                    return Optional.of(IntentData.builder()
                            .responses(Collections.singletonList("Default responses " + langEnum + " is not set"))
                            .build());
                } else {
                    return defaultEngResponse;
                }
            } else {
                return byIntentIdAndLang;
            }
        } else {
            return Optional.of(IntentData.builder()
                    .responses(Collections.singletonList("Default responses are set"))
                    .build());
        }
    }

    public IntentData getOrCreateIntentDataByIntentIdAndLang(String intentId, LangEnum lang) {
        final var optionalIntentData = intentDataRepository.findByIntentIdAndLang(intentId, lang);

        if (optionalIntentData.isPresent()) {
            return optionalIntentData.get();
        } else {
            final var intentData = IntentData.builder()
                    .intentId(intentId)
                    .lang(lang)
                    .samples(new ArrayList<>())
                    .responses(new ArrayList<>())
                    .build();
            return saveIntentData(intentData);
        }
    }

    public Optional<IntentData> getIntentDataById(String id) {
        return intentDataRepository.findById(id);
    }

    public String getRandomItem(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "empty";
        } else {
            Random rand = new Random();
            return list.get(rand.nextInt(list.size()));
        }
    }

    public List<IntentEntity> findIntentByBotSorted(String botId) {
        final var botIntents = intentRepository.findByBotId(botId);

        return botIntents.stream()
                .sorted(Comparator.comparing(IntentEntity::getName))
                .collect(Collectors.toList());
    }

    public Optional<IntentEntity> findIntentById(String intentId) {
        return intentRepository.findById(intentId);
    }


    public Optional<IntentData> getDataIntentIdByIntentIdAndLang(String intentId, LangEnum langEnum) {
        return intentDataRepository.findByIntentIdAndLang(intentId, langEnum);
    }

    public void deleteIntent(String id) {
        final var optionalIntentEntity = intentRepository.findById(id);
        optionalIntentEntity.ifPresent(intentEntity -> {
            final var intentDataIdList = intentDataRepository.findByIntentId(id).stream()
                    .map(IntentData::getId)
                    .collect(Collectors.toList());

            responseButtonsService.deleteButtonsByBotIdAndIntentDataIds(intentEntity.getBotId(), intentDataIdList);

            intentRepository.deleteById(id);
        });
        intentDataRepository.deleteAllByIntentId(id);
    }

    public String newIntent(String intentName, String botId) {
        final var intent = IntentEntity.builder()
                .name(intentName)
                .botId(botId)
                .build();
        return saveIntent(intent);
    }

    public Optional<IntentData> getOnStartIntentData(BotUpdate botUpdate) {
        final var optionalIntentEntity = intentRepository.findByBotIdAndName(botUpdate.getBotId(), BotConst.ON_START_INTENT_ID);

        if (optionalIntentEntity.isPresent()) {
            var optionalIntentData = intentDataRepository.findByIntentIdAndLang(optionalIntentEntity.get().getId(), LangEnum.ENG);

            if (optionalIntentData.isPresent()) {
                return optionalIntentData;
            }
        }
        botUpdate.addOutEditMessage("cannon find start message intent with name " + BotConst.ON_START_INTENT_ID);
        return Optional.empty();
    }

    public boolean isIntentDataExists(String targetIntentDataId) {
        if (targetIntentDataId == null) {
            return false;
        }
        return intentDataRepository.existsById(targetIntentDataId);
    }

    public boolean renameIntent(String intentId, String newIntentName) {
        final var optionalIntentEntity = intentRepository.findById(intentId);

        if (optionalIntentEntity.isPresent()) {
            final var optionalIntentEntityWithSameName = intentRepository.findByBotIdAndName(optionalIntentEntity.get().getBotId(), newIntentName);
            if (optionalIntentEntityWithSameName.isPresent()) {
                return false;
            } else {
                optionalIntentEntity.get().setName(newIntentName);
                intentRepository.save(optionalIntentEntity.get());
                return true;
            }
        } else {
            return false;
        }

    }

    public List<String> findIntentNamesByIntentDataId(List<String> intentDataIds) {
        final var intentDataIterable = intentDataRepository.findAllById(intentDataIds);
        final var intentIds = StreamSupport.stream(intentDataIterable.spliterator(), true)
                .map(IntentData::getIntentId)
                .collect(Collectors.toList());

        final var intentIterable = intentRepository.findAllById(intentIds);

        return StreamSupport.stream(intentIterable.spliterator(), true)
                .map(IntentEntity::getName)
                .collect(Collectors.toList());
    }

    public void deleteAllByBot(String botId) {
        final var intentsByBot = intentRepository.findByBotId(botId);
        final var intentIds = intentsByBot.stream()
                .map(IntentEntity::getId)
                .collect(Collectors.toList());
        intentDataRepository.deleteAllByIntentIdIn(intentIds);
        intentRepository.deleteAllByBotId(botId);

        responseButtonsService.deleteAllByBot(botId);

    }
}
