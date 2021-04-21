package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.conts.BotConst;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.entity.*;
import com.mykovolod.mando.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Intents implements MainBotAction {

    //    PARAM NAMES
    public static final String BACK_TO_START = "Back to start";
    private static final String EDIT_EXAMPLES = "Edit Examples";
    private static final String EDIT_RESPONSES = "Edit Responses";
    private static final String EDIT_RESPONSE_BUTTONS = "Edit Responses Buttons";
    private static final String UPDATE_ONE_BUTTON = "Update one button";
    private static final String UPDATE_ONE_BUTTON_DELETE = "Delete button";
    private static final String UPDATE_ONE_BUTTON_DELETE_YES = "Delete button yes";
    private static final String UPDATE_ONE_BUTTON_RENAME = "Rename button";
    private static final String UPDATE_ONE_BUTTON_RENAME_FROM_USER = "Button rnm from user";
    private static final String INTENT_UPDATE_OPTIONS = "Intent options";
    private static final String INTENT_UPDATE = "Intent update";
    private static final String INTENT_UPDATE_EXAMPLES_FROM_USER = "Intent update examples from user";
    private static final String INTENT_UPDATE_RESPONSES_FROM_USER = "Intent update responses from user";
    private static final String INTENT_RENAME = "Intent rename";
    private static final String INTENT_DELETE = "Intent delete";
    private static final String INTENT_RENAME_FROM_USER = "Intent rnm from user";
    private static final String INTENT_DELETE_YES = "Intent delete Yes";
    private static final String NEW_INTENT = "New Intent";
    private static final String NEW_BUTTON = "New Button";
    private static final String NEW_BUTTON_FOR_INTENT = "New Button for intent";
    private static final String NEW_BUTTON_FOR_INTENT_FROM_USER = "New Btn from User";
    private static final String NEW_BUTTON_FROM_USER_WITH_INTENT_DIRECT = "NBD";
    private static final String ASSIGN_BUTTON = "BA";
    private static final String ASSIGN_BUTTON_TO = "BAT";
    private static final String UN_ASSIGN_BUTTON = "Un Assign button";
    private static final String UN_ASSIGN_BUTTON_YES = "Un Assign button Yes";
    private static final String NEW_INTENT_NAME_FROM_USER = "New Intent from user";
    private static final String NEW_BUTTON_FROM_USER = "New Btn";
    private static final String NEW_BUTTON_FROM_USER_WITH_LANG = "NBL";
    private static final String NEW_BUTTON_FROM_USER_WITH_INTENT = "NBI";
    private static final String UPDATE_INTENTS = "Update existing Intents";
    private static final String UPDATE_BUTTONS = "Update Buttons";

    private final IntentService intentService;
    private final ResponseButtonsService responseButtonsService;
    private final BotEntityService botEntityService;
    private final BotFatherService botFatherService;
    private final ChatService chatService;
    private final LangBundleService langBundleService;

    @Override
    public String getName() {
        return INTENTS;
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        String intentId;
        String intentDataId;
        String intentLang;
        String botId;
        String buttonName;
        String intentResponseButtonId;
        Optional<IntentEntity> optionalIntent;
        Optional<IntentData> optionalIntentData;
        Optional<BotEntity> optionalBotEntity;
        Optional<ResponseButton> optionalIntentDataResponseButton;
        final var intentText = langBundleService.getMessage("text.intent"
                , botUpdate.getUser().getLang());
        if (botUpdate.hasCommandParams()) {
            final var firstCommandParam = botUpdate.getFirstCommandParam();

            switch (firstCommandParam) {
                case UPDATE_INTENTS:
                    botId = botUpdate.getSecondCommandParam();
                    final var allIntents = intentService.findIntentByBotSorted(botId);

                    final var showIntentMsg = langBundleService.getMessage("bot.main.intent.show",
                            new Object[]{allIntents.size()}
                            , botUpdate.getUser().getLang());
                    final var addNewIntentText = langBundleService.getMessage("text.add_new_intent"
                            , botUpdate.getUser().getLang());

                    final var telegramInlineKeyboard = new TelegramInlineKeyboard(getName());
                    telegramInlineKeyboard.addBackButton(BACK_TO_START);
                    telegramInlineKeyboard.addButton(addNewIntentText, NEW_INTENT, botId);
                    allIntents.forEach(intent -> {
                        telegramInlineKeyboard.addRow();
                        if (intent.getName().equals(BotConst.ON_START_INTENT_ID)) {
                            telegramInlineKeyboard.addButton(intent.getName(), INTENT_UPDATE, intent.getId(), LangEnum.ENG.name());
                        } else {
                            telegramInlineKeyboard.addButton(intent.getName(), INTENT_UPDATE_OPTIONS, intent.getId());
                        }
                    });

                    botUpdate.addOutEditMessage(showIntentMsg)
                            .setKeyBoard(telegramInlineKeyboard.getMarkup());
                    break;

                case UPDATE_BUTTONS:
                    botId = botUpdate.getSecondCommandParam();

                    final var addNewButtonText = langBundleService.getMessage("text.add_new_button"
                            , botUpdate.getUser().getLang());

                    final var updateButtonsKeyboard = new TelegramInlineKeyboard(getName())
                            .addBackButton(BACK_TO_START)
                            .addButton(addNewButtonText, NEW_BUTTON, botId);

                    final var buttonsList = responseButtonsService.findButtonsByBotId(botId);
                    buttonsList.forEach(responseButton -> {
                        if (isButtonValid(responseButton)) {
                            var optionalTargetIntentData = intentService.getIntentDataById(responseButton.getTargetIntentDataId());
                            optionalTargetIntentData.ifPresent(targetIntentData -> {
                                var optionalTargetIntent = intentService.findIntentById(targetIntentData.getIntentId());

                                optionalTargetIntent.ifPresent(targetIntentEntity -> {

                                    updateButtonsKeyboard.addRow();
                                    updateButtonsKeyboard.addButton(responseButton.getButtonName() + " -> " + targetIntentEntity.getName() + " [" + targetIntentData.getLang() + "]",
                                            UPDATE_ONE_BUTTON, responseButton.getId());
                                });
                            });
                        } else {
                            responseButtonsService.deleteById(responseButton.getId());
                        }
                    });

                    final var buttonsUpdateText = langBundleService.getMessage("text.response_buttons.update"
                            , botUpdate.getUser().getLang());

                    botUpdate
                            .addOutEditMessage(buttonsUpdateText)
                            .setKeyBoard(updateButtonsKeyboard.getMarkup());
                    break;

                case INTENT_UPDATE_OPTIONS:
                    intentId = botUpdate.getSecondCommandParam();
                    optionalIntent = intentService.findIntentById(intentId);


                    optionalIntent.ifPresent(intent -> {
                        botUpdate.addOutEditMessage(intentText + " " + intent.getName())
                                .setKeyBoard(intentOptionsButtons(intent, botUpdate.getUser().getLang()));
                    });
                    break;

                case NEW_INTENT:
                    botId = botUpdate.getSecondCommandParam();

                    final var writeNewIntentOrMsg = langBundleService.getMessage("text.write_new_intent_or",
                            new Object[]{STOP}
                            , botUpdate.getUser().getLang());

                    botUpdate
                            .addOutEditMessage(writeNewIntentOrMsg);
                    chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), NEW_INTENT_NAME_FROM_USER, botId);
                    break;

                case NEW_INTENT_NAME_FROM_USER:
                    botId = botUpdate.getSecondCommandParam();
                    var intentName = botUpdate.getThirdCommandParam();

                    if (Strings.isBlank(intentName)) {
                        chatService.removePendingCommand(botUpdate.getChat());
                        break;
                    } else {
                        intentName = intentName.replace(BotConst.SYSTEM_INTENT_IDENTIFIER, "");
                    }

                    intentId = intentService.newIntent(intentName, botId);

                    final var intentIsCreateMsg = langBundleService.getMessage("text.intent_is_created",
                            new Object[]{intentName}
                            , botUpdate.getUser().getLang());
                    final var addDataToIntentMsg = langBundleService.getMessage("text.add_data"
                            , botUpdate.getUser().getLang());

                    final var editIntentOptionButton = new TelegramInlineKeyboard(getName())
                            .addBackButton(UPDATE_INTENTS, botId)
                            .addButton(addDataToIntentMsg, INTENT_UPDATE_OPTIONS, intentId)
                            .addRow()
                            .getMarkup();

                    botUpdate.addOutEditMessage(intentIsCreateMsg)
                            .setKeyBoard(editIntentOptionButton);
                    chatService.removePendingCommand(botUpdate.getChat());
                    break;

                case INTENT_UPDATE:
                    intentId = botUpdate.getSecondCommandParam();
                    intentLang = botUpdate.getThirdCommandParam();

                    optionalIntent = intentService.findIntentById(intentId);

                    var langEnum = LangEnum.getEnumOrDefault(intentLang);


                    optionalIntent.ifPresent(intentEntity -> {
                        var intentData = intentService.getOrCreateIntentDataByIntentIdAndLang(intentEntity.getId(), langEnum);

                        showIntentUpdateView(botUpdate, intentEntity, intentData);

                    });
                    break;

                case EDIT_EXAMPLES:
                    intentDataId = botUpdate.getSecondCommandParam();

                    optionalIntentData = intentService.getIntentDataById(intentDataId);
                    optionalIntent = intentService.findIntentById(optionalIntentData.get().getIntentId());

                    optionalIntentData.ifPresent(intentData -> {
                        optionalIntent.ifPresent(intentEntity -> {

                            final var updatingExamplesMsg = langBundleService.getMessage("bot.main.intent.update.examples",
                                    new Object[]{intentEntity.getName(), intentData.getLang()}
                                    , botUpdate.getUser().getLang());

                            botUpdate.addOutEditMessage(updatingExamplesMsg).setHtmlMode();

                            if (intentData.getSamples() == null || intentData.getSamples().isEmpty()) {
                                final var writeNewIntentMsg = langBundleService.getMessage("bot.main.intent.update.examples.write_new_or",
                                        new Object[]{STOP}
                                        , botUpdate.getUser().getLang());

                                botUpdate.addOutMessage(writeNewIntentMsg);
                            } else {
                                botUpdate.addOutMessage(String.join("\n# ", intentData.getSamples())).disableWebPagePreview();

                                final var editAndWriteNewMsg = langBundleService.getMessage("bot.main.intent.update.examples.edit_write_new_or",
                                        new Object[]{STOP}
                                        , botUpdate.getUser().getLang());

                                botUpdate.addOutMessage(editAndWriteNewMsg);
                            }

                            chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), INTENT_UPDATE_EXAMPLES_FROM_USER, intentDataId);
                        });
                    });
                    break;

                case INTENT_UPDATE_EXAMPLES_FROM_USER:
                    intentDataId = botUpdate.getSecondCommandParam();
                    var exampleText = botUpdate.getThirdCommandParam();

                    chatService.removePendingCommand(botUpdate.getChat());
                    exampleText = StringUtils.stripStart(exampleText, "#");
                    optionalIntentData = intentService.getIntentDataById(intentDataId);
                    optionalIntent = intentService.findIntentById(optionalIntentData.get().getIntentId());
                    optionalBotEntity = botEntityService.findBotById(optionalIntent.get().getBotId());

                    final var newSampleList = List.of(exampleText.split("[\\s]+#[\\s]*"));

                    optionalIntentData.ifPresent(intentData -> {
                        optionalIntent.ifPresent(intentEntity -> {
                            final var oldSampleList = intentData.getSamples();

                            intentService.updateIntentSamples(intentData, newSampleList);

                            try {
                                botFatherService.trainBotById(optionalBotEntity.get().getId());

                                showIntentUpdateView(botUpdate, intentEntity, intentData);
                            } catch (Exception e) {
                                intentService.updateIntentSamples(intentData, oldSampleList);

                                var intentIsUpdatedMsg = langBundleService.getMessage("bot.main.intent.updated.error",
                                        new Object[]{intentEntity.getName(), intentData.getLang(), e.getMessage()}
                                        , botUpdate.getUser().getLang());

                                final InlineKeyboardMarkup updateOptionsButton = constructIntentUpdateOptionsKeyboard(botUpdate, intentData, intentEntity);

                                botUpdate.addOutMessage(intentIsUpdatedMsg)
                                        .setKeyBoard(updateOptionsButton);
                            }
                        });
                    });

                    break;

                case EDIT_RESPONSES:
                    intentDataId = botUpdate.getSecondCommandParam();

                    optionalIntentData = intentService.getIntentDataById(intentDataId);
                    optionalIntent = intentService.findIntentById(optionalIntentData.get().getIntentId());

                    optionalIntentData.ifPresent(intentData -> {
                        optionalIntent.ifPresent(intentEntity -> {

                            final var updatingResponsesMsg = langBundleService.getMessage("bot.main.intent.update.responses",
                                    new Object[]{intentEntity.getName(), intentData.getLang()}
                                    , botUpdate.getUser().getLang());

                            botUpdate.addOutEditMessage(updatingResponsesMsg).setHtmlMode();

                            if (intentData.getResponses() == null || intentData.getResponses().isEmpty()) {
                                final var writeNewResponsesMsg = langBundleService.getMessage("bot.main.intent.update.responses.write_new_or",
                                        new Object[]{STOP}
                                        , botUpdate.getUser().getLang());

                                botUpdate.addOutMessage(writeNewResponsesMsg);
                            } else {
                                botUpdate.addOutMessage(String.join("\n# ", intentData.getResponses())).disableWebPagePreview();

                                final var editAndWriteNewMsg = langBundleService.getMessage("bot.main.intent.update.responses.edit_write_new_or",
                                        new Object[]{STOP}
                                        , botUpdate.getUser().getLang());

                                botUpdate.addOutMessage(editAndWriteNewMsg);
                            }

                            chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), INTENT_UPDATE_RESPONSES_FROM_USER, intentDataId);
                        });
                    });
                    break;

                case INTENT_UPDATE_RESPONSES_FROM_USER:
                    intentDataId = botUpdate.getSecondCommandParam();
                    var responseText = botUpdate.getThirdCommandParam();

                    chatService.removePendingCommand(botUpdate.getChat());
                    responseText = StringUtils.stripStart(responseText, "#");
                    optionalIntentData = intentService.getIntentDataById(intentDataId);
                    optionalIntent = intentService.findIntentById(optionalIntentData.get().getIntentId());

                    final var newResponseList = List.of(responseText.split("[\\s]+#[\\s]*"));

                    optionalIntentData.ifPresent(intentData -> {
                        optionalIntent.ifPresent(intentEntity -> {
                            intentService.updateIntentResponses(intentData, newResponseList);

                            showIntentUpdateView(botUpdate, intentEntity, intentData);
                        });
                    });

                    break;

                case INTENT_DELETE:
                    intentId = botUpdate.getSecondCommandParam();
                    optionalIntent = intentService.findIntentById(intentId);

                    var deleteText = langBundleService.getMessage("text.delete"
                            , botUpdate.getUser().getLang());
                    var yesText = langBundleService.getMessage("text.yes"
                            , botUpdate.getUser().getLang());
                    var nesText = langBundleService.getMessage("text.no"
                            , botUpdate.getUser().getLang());

                    final var yesNotKeyboard = new TelegramInlineKeyboard(getName())
                            .addButton(yesText, INTENT_DELETE_YES, intentId)
                            .addButton(nesText, INTENT_UPDATE_OPTIONS, intentId)
                            .addRow()
                            .getMarkup();

                    optionalIntent.ifPresent(intent -> {
                        botUpdate
                                .addOutEditMessage(deleteText + " " + intentText + " " + intent.getName() + "?")
                                .setKeyBoard(yesNotKeyboard);
                    });
                    break;

                case INTENT_DELETE_YES:
                    intentId = botUpdate.getSecondCommandParam();
                    optionalIntent = intentService.findIntentById(intentId);

                    optionalIntent.ifPresent(intent -> {
                        final var updateIntentOptions = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_INTENTS, intent.getBotId())
                                .getMarkup();

                        var allIntentsByBot = intentService.findIntentByBotSorted(optionalIntent.get().getBotId());
                        int remainingIntents = 0;
                        for (IntentEntity intentEntity : allIntentsByBot) {
                            if (!intentEntity.getId().equals(intentId) && !intentEntity.getName().contains(BotConst.SYSTEM_INTENT_IDENTIFIER)) {
                                remainingIntents++;
                            }
                        }

                        if (remainingIntents < 2) {
                            botUpdate
                                    .addOutEditMessage("Bot should have minim 2 User Intents so not possible to delete this one")
                                    .setKeyBoard(updateIntentOptions);
                        } else {
                            intentService.deleteIntent(intent.getId());

                            var optionalBotEntityToTrain = botEntityService.findBotById(optionalIntent.get().getBotId());

                            var wasDeletedText = langBundleService.getMessage("text.was_deleted"
                                    , botUpdate.getUser().getLang());

                            optionalBotEntityToTrain.ifPresent(botEntity -> {
                                String resultMsg;
                                try {
                                    botFatherService.trainBotById(botEntity.getId());

                                    resultMsg = intentText + " " + intent.getName() + " " + wasDeletedText;
                                } catch (Exception e) {
                                    resultMsg = e.getMessage();
                                }

                                botUpdate
                                        .addOutEditMessage(resultMsg)
                                        .setKeyBoard(updateIntentOptions);
                            });
                        }
                    });

                    break;

                case INTENT_RENAME:
                    intentId = botUpdate.getSecondCommandParam();
                    optionalIntent = intentService.findIntentById(intentId);

                    optionalIntent.ifPresent(intent -> {

                        final var writeNewIntentForRenamingOrMsg = langBundleService.getMessage("text.write_new_intent_or",
                                new Object[]{STOP}
                                , botUpdate.getUser().getLang());

                        botUpdate
                                .addOutEditMessage(writeNewIntentForRenamingOrMsg);
                        chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), INTENT_RENAME_FROM_USER, intentId);
                    });

                    break;

                case INTENT_RENAME_FROM_USER:
                    intentId = botUpdate.getSecondCommandParam();
                    var newIntentName = botUpdate.getThirdCommandParam();
                    chatService.removePendingCommand(botUpdate.getChat());

                    optionalIntent = intentService.findIntentById(intentId);
                    optionalIntent.ifPresent(intent -> {
                        String outMsg;
                        if (intentService.renameIntent(intentId, newIntentName)) {
                            outMsg = langBundleService.getMessage("text.renamed",
                                    new Object[]{STOP}
                                    , botUpdate.getUser().getLang());

                        } else {
                            outMsg = "Intent with this name already exists";
                        }

                        final var backToIntentKeyBoard = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_INTENTS, intent.getBotId())
                                .addRow()
                                .getMarkup();

                        botUpdate
                                .addOutEditMessage(outMsg)
                                .setKeyBoard(backToIntentKeyBoard);
                    });
                    break;

                case EDIT_RESPONSE_BUTTONS:
                    intentDataId = botUpdate.getSecondCommandParam();

                    optionalIntentData = intentService.getIntentDataById(intentDataId);
                    optionalIntent = intentService.findIntentById(optionalIntentData.get().getIntentId());

                    optionalIntentData.ifPresent(intentData -> {
                        optionalIntent.ifPresent(intentEntity -> {

                            final var updatingButtonResponsesMsg = langBundleService.getMessage("bot.main.intent.update.response_buttons",
                                    new Object[]{intentEntity.getName(), intentData.getLang()}
                                    , botUpdate.getUser().getLang());

                            var backToIntentText = langBundleService.getMessage("text.back_to_intent"
                                    , botUpdate.getUser().getLang());

                            final var assignButtonText = langBundleService.getMessage("text.assign_new_button"
                                    , botUpdate.getUser().getLang());

                            final var telegramInlineKeyboardNewButtons = new TelegramInlineKeyboard(getName())
                                    .addButton(backToIntentText, INTENT_UPDATE, intentEntity.getId(), intentData.getLang().name())
                                    .addButton(assignButtonText, ASSIGN_BUTTON, intentData.getId());


                            final var buttonList = responseButtonsService.findButtonsAttachedToIntentDataId(intentDataId);
                            buttonList.forEach(responseButtonMapping -> {
                                final var optionalResponseButton = responseButtonsService.getById(responseButtonMapping.getButtonId());

                                optionalResponseButton.ifPresent(responseButton -> {
                                    if (isButtonValid(responseButton)) {
                                        telegramInlineKeyboardNewButtons.addRow();

                                        telegramInlineKeyboardNewButtons.addButton(responseButton.getButtonName(), UN_ASSIGN_BUTTON, responseButtonMapping.getId());
                                    } else {
                                        responseButtonsService.deleteById(responseButton.getId());
                                    }
                                });
                            });

                            botUpdate.addOutEditMessage(updatingButtonResponsesMsg)
                                    .setHtmlMode()
                                    .setKeyBoard(telegramInlineKeyboardNewButtons.getMarkup());
                        });
                    });
                    break;

                case ASSIGN_BUTTON:
                    intentDataId = botUpdate.getSecondCommandParam();

                    optionalIntentData = intentService.getIntentDataById(intentDataId);
                    optionalIntentData.ifPresent(intentData -> {
                        var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());

                        if (botByOwner != null) {
                            final var buttonsPerBot = responseButtonsService.findButtonsByBotId(botByOwner.getId());

                            var optionalIntentToAssignTo = intentService.findIntentById(intentData.getIntentId());
                            optionalIntentToAssignTo.ifPresent(intentEntity -> {

                                final var selectButtonsText = langBundleService.getMessage("text.select_buttons"
                                        , new Object[]{intentEntity.getName(), intentData.getLang()}
                                        , botUpdate.getUser().getLang());

                                final var addNewButtonText2 = langBundleService.getMessage("text.add_new_button"
                                        , botUpdate.getUser().getLang());

                                final var selectButtonKeyBoard = new TelegramInlineKeyboard(getName())
                                        .addBackButton(EDIT_RESPONSE_BUTTONS, intentDataId)
                                        .addButton(addNewButtonText2, NEW_BUTTON_FOR_INTENT, intentDataId);

                                buttonsPerBot.forEach(responseButton -> {
                                    if (isButtonValid(responseButton)) {

                                        var optionalTargetIntentData = intentService.getIntentDataById(responseButton.getTargetIntentDataId());
                                        optionalTargetIntentData.ifPresent(targetIntentData -> {
                                            var optionalTargetIntent = intentService.findIntentById(targetIntentData.getIntentId());

                                            optionalTargetIntent.ifPresent(targetIntentEntity -> {
                                                if (targetIntentData.getLang().equals(intentData.getLang()) || intentEntity.getName().contains(BotConst.SYSTEM_INTENT_IDENTIFIER)) {
                                                    selectButtonKeyBoard.addRow();
                                                    selectButtonKeyBoard.addButton(responseButton.getButtonName() + " -> " + targetIntentEntity.getName() + " [" + targetIntentData.getLang() + "]", ASSIGN_BUTTON_TO, intentDataId, responseButton.getId());

                                                }
                                            });
                                        });
                                    } else {
                                        responseButtonsService.deleteById(responseButton.getId());
                                    }
                                });

                                botUpdate.addOutEditMessage(selectButtonsText)
                                        .setHtmlMode()
                                        .setKeyBoard(selectButtonKeyBoard.getMarkup());
                            });
                        }
                    });
                    break;

                case ASSIGN_BUTTON_TO:
                    intentDataId = botUpdate.getSecondCommandParam();
                    intentResponseButtonId = botUpdate.getThirdCommandParam();

                    optionalIntentData = intentService.getIntentDataById(intentDataId);
                    responseButtonsService.assignButtonToIntentData(intentResponseButtonId, intentDataId);

                    final var backToUpdateButtons = new TelegramInlineKeyboard(getName())
                            .addBackButton(INTENT_UPDATE, optionalIntentData.get().getIntentId(), optionalIntentData.get().getLang().name())
                            .getMarkup();

                    var buttonAssignedText = langBundleService.getMessage("text.button_assigned"
                            , botUpdate.getUser().getLang());

                    botUpdate.addOutEditMessage(buttonAssignedText)
                            .setHtmlMode()
                            .setKeyBoard(backToUpdateButtons);
                    break;

                case UN_ASSIGN_BUTTON:
                    var buttonMappingId = botUpdate.getSecondCommandParam();

                    final var optionalResponseButtonMapping = responseButtonsService.findResponseButtonMappingById(buttonMappingId);

                    optionalResponseButtonMapping.ifPresent(responseButtonMapping -> {
                        var optionalButton = responseButtonsService.getById(responseButtonMapping.getButtonId());
                        optionalButton.ifPresent(responseButton -> {

                            var yesDeleteText = langBundleService.getMessage("text.yes"
                                    , botUpdate.getUser().getLang());


                            var optionalTargetIntentData = intentService.getIntentDataById(responseButton.getTargetIntentDataId());
                            optionalTargetIntentData.ifPresent(intentData -> {
                                var optionalTargetIntent = intentService.findIntentById(intentData.getIntentId());

                                final var deleteOrBackButton = new TelegramInlineKeyboard(getName())
                                        .addBackButton(EDIT_RESPONSE_BUTTONS, responseButtonMapping.getIntentDataId())
                                        .addButton(yesDeleteText, UN_ASSIGN_BUTTON_YES, buttonMappingId);

                                optionalTargetIntent.ifPresent(intentEntity -> {
                                    var confirmButtonDeletedText = langBundleService.getMessage("bot.main.intent.update.response.button.unassign",
                                            new Object[]{responseButton.getButtonName(), intentEntity.getName() + " [" + intentData.getLang() + "]"}
                                            , botUpdate.getUser().getLang());

                                    botUpdate
                                            .addOutEditMessage(confirmButtonDeletedText)
                                            .setKeyBoard(deleteOrBackButton.getMarkup());

                                });
                            });
                        });

                    });
                    break;

                case UN_ASSIGN_BUTTON_YES:
                    var buttonMappingId2 = botUpdate.getSecondCommandParam();

                    intentDataId = responseButtonsService.getIntendDataIdByMappingId(buttonMappingId2);
                    responseButtonsService.deleteAssignMapping(buttonMappingId2);

                    final var backToIntentButtonsKeyboard = new TelegramInlineKeyboard(getName())
                            .addBackButton(EDIT_RESPONSE_BUTTONS, intentDataId);

                    var buttonUnAssignedText = langBundleService.getMessage("text.button_deleted"
                            , botUpdate.getUser().getLang());

                    botUpdate
                            .addOutEditMessage(buttonUnAssignedText)
                            .setKeyBoard(backToIntentButtonsKeyboard.getMarkup());
                    break;

                case UPDATE_ONE_BUTTON_RENAME:
                    intentResponseButtonId = botUpdate.getSecondCommandParam();

                    optionalIntentDataResponseButton = responseButtonsService.getById(intentResponseButtonId);
                    optionalIntentDataResponseButton.ifPresent(responseButton -> {
                        final var writeRenamedButtonNameOrStop = langBundleService.getMessage("bot.main.intent.response.new_button",
                                new Object[]{STOP}
                                , botUpdate.getUser().getLang());

                        botUpdate
                                .addOutEditMessage(writeRenamedButtonNameOrStop);
                        chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), UPDATE_ONE_BUTTON_RENAME_FROM_USER, responseButton.getId());
                    });
                    break;

                case UPDATE_ONE_BUTTON_RENAME_FROM_USER:
                    intentResponseButtonId = botUpdate.getSecondCommandParam();
                    var newButtonName = botUpdate.getThirdCommandParam();

                    chatService.removePendingCommand(botUpdate.getChat());
                    optionalIntentDataResponseButton = responseButtonsService.getById(intentResponseButtonId);
                    optionalIntentDataResponseButton.ifPresent(responseButton -> {
                        final var backToUpdateButtonsKeyboard = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_BUTTONS, responseButton.getBotId());

                        String msg;
                        if (responseButtonsService.renameButton(responseButton.getId(), newButtonName, responseButton.getBotId())) {
                            msg = langBundleService.getMessage("text.renamed"
                                    , botUpdate.getUser().getLang());
                        } else {
                            msg = "Button with this name already exists";
                        }

                        botUpdate
                                .addOutEditMessage(msg)
                                .setKeyBoard(backToUpdateButtonsKeyboard.getMarkup());
                    });
                    break;

                case NEW_BUTTON_FOR_INTENT:
                    intentDataId = botUpdate.getSecondCommandParam();

                    var writeNewButtonNameOrStop = langBundleService.getMessage("bot.main.intent.response.new_button",
                            new Object[]{STOP}
                            , botUpdate.getUser().getLang());

                    botUpdate
                            .addOutEditMessage(writeNewButtonNameOrStop);
                    chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), NEW_BUTTON_FOR_INTENT_FROM_USER, intentDataId);
                    break;

                case NEW_BUTTON_FOR_INTENT_FROM_USER:
                    intentDataId = botUpdate.getSecondCommandParam();
                    buttonName = botUpdate.getThirdCommandParam();

                    chatService.removePendingCommand(botUpdate.getChat());

                    var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                    if (botByOwner != null) {
                        final var optionalResponseButton = responseButtonsService.getButtonByNameAndBotId(buttonName, botByOwner.getId());

                        if (optionalResponseButton.isPresent()) {
                            final var backToButtons = new TelegramInlineKeyboard(getName())
                                    .addBackButton(ASSIGN_BUTTON, intentDataId)
                                    .getMarkup();

                            botUpdate.addOutEditMessage("This button name is already exists. Select existing or create button with another name")
                                    .setKeyBoard(backToButtons);
                        } else {
                            optionalIntentData = intentService.getIntentDataById(intentDataId);

                            optionalIntentData.ifPresent(intentData -> {
                                final var newButtonId = responseButtonsService.createNewButton(botByOwner.getId(), buttonName);

                                responseButtonsService.assignButtonToIntentData(newButtonId, intentDataId);

                                final var selectIntentForNewButtonMsg = langBundleService.getMessage("bot.main.intent.response.new_button_intent"
                                        , botUpdate.getUser().getLang());

                                final var intentsByBot = intentService.findIntentByBotSorted(botByOwner.getId());

                                final var keyboardWithIntents = new TelegramInlineKeyboard(getName());
                                for (IntentEntity intentEntity : intentsByBot) {

                                    final var intentDataOptional = intentService.getDataIntentIdByIntentIdAndLang(intentEntity.getId(), intentData.getLang());
                                    if (intentDataOptional.isPresent()) {
                                        if (intentDataOptional.get().getResponses() != null && !intentDataOptional.get().getResponses().isEmpty()) {
                                            keyboardWithIntents
                                                    .addRow()
                                                    .addButton(intentEntity.getName() + " [" + intentData.getLang() + "]",
                                                            NEW_BUTTON_FROM_USER_WITH_INTENT_DIRECT, intentDataOptional.get().getId(), newButtonId);
                                        }
                                    }
                                }

                                botUpdate.addOutEditMessage(selectIntentForNewButtonMsg)
                                        .setKeyBoard(keyboardWithIntents.getMarkup());
                            });

                        }
                    }
                    break;

                case NEW_BUTTON:
                    botId = botUpdate.getSecondCommandParam();

                    writeNewButtonNameOrStop = langBundleService.getMessage("bot.main.intent.response.new_button",
                            new Object[]{STOP}
                            , botUpdate.getUser().getLang());

                    botUpdate
                            .addOutEditMessage(writeNewButtonNameOrStop);
                    chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), NEW_BUTTON_FROM_USER, botId);
                    break;

                case NEW_BUTTON_FROM_USER:
                    botId = botUpdate.getSecondCommandParam();
                    buttonName = botUpdate.getThirdCommandParam();

                    chatService.removePendingCommand(botUpdate.getChat());

                    final var optionalResponseButton = responseButtonsService.getButtonByNameAndBotId(buttonName, botId);

                    if (optionalResponseButton.isPresent()) {
                        final var backToButtons = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_BUTTONS, botId)
                                .getMarkup();

                        botUpdate.addOutEditMessage("This button name is already exists. Delete old one first")
                                .setKeyBoard(backToButtons);
                    } else {
                        final var newButtonId = responseButtonsService.createNewButton(botId, buttonName);

                        final var selectIntentForNewButtonMsg = langBundleService.getMessage("bot.main.intent.response.new_button_intent"
                                , botUpdate.getUser().getLang());

                        final var keyboardWithIntents = new TelegramInlineKeyboard(getName());

                        botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                        if (botByOwner != null) {
                            for (LangEnum lang : botByOwner.getSupportedLang()) {
                                keyboardWithIntents
                                        .addButton(lang.name(), NEW_BUTTON_FROM_USER_WITH_LANG, lang.name(), newButtonId);
                            }
                        }

                        botUpdate.addOutEditMessage(selectIntentForNewButtonMsg)
                                .setKeyBoard(keyboardWithIntents.getMarkup());

                    }
                    break;

                case NEW_BUTTON_FROM_USER_WITH_LANG:
                    intentLang = botUpdate.getSecondCommandParam();
                    var buttonId = botUpdate.getThirdCommandParam();

                    final var selectIntentForNewButtonMsg = langBundleService.getMessage("bot.main.intent.response.new_button_intent"
                            , botUpdate.getUser().getLang());

                    final var optionalButton = responseButtonsService.getById(buttonId);

                    if (optionalButton.isPresent()) {
                        final var intentsByBot = intentService.findIntentByBotSorted(optionalButton.get().getBotId());

                        final var keyboardWithIntents = new TelegramInlineKeyboard(getName());
                        for (IntentEntity intentEntity : intentsByBot) {

                            final var intentDataOptional = intentService.getDataIntentIdByIntentIdAndLang(intentEntity.getId(), LangEnum.getEnumOrDefault(intentLang));
                            if (intentDataOptional.isPresent()) {
                                if (intentDataOptional.get().getResponses() != null && !intentDataOptional.get().getResponses().isEmpty()) {
                                    keyboardWithIntents
                                            .addRow()
                                            .addButton(intentEntity.getName() + " [" + intentLang + "]",
                                                    NEW_BUTTON_FROM_USER_WITH_INTENT, intentDataOptional.get().getId(), buttonId);
                                }
                            }
                        }

                        botUpdate.addOutEditMessage(selectIntentForNewButtonMsg)
                                .setKeyBoard(keyboardWithIntents.getMarkup());

                    }
                    break;

                case NEW_BUTTON_FROM_USER_WITH_INTENT:
                    var targetIntentDataId = botUpdate.getSecondCommandParam();
                    var newButtonId = botUpdate.getThirdCommandParam();

                    var optionalButtonToUpdate = responseButtonsService.getById(newButtonId);

                    optionalButtonToUpdate.ifPresent(responseButton -> {

                        responseButtonsService.setTargetIntentDataId(responseButton.getId(), targetIntentDataId);

                        final var backToUpdateKeyboard = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_BUTTONS, responseButton.getBotId())
                                .getMarkup();

                        var buttonCreateText = langBundleService.getMessage("text.button_created"
                                , botUpdate.getUser().getLang());

                        botUpdate.addOutEditMessage(buttonCreateText)
                                .setKeyBoard(backToUpdateKeyboard);
                    });
                    break;

                case NEW_BUTTON_FROM_USER_WITH_INTENT_DIRECT:
                    targetIntentDataId = botUpdate.getSecondCommandParam();
                    newButtonId = botUpdate.getThirdCommandParam();

                    optionalButtonToUpdate = responseButtonsService.getById(newButtonId);
                    optionalButtonToUpdate.ifPresent(responseButton -> {
                        var optionalTargetIntentData = intentService.getIntentDataById(targetIntentDataId);
                        optionalTargetIntentData.ifPresent(targetIntentData -> {
                            var optionalTargetIntent = intentService.findIntentById(targetIntentData.getIntentId());
                            optionalTargetIntent.ifPresent(targetIntentEntity -> {

                                responseButtonsService.setTargetIntentDataId(responseButton.getId(), targetIntentDataId);

                                final var assignedIntentDataIdsByButtonId = responseButtonsService.findAssignedIntentDataIdsByButtonId(responseButton.getId());

                                InlineKeyboardMarkup keyboard;
                                if (assignedIntentDataIdsByButtonId.size() == 1) {
                                    //means only one intent is assigned here for now
                                    assignedIntentDataIdsByButtonId.get(0);
                                    keyboard = new TelegramInlineKeyboard(getName())
                                            .addBackButton(EDIT_RESPONSE_BUTTONS, assignedIntentDataIdsByButtonId.get(0)).getMarkup();
                                } else {
                                    keyboard = constructUpdateIntentOptions(botUpdate, responseButton.getBotId());
                                }

                                var buttonCreateText = langBundleService.getMessage("text.button_created"
                                        , botUpdate.getUser().getLang());

                                botUpdate.addOutEditMessage(buttonCreateText)
                                        .setKeyBoard(keyboard);
                            });
                        });
                    });
                    break;


                case UPDATE_ONE_BUTTON:
                    intentResponseButtonId = botUpdate.getSecondCommandParam();

                    optionalIntentDataResponseButton = responseButtonsService.getById(intentResponseButtonId);
                    optionalIntentDataResponseButton.ifPresent(responseButton -> {

                        var renameText = langBundleService.getMessage("text.rename"
                                , botUpdate.getUser().getLang());

                        final var updateButtonKeyBoard = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_BUTTONS, responseButton.getBotId())
                                .addDeleteButton(UPDATE_ONE_BUTTON_DELETE, responseButton.getId())
                                .addButton(renameText, UPDATE_ONE_BUTTON_RENAME, responseButton.getId());

                        var optionalTargetIntentData = intentService.getIntentDataById(responseButton.getTargetIntentDataId());
                        optionalTargetIntentData.ifPresent(intentData -> {
                            var optionalTargetIntent = intentService.findIntentById(intentData.getIntentId());

                            optionalTargetIntent.ifPresent(intentEntity -> {
                                final var assignedIntentDataIdsByButtonId = responseButtonsService.findAssignedIntentDataIdsByButtonId(responseButton.getId());
                                final var intentNames = intentService.findIntentNamesByIntentDataId(assignedIntentDataIdsByButtonId);
                                final var assignedToIntentNames = String.join(", ", intentNames);

                                var confirmButtonDeletedText = langBundleService.getMessage("bot.main.intent.update.response.button.update",
                                        new Object[]{responseButton.getButtonName(), intentEntity.getName() + " [" + intentData.getLang() + "]", assignedToIntentNames}
                                        , botUpdate.getUser().getLang());

                                botUpdate
                                        .addOutEditMessage(confirmButtonDeletedText)
                                        .setKeyBoard(updateButtonKeyBoard.getMarkup());

                            });
                        });
                    });
                    break;

                case UPDATE_ONE_BUTTON_DELETE:
                    intentResponseButtonId = botUpdate.getSecondCommandParam();

                    optionalIntentDataResponseButton = responseButtonsService.getById(intentResponseButtonId);
                    optionalIntentDataResponseButton.ifPresent(responseButton -> {

                        var yesText2 = langBundleService.getMessage("text.yes"
                                , botUpdate.getUser().getLang());

                        final var updateButtonKeyBoard = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_BUTTONS, responseButton.getBotId())
                                .addButton(yesText2, UPDATE_ONE_BUTTON_DELETE_YES, responseButton.getId());

                        var optionalTargetIntentData = intentService.getIntentDataById(responseButton.getTargetIntentDataId());
                        optionalTargetIntentData.ifPresent(intentData -> {
                            var optionalTargetIntent = intentService.findIntentById(intentData.getIntentId());

                            optionalTargetIntent.ifPresent(intentEntity -> {
                                var confirmButtonDeletedText = langBundleService.getMessage("bot.main.intent.update.response.button.delete",
                                        new Object[]{responseButton.getButtonName(), intentEntity.getName() + " [" + intentData.getLang() + "]"}
                                        , botUpdate.getUser().getLang());

                                botUpdate
                                        .addOutEditMessage(confirmButtonDeletedText)
                                        .setKeyBoard(updateButtonKeyBoard.getMarkup());

                            });
                        });
                    });
                    break;

                case UPDATE_ONE_BUTTON_DELETE_YES:
                    intentResponseButtonId = botUpdate.getSecondCommandParam();

                    optionalIntentDataResponseButton = responseButtonsService.getById(intentResponseButtonId);
                    optionalIntentDataResponseButton.ifPresent(responseButton -> {
                        final var backToUpdateButtonsKeyboard = new TelegramInlineKeyboard(getName())
                                .addBackButton(UPDATE_BUTTONS, responseButton.getBotId());

                        responseButtonsService.deleteById(responseButton.getId());

                        var buttonDeletedText = langBundleService.getMessage("text.button_deleted"
                                , botUpdate.getUser().getLang());

                        botUpdate
                                .addOutEditMessage(buttonDeletedText)
                                .setKeyBoard(backToUpdateButtonsKeyboard.getMarkup());
                    });
                    break;

                case BACK_TO_START:
                    botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                    if (botByOwner != null) {
                        var whatNextText = langBundleService.getMessage("bot.main.train"
                                , botUpdate.getUser().getLang());

                        final var updateIntentOptions = constructUpdateIntentOptions(botUpdate, botByOwner.getId());
                        botUpdate
                                .addOutEditMessage(whatNextText + "\n@" + botByOwner.getBotName())
                                .setKeyBoard(updateIntentOptions);
                    } else {
                        var newBotText = langBundleService.getMessage("bot.main.dont_have_bot"
                                , new Object[]{NEW_BOT}
                                , botUpdate.getUser().getLang());

                        botUpdate.addOutEditMessage(newBotText);
                    }
                    break;

                default:
                    final var noSuchMsg = langBundleService.getMessage("bot.main.command_param.not_found"
                            , botUpdate.getUser().getLang());
                    chatService.removePendingCommand(botUpdate.getChat());
                    botUpdate.addOutEditMessage(noSuchMsg);
            }

        } else {
            final var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
            if (botByOwner != null) {
                var updateMyIntentsMsg = langBundleService.getMessage("bot.main.intent.update"
                        , botUpdate.getUser().getLang());

                final var updateIntentOptions = constructUpdateIntentOptions(botUpdate, botByOwner.getId());
                botUpdate
                        .addOutMessage(updateMyIntentsMsg + "\n@" + botByOwner.getBotName())
                        .setKeyBoard(updateIntentOptions);
            } else {
                var newBotText = langBundleService.getMessage("bot.main.dont_have_bot"
                        , new Object[]{NEW_BOT}
                        , botUpdate.getUser().getLang());

                botUpdate.addOutMessage(newBotText);
            }
        }
    }

    private void showIntentUpdateView(BotUpdate botUpdate, IntentEntity intentEntity, IntentData intentData) {
        final var emptyText = langBundleService.getMessage("text.empty"
                , botUpdate.getUser().getLang());
        final var examplesText = langBundleService.getMessage("text.examples"
                , botUpdate.getUser().getLang());
        final var responsesText = langBundleService.getMessage("text.responses"
                , botUpdate.getUser().getLang());
        final var examplesUpdateText = langBundleService.getMessage("text.examples.update"
                , botUpdate.getUser().getLang());
        final var responsesUpdateText = langBundleService.getMessage("text.responses.update"
                , botUpdate.getUser().getLang());
        final var responseButtonsText = langBundleService.getMessage("text.response_buttons"
                , botUpdate.getUser().getLang());
        final var responseButtonsUpdateText = langBundleService.getMessage("text.button.update_assigned"
                , botUpdate.getUser().getLang());
        var updateIntentsText = langBundleService.getMessage("text.intent"
                , botUpdate.getUser().getLang());

        String samples;
        if (intentData.getSamples() == null || intentData.getSamples().isEmpty()) {
            samples = emptyText;
        } else {
            samples = String.join("\n # ", intentData.getSamples());
        }
        String responses;
        if (intentData.getResponses() == null || intentData.getResponses().isEmpty()) {
            responses = emptyText;
        } else {
            responses = String.join("\n # ", intentData.getResponses());
        }
        final var buttonList = responseButtonsService.findButtonsAttachedToIntentDataId(intentData.getId());
        final StringBuilder stringBuilderButtonNames = new StringBuilder();
        if (buttonList.isEmpty()) {
            stringBuilderButtonNames.append("\n # ").append(emptyText);
        } else {
            for (ResponseButtonMapping responseButtonMapping : buttonList) {
                final var optionalResponseButton = responseButtonsService.getById(responseButtonMapping.getButtonId());
                optionalResponseButton.ifPresent(responseButton -> {
                    stringBuilderButtonNames.append("\n # ").append(responseButton.getButtonName());
                });
            }
        }

        final var updateOptionKeyBoard = new TelegramInlineKeyboard(getName());

        var intentNameText = updateIntentsText + " " + intentEntity.getName();
        if (intentEntity.getName().equals(BotConst.ON_START_INTENT_ID)) {
            updateOptionKeyBoard.addBackButton(UPDATE_INTENTS, intentEntity.getBotId());
        } else {
            updateOptionKeyBoard.addBackButton(INTENT_UPDATE_OPTIONS, intentEntity.getId());
            intentNameText = intentNameText + " [" + intentData.getLang() + "]";
        }
        updateOptionKeyBoard.addRow()
                .addButton(examplesUpdateText, EDIT_EXAMPLES, intentData.getId())
                .addButton(responsesUpdateText, EDIT_RESPONSES, intentData.getId())
                .addRow()
                .addButton(responseButtonsUpdateText, EDIT_RESPONSE_BUTTONS, intentData.getId());

        botUpdate.addOutEditMessage(intentNameText +
                "\n\n" + examplesText + " \n # " + samples +
                "\n\n" + responsesText + " \n # " + responses +
                "\n\n" + responseButtonsText + stringBuilderButtonNames.toString())
                .disableWebPagePreview()
                .setKeyBoard(updateOptionKeyBoard.getMarkup());
    }

    private boolean isButtonValid(ResponseButton responseButton) {
        return intentService.isIntentDataExists(responseButton.getTargetIntentDataId());
    }

    private InlineKeyboardMarkup constructUpdateIntentOptions(BotUpdate botUpdate, String botId) {
        var updateIntentsText = langBundleService.getMessage("text.update_intents"
                , botUpdate.getUser().getLang());
        final var buttonsUpdateText = langBundleService.getMessage("text.response_buttons.update"
                , botUpdate.getUser().getLang());

        return new TelegramInlineKeyboard(getName())
                .addButton(updateIntentsText, UPDATE_INTENTS, botId)
                .addRow()
                .addButton(buttonsUpdateText, UPDATE_BUTTONS, botId)
                .getMarkup();
    }

    private InlineKeyboardMarkup constructIntentUpdateOptionsKeyboard(BotUpdate botUpdate, IntentData intentData, IntentEntity intentEntity) {
        chatService.removePendingCommand(botUpdate.getChat());

        var backToIntentText = langBundleService.getMessage("text.back_to_intent"
                , botUpdate.getUser().getLang());

        return new TelegramInlineKeyboard(getName())
                .addButton(backToIntentText, INTENT_UPDATE, intentEntity.getId(), intentData.getLang().name())
                .getMarkup();
    }

    private InlineKeyboardMarkup intentOptionsButtons(IntentEntity intent, LangEnum userLang) {
        final var telegramInlineKeyboard = new TelegramInlineKeyboard(getName())
                .addBackButton(UPDATE_INTENTS, intent.getBotId())
                .addRow();

        if (!intent.getName().contains(BotConst.SYSTEM_INTENT_IDENTIFIER)) {
            var renameText = langBundleService.getMessage("text.rename"
                    , userLang);
            telegramInlineKeyboard
                    .addButton(renameText, INTENT_RENAME, intent.getId())
                    .addRow()
                    .addDeleteButton(INTENT_DELETE, intent.getId())
                    .addRow();
        }
        final var optionalBotEntity = botEntityService.findBotById(intent.getBotId());
        if (optionalBotEntity.isPresent()) {
            int buttonsInRow = 0;
            for (LangEnum lang : optionalBotEntity.get().getSupportedLang()) {
                telegramInlineKeyboard
                        .addButton(lang.name(), INTENT_UPDATE, intent.getId(), lang.name());
                buttonsInRow++;
                if (buttonsInRow >= 3) {
                    telegramInlineKeyboard.addRow();
                    buttonsInRow = 0;
                }
            }
        }
        return telegramInlineKeyboard.getMarkup();
    }


}
