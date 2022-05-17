package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.BotStatus;
import com.mykovolod.mando.entity.User;
import com.mykovolod.mando.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.regex.Pattern;

import static com.mykovolod.mando.bot.action.main.Intents.BACK_TO_START;

@Service
@RequiredArgsConstructor
public class Set implements MainBotAction {
    public static final String BOT_NAME = "Bot name";
    public static final String API_KEY = "Bot api key";
    public static final String CHANGE_EXISTING_TELEGRAM_BOT = "Change existing";
    public static final String CONTROL_ANOTHER_BOT = "Ctrl another bot";
    public static final String CONTROL_ANOTHER_BOT_ID = "Ctrl another bot name";
    public static final String START_BOT_AGAIN = "Start bot again";
    public static final String CHANGE_EXISTING_VIBER_BOT = "Change existing viber";
    public static final String BOT_LANG = "Bot Lang";
    public static final String AI_ASSIST = "AI Assist";
    private static final String DEBUG_MODE = "Debug mode";
    private final BotFatherService botFatherService;
    private final BotEntityService botEntityService;
    private final ChatService chatService;
    private final LangBundleService langBundleService;

    @Value("${telegram.bot.owner.userid}")
    String mainBotOwnerUserId;

    @Override
    public String getName() {
        return SET;
    }

    @Override
    public void handle(BotUpdate botUpdate) {

        if (botUpdate.hasCommandParams()) {
            final var commandFromUser = botUpdate.getFirstCommandParam();

            switch (commandFromUser) {
                case BOT_LANG:
                    var botId = botUpdate.getSecondCommandParam();
                    var inputLang = botUpdate.getThirdCommandParam();

                    final var optionalBotEntity = botEntityService.findBotById(botId);
                    optionalBotEntity.ifPresent(botEntity -> {
                        if (inputLang != null) {
                            final var langEnum = LangEnum.getEnum(inputLang);
                            if (langEnum != null) {
                                botEntityService.addOrRemoveSupportedLang(botEntity, langEnum);
                            }
                        }

                        final var reconnectTelegramKeyBoard = new TelegramInlineKeyboard(getName())
                                .addBackButton("back");

                        for (LangEnum lang : LangEnum.values()) {
                            reconnectTelegramKeyBoard.addRow();
                            if (botEntity.getSupportedLang().contains(lang)) {
                                reconnectTelegramKeyBoard.addButton("-  " + lang.name(), BOT_LANG, botId, lang.name());
                            } else {
                                reconnectTelegramKeyBoard.addButton("+  " + lang.name(), BOT_LANG, botId, lang.name());
                            }
                        }

                        StringBuilder currentSelection = new StringBuilder();
                        for (LangEnum langEnum : botEntity.getSupportedLang()) {
                            currentSelection.append("\n # ").append(langEnum);
                        }

                        final var langSelect = langBundleService.getMessage("bot.main.set.lang",
                                new Object[]{currentSelection.toString()}
                                , botUpdate.getUser().getLang());


                        botUpdate.addOutEditMessage(langSelect)
                                .setKeyBoard(reconnectTelegramKeyBoard.getMarkup());
                    });
                    break;

                case CHANGE_EXISTING_TELEGRAM_BOT:

                    final var resetBotName = langBundleService.getMessage("bot.main.set.new_name"
                            , botUpdate.getUser().getLang());

                    botUpdate.addOutEditMessage(resetBotName).setHtmlMode();
                    chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), BOT_NAME);
                    break;

                case BOT_NAME:
                    var botNameFromUser = botUpdate.getSecondCommandParam();
                    if (botNameFromUser.startsWith("@")) {
                        botNameFromUser = botNameFromUser.substring(1);
                    }
                    if (botNameFromUser.contains("/")) {
                        botNameFromUser = botNameFromUser.substring(botNameFromUser.lastIndexOf("/")+1);
                    }
                    if (!Pattern.matches("^[^@\\s+][\\S]{3,}(?i)(bot)$", botNameFromUser)) {
                        final var invalidBotName = langBundleService.getMessage("bot.main.set.name.invalid"
                                , botUpdate.getUser().getLang());

                        final var buttonReconnectTelegram = langBundleService.getMessage("bot.main.set.button.reconnect.telegram"
                                , botUpdate.getUser().getLang());
                        final var reconnectTelegramKeyBoard = new TelegramInlineKeyboard(getName())
                                .addButton("\uD83D\uDD0C  " + buttonReconnectTelegram, CHANGE_EXISTING_TELEGRAM_BOT)
                                .getMarkup();

                        botUpdate.addOutMessage(invalidBotName)
                                .setKeyBoard(reconnectTelegramKeyBoard);

                    } else {
                        final var botNameUpdated = langBundleService.getMessage("bot.main.set.name.updated"
                                , botUpdate.getUser().getLang());

                        botEntityService.newBotOrUpdateExisting(botUpdate.getUser(), botNameFromUser);
                        botUpdate.addOutMessage(botNameUpdated);
                        chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), API_KEY);
                    }
                    break;

                case API_KEY:
                    final var apiKeyFromUser = botUpdate.getSecondCommandParam();
                    final var botEntity = botEntityService.newBotTelegramApiKey(botUpdate.getUser().getId(), apiKeyFromUser);
                    chatService.removePendingCommand(botUpdate.getChat());
                    botFatherService.startBot(botEntity);
                    botFatherService.notifyMainBotOwner(botEntity.getBotName() + " was connected to Telegram with msg: " + botEntity.getInitError());
                    if (botEntity.getInitError() == null) {
                        final var botStarted = langBundleService.getMessage("bot.main.set.ok",
                                new Object[]{botEntity.getBotName()}
                                , botUpdate.getUser().getLang());
                        final var buttonLang = langBundleService.getMessage("bot.main.set.button.lang"
                                , botUpdate.getUser().getLang());
                        final var buttonTrain = langBundleService.getMessage("bot.main.set.button.train"
                                , botUpdate.getUser().getLang());


                        final var telegramKeyboard = new TelegramInlineKeyboard()
                                .command(INTENTS).addButton("\uD83D\uDC68\u200D\uD83C\uDF93  " + buttonTrain, BACK_TO_START)
                                .command(getName()).addButton("\uD83C\uDFF3  " + buttonLang, BOT_LANG, botEntity.getId())
                                .getMarkup();
                        botUpdate.addOutMessage(botStarted)
                                .setKeyBoard(telegramKeyboard);

                    } else {
                        showErrorWhileConnectingToTelegram(botUpdate, botEntity.getInitError());
                    }
                    break;

                case DEBUG_MODE:
                    botId = botUpdate.getSecondCommandParam();
                    final var isDebug = botEntityService.setBotDebugMode(botId);
                    if (isDebug) {
                        botUpdate.addOutEditMessage("Debug mode is ENABLED!" +
                                "\n\nWhen your bot in debug mode, you will see every response that your bot gives in real time. If you replay to that message, it will be redirected to user");
                    } else {
                        botUpdate.addOutEditMessage("Debug mode is DISABLED" +
                                "\n\nYou should receive only message that your bot was not able to answer. If you replay to that message, it will be redirected to user");
                    }
                    break;

                case AI_ASSIST:
                    botId = botUpdate.getSecondCommandParam();
                    final var isUseGpt3 = botEntityService.setUseGpt3(botId);
                    String message = "";
                    String botLink = "";
                    if (isUseGpt3 == null) {
                        message = langBundleService.getMessage("bot.main.set.gpt3.limit", botUpdate.getUser().getLang());
                    } else if (isUseGpt3) {
                        message = langBundleService.getMessage("bot.main.set.gpt3.enabled", botUpdate.getUser().getLang());
                        var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                        if (botByOwner != null) {
                            botLink = langBundleService.getMessage("bot.main.bot.link",
                                    new Object[]{botByOwner.getBotName()}, botUpdate.getUser().getLang());
                        }
                    } else {
                        message = langBundleService.getMessage("bot.main.set.gpt3.disabled", botUpdate.getUser().getLang());
                    }
                    botUpdate.addOutEditMessage(message + botLink).setHtmlMode();
                    break;

                case CHANGE_EXISTING_VIBER_BOT:
                    botUpdate.addOutEditMessage(langBundleService.getMessage("bot.in.viber.not_implemented"
                            , botUpdate.getUser().getLang()));
                    break;

                case CONTROL_ANOTHER_BOT:
                    final var allBotsExceptMain = botEntityService.findAllBotsExceptMain();
                    final var allBotButtons = new TelegramInlineKeyboard(getName());

                    if (botUpdate.getUser().getId().equals(mainBotOwnerUserId)) {
                        allBotsExceptMain.forEach(botEnt -> {
                            allBotButtons.addButton(botEnt.getBotName(), CONTROL_ANOTHER_BOT_ID, botEnt.getId()).addRow();
                        });

                        botUpdate.addOutEditMessage("Select bot to control temporally")
                                .setKeyBoard(allBotButtons.getMarkup());
                    }
                    break;
                case CONTROL_ANOTHER_BOT_ID:
                    botId = botUpdate.getSecondCommandParam();

                    if (botUpdate.getUser().getId().equals(mainBotOwnerUserId)) {
                        botEntityService.setTempRemoteHelpWithBot(botId);
                    }

                    botUpdate.addOutEditMessage("Done")
                            .setKeyBoard(new TelegramInlineKeyboard(getName()).addBackButton("back").getMarkup());

                    break;


                case START_BOT_AGAIN:
                    final var existingBotByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                    if (existingBotByOwner != null) {
                        botFatherService.unFreezeBot(existingBotByOwner);

                        if (existingBotByOwner.getInitError() == null) {
                            botUpdate.addOutEditMessage("Your bot @" + existingBotByOwner.getBotName() + " is ONLINE again")
                                    .setKeyBoard(addChangeBotButton(existingBotByOwner, botUpdate.getUser()));
                        } else {
                            showErrorWhileConnectingToTelegram(botUpdate, existingBotByOwner.getInitError());
                        }
                    }
                    break;
                default:
                    chatService.removePendingCommand(botUpdate.getChat());
                    final var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
                    if (botByOwner != null) {
                        final var status = botFatherService.getBotRunningStatusById(botByOwner.getId());
                        botUpdate.addOutEditMessage("Your bot @" + botByOwner.getBotName() + "\nStatus: " + status)
                                .setKeyBoard(addChangeBotButton(botByOwner, botUpdate.getUser()));
                    }
            }
        } else {
            final var botByOwner = botEntityService.findSupportBotByOwner(botUpdate.getUser().getId());
            if (botByOwner != null) {
                if (botByOwner.getStatus().equals(BotStatus.FROZEN)) {
                    botUpdate.addOutMessage("Your bot @" + botByOwner.getBotName() + " is frozen as no one use it for a long time")
                            .setKeyBoard(addActivateButton(botByOwner, botUpdate.getUser()));
                } else {
                    final var status = botFatherService.getBotRunningStatusById(botByOwner.getId());
                    botUpdate.addOutMessage("Your bot @" + botByOwner.getBotName() + "\nStatus: " + status)
                            .setKeyBoard(addChangeBotButton(botByOwner, botUpdate.getUser()));
                }
            } else {
                final var resetBotName = langBundleService.getMessage("bot.main.set.new_name"
                        , botUpdate.getUser().getLang());

                botUpdate.addOutMessage(resetBotName).setHtmlMode();
                chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), BOT_NAME);
            }
        }
    }

    private void showErrorWhileConnectingToTelegram(BotUpdate botUpdate, String errorMsg) {
        final var buttonReconnectTelegram = langBundleService.getMessage("bot.main.set.button.reconnect.telegram"
                , botUpdate.getUser().getLang());
        final var tryToReconnect = langBundleService.getMessage("bot.main.set.telegram.error",
                new Object[]{errorMsg}
                , botUpdate.getUser().getLang());

        final var reconnectTelegramKeyBoard = new TelegramInlineKeyboard(getName())
                .addButton("\uD83D\uDD0C  " + buttonReconnectTelegram, CHANGE_EXISTING_TELEGRAM_BOT)
                .getMarkup();

        botUpdate.addOutMessage(tryToReconnect)
                .setKeyBoard(reconnectTelegramKeyBoard);
    }

    public InlineKeyboardMarkup addChangeBotButton(BotEntity botByOwner, User user) {
        final var buttonReconnectTelegram = langBundleService.getMessage("bot.main.set.button.reconnect.telegram"
                , user.getLang());
        final var buttonReconnectViber = langBundleService.getMessage("bot.main.set.button.reconnect.viber"
                , user.getLang());
        final var aiAssist = langBundleService.getMessage("bot.main.set.button.gpt3.assist"
                , user.getLang());
        final var buttonLang = langBundleService.getMessage("bot.main.set.button.lang"
                , user.getLang());
        final var buttonTrain = langBundleService.getMessage("bot.main.set.button.train"
                , user.getLang());

        final var keyboard = new TelegramInlineKeyboard(getName())
                .command(INTENTS).addButton("\uD83D\uDC68\u200D\uD83C\uDF93  " + buttonTrain, BACK_TO_START)
                .addRow()
                .command(getName()).addButton("\uD83D\uDD0C  " + buttonReconnectTelegram, CHANGE_EXISTING_TELEGRAM_BOT)
                .addRow()
                .command(getName()).addButton("\uD83D\uDD0C  " + buttonReconnectViber, CHANGE_EXISTING_VIBER_BOT)
                .addRow()
                .command(getName()).addButton((botByOwner.isDebugMode() ? "Disable debug mode" : "Enable debug mode"), DEBUG_MODE, botByOwner.getId())
                .addRow()
                .command(getName()).addButton("\uD83E\uDDE0  " + aiAssist, AI_ASSIST, botByOwner.getId())
                .addRow()
                .command(getName()).addButton("\uD83C\uDFF3  " + buttonLang, BOT_LANG, botByOwner.getId())
                .addRow()
                .command(DESTROY).addButton("⛔️ Delete this bot", "");

        if (user.getId().equals(mainBotOwnerUserId)) {
            keyboard.addRow().command(getName()).addButton("\uD83D\uDE0E️ Control another bot", CONTROL_ANOTHER_BOT);
        }

        return keyboard.getMarkup();
    }

    public InlineKeyboardMarkup addActivateButton(BotEntity botByOwner, User user) {
        final var keyboard = new TelegramInlineKeyboard(getName())
                .addButton("⚡️  Start bot", START_BOT_AGAIN, botByOwner.getId());

        if (user.getId().equals(mainBotOwnerUserId)) {
            keyboard.addRow().command(getName()).addButton("\uD83D\uDE0E️ Control another bot", CONTROL_ANOTHER_BOT);
        }

        return keyboard.getMarkup();
    }
}
