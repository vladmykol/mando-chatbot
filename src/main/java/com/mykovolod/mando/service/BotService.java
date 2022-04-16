package com.mykovolod.mando.service;

import com.mykovolod.mando.bot.action.main.MainBotAction;
import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.dto.Intent;
import com.mykovolod.mando.dto.OutMessage;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.Chat;
import com.mykovolod.mando.entity.MessageEntity;
import com.mykovolod.mando.repository.BotEntityRepository;
import com.mykovolod.mando.repository.MessageEntityRepository;
import com.mykovolod.mando.repository.UserRepository;
import com.mykovolod.mando.utils.StringParamUtil;
import com.mykovolod.mando.utils.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.doccat.DocumentCategorizerME;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotService {
    private final LangDetectService langDetectService;
    private final IntentService intentService;
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final BotEntityRepository botEntityRepository;
    private final BotFatherService botFatherService;
    private final MessageEntityRepository messageEntityRepository;
    private final LangBundleService langBundleService;
    private final Gpt3Service gpt3Service;


    @Value("${telegram.bot.name}")
    @Getter
    private String mainBotName;

    public Intent[] detectIntent(BotUpdate botUpdate, Map<LangEnum, DocumentCategorizerME> categorizers) {
        final var detectedLang = langDetectService.detect(botUpdate.getInMessage());
        var responseIntents = intentService.detectIntent(botUpdate, detectedLang, categorizers);

        //try to make a bot more smart with GPT3
        if (botUpdate.getBotInfo().isUseGpt3() && isBotAlreadyRespondedWithSameMassageRecently(botUpdate.getBotId(), botUpdate.getUser().getId(), responseIntents)) {
            if (gpt3Service.isNotRateLimited(botUpdate.getBotId())) {
                responseIntents = getResponseFromGpt3(botUpdate.getInMessage(), responseIntents, detectedLang);
            } else {
                log.info("Bot {} is rateLimited to use GPT3 today", botUpdate.getBotInfo().getName());
            }
        }

        return responseIntents;
    }

    private Intent[] getResponseFromGpt3(String message, Intent[] responseIntents, LangEnum detectedLang) {
        var context = langBundleService.getMessage("gpt3.context", detectedLang);
        var stopWord = langBundleService.getMessage("gpt3.context.stopword", detectedLang);
        try {
            var gpt3Response = gpt3Service.getAiResponse(context, stopWord, message);
            if (gpt3Response != null) {
                //remove other responses if there are more than 1
                if (responseIntents.length > 1) {
                    responseIntents = ArrayUtils.toArray(responseIntents[0]);
                }
                responseIntents[0].setResponse(gpt3Response);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            log.error("Error from GPT3", e);
        }
        return responseIntents;
    }

    private boolean isBotAlreadyRespondedWithSameMassageRecently(String botId, String userId, Intent[] responseIntents) {
        Stream<String> lastBotMessages = chatService.getLastBotMessagesForUser(botId, userId, 7).stream()
                .map(messageEntity -> messageEntity.getOutIntentResponse().trim());
        List<String> currentBotMessages = Arrays.stream(responseIntents).map(intent -> intent.getResponse().trim())
                .collect(Collectors.toList());

        return lastBotMessages.anyMatch(currentBotMessages::contains);
    }

    public void connectOperatorToChat(BotUpdate botUpdate) {
        chatService.setConnectOperator(botUpdate.getChat().getId());
        resendChatToBotOwner(botUpdate);
    }

    public void removePendingCommand(Chat chat) {
        chatService.removePendingCommand(chat);
    }

    public String getChatIdWithBotOwner(String botId) {
        final var optionalBotEntity = findBotById(botId);

        //chatid and ownerid is the same for chat with one member
        return optionalBotEntity.map(BotEntity::getOwnerId).orElse(null);
    }

    public void resendChatToBotOwner(BotUpdate botUpdate) {
        final var chatIdToResend = botUpdate.getChat().getId();
        final var targetTelegramChatId = getChatIdWithBotOwner(botUpdate.getBotId());

        if (targetTelegramChatId != null) {
            final var chatMessages = messageEntityRepository.findAllByChatId(chatIdToResend);
            final SendDocument sendDocument = getSendDocumentWithMessages("chat_history", chatMessages, false);

            var optionalUser = userRepository.findById(botUpdate.getUser().getId());
            String userName;
            userName = optionalUser.map(user -> user.getFirstName() + " " + user.getLastName()).orElse("unknown");

            final var lastMessageId = chatMessages.get(chatMessages.size() - 1).getId();
            var connectOperatorMessage = langBundleService.getMessage("bot.main.operator.connect"
                    , new Object[]{userName}
                    , botUpdate.getUser().getLang());
            connectOperatorMessage = connectOperatorMessage + "\r\n" + StringParamUtil.constructIdInMsg(lastMessageId); //Important: by this msg id we will find it where admins wants to replay

            botFatherService.sendMessageByMainBot(targetTelegramChatId, connectOperatorMessage, sendDocument);
        }
    }

    private SendDocument getSendDocumentWithMessages(String docName, List<MessageEntity> chatMessages, boolean isIncludeDebugInfo) {
        final var stringBuilder = new StringBuilder();

        chatMessages.forEach(messageEntity -> {
            constructMessage(stringBuilder, messageEntity, isIncludeDebugInfo);
            stringBuilder.append("\r\n-------end---------\r\n\r\n\r\n");
        });

        final var inputFile = new InputFile();
        final var inputStream = IOUtils.toInputStream(stringBuilder.toString(), StandardCharsets.UTF_8);
        inputFile.setMedia(inputStream, docName + ".txt");
        final var sendDocument = new SendDocument();
        sendDocument.setDocument(inputFile);
        return sendDocument;
    }


    private void constructMessage(StringBuilder stringBuilder, MessageEntity messageEntity, boolean isIncludeDebugInfo) {
        var optionalUser = userRepository.findById(messageEntity.getUserId());
        optionalUser.ifPresentOrElse(user -> {
            stringBuilder.append(user.getFirstName()).append(" ").append(user.getLastName());
        }, () -> {
            stringBuilder.append("unknown");
        });

        stringBuilder.append("\r\n\uD83D\uDCAC ").append(messageEntity.getInMessage());
        if (messageEntity.getOutIntentResponse() != null) {
            stringBuilder.append("\r\n------\uD83E\uDD16 Bot------\r\n").append(StringUtils.abbreviate(messageEntity.getOutIntentResponse(), "...\r\n", 200));
        }


        if (isIncludeDebugInfo) {
            stringBuilder.append("------details-------");
            if (messageEntity.getCreateDate() != null) {
                stringBuilder
                        .append("\r\nTime since: ").append(TimeUtils.timeSince(messageEntity.getCreateDate().toInstant()));
            }

            if (messageEntity.getIntentScore() != null) {
                stringBuilder
                        .append("\r\nIntent score: ").append(intentService.sortedScoreMapToWithNames(messageEntity.getIntentScore()))
                        .append("\r\nDetected lang: ").append(messageEntity.getDetectedLang());
            }
        }

    }

    public void resendSimpleMessageToBotOwner(String messageText, String botId) throws TelegramApiException {
        final var telegramChatId = getChatIdWithBotOwner(botId);

        if (telegramChatId != null) {
            botFatherService.sendSimpleMessageByMainBot(telegramChatId, messageText);
        }
    }

    public void resendMessageToBotOwner(MessageEntity messageEntity, String botId) {
        final var telegramChatId = getChatIdWithBotOwner(botId);

        if (telegramChatId != null) {
            final var stringBuilder = new StringBuilder();

            constructMessage(stringBuilder, messageEntity, false);
            stringBuilder
                    .append("\r\n").append(StringParamUtil.constructIdInMsg(messageEntity.getId())); //Important: by this msg id we will find it where admins wants to replay

            botFatherService.sendMessageByMainBot(telegramChatId, stringBuilder.toString());
        }
    }

    public void replayToUser(String msgId, BotUpdate botUpdate) {
        final var optionalMessageEntity = messageEntityRepository.findById(msgId);

        optionalMessageEntity.ifPresent(messageEntity -> {
            boolean isOperatorConnected = chatService.isOperatorConnectedToChat(messageEntity.getChatId());
            if (!isOperatorConnected) {
                botUpdate.addOutMessage("You are connected as Operator." +
                        "\nNow bot will redirect all message from this user to you");
            }

            final var chat = chatService.setConnectOperator(messageEntity.getChatId());
            botFatherService.sendMessageBySupportBot(chat.getBotId(), chat.getTelegramChatId(), botUpdate.getInMessage());
        });
    }

    public void setUserPreferredLang(String userId, LangEnum langEnum) {
        var optionalUser = userRepository.findById(userId);

        optionalUser.ifPresent(user -> {
            user.setPreferredLang(langEnum);
            userRepository.save(user);
        });
    }

    public Optional<com.mykovolod.mando.entity.User> findUserById(String id) {
        return userRepository.findById(id);
    }

    public com.mykovolod.mando.entity.User saveUser(User inUser) {
        var optionalUser = userRepository.findById(String.valueOf(inUser.getId()));

        com.mykovolod.mando.entity.User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.setUserName(inUser.getUserName());
            user.setFirstName(inUser.getFirstName());
            user.setLastName(inUser.getLastName());
            user.setAccountLang(LangEnum.valueOf2LetterLang(inUser.getLanguageCode()));
        } else {
            user = com.mykovolod.mando.entity.User.builder()
                    .id(String.valueOf(inUser.getId()))
                    .userName(inUser.getUserName())
                    .firstName(inUser.getFirstName())
                    .lastName(inUser.getLastName())
                    .accountLang(LangEnum.valueOf2LetterLang(inUser.getLanguageCode()))
                    .build();
        }

        return userRepository.save(user);
    }


    public Map<LangEnum, DocumentCategorizerME> trainCategorizers(String botId) {
        final var optionalBotEntity = botEntityRepository.findById(botId);
        if (optionalBotEntity.isPresent()) {
            return intentService.trainModel(optionalBotEntity.get());
        } else {
            throw new RuntimeException("Bot with ID " + botId + " is not found");
        }
    }

    public Optional<BotEntity> findBotById(String botId) {
        return botEntityRepository.findById(botId);
    }

    public Chat saveChat(String telegramChatId, String botId) {
        return chatService.saveChat(telegramChatId, botId);
    }

    public void addOutMessageWithDefaultCommands(BotUpdate botUpdate) {
        final var commandMsg = langBundleService.getMessage("bot.main.commands",
                new Object[]{MainBotAction.INTENTS, MainBotAction.SET, MainBotAction.STAT, MainBotAction.HISTORY},
                botUpdate.getUser().getLang());
        botUpdate.addOutMessage(commandMsg).setHtmlMode();
    }

    public MessageEntity saveMessage(MessageEntity messageEntity) {
        return messageEntityRepository.save(messageEntity);
    }

    public void updateMessageWithIntent(MessageEntity messageEntity, Intent intent) {
        messageEntity.setIntentScore(intent.getAllScoredIntentDataIds());
        messageEntity.setIntentDetermined(intent.getBestIntentDataId() != null);
        messageEntity.setDetectedLang(intent.getDetectedLang());
        messageEntity.setOutIntentResponse(intent.getResponse());
        messageEntityRepository.save(messageEntity);
    }

    public void updateMessageWithResponse(MessageEntity messageEntity, List<OutMessage> messages) {
        StringBuilder stringBuilder = new StringBuilder();
        messages.forEach(outMessage -> {
            stringBuilder.append(outMessage.getText()).append("\r\n");
        });
        messageEntity.setOutIntentResponse(stringBuilder.toString());
        messageEntityRepository.save(messageEntity);
    }

    public void deleteBot(String botId) {
        botFatherService.stopBot(botId);
        chatService.deleteAllByBot(botId);
        intentService.deleteAllByBot(botId);
        botEntityRepository.deleteById(botId);
    }

    public void addNotFoundCommandMsg(BotUpdate botUpdate) {
        if (botUpdate.getBotInfo().getOwnerId().equals(botUpdate.getUser().getId())) {
            botUpdate.addOutMessage(langBundleService.getMessage("text.command.not_found.use_main_bot"
                    , botUpdate.getUser().getLang())).setHtmlMode();
        } else {
            botUpdate.addOutMessage(langBundleService.getMessage("text.command.not_found"
                    , botUpdate.getUser().getLang()));
        }
    }

    public void resendLastBotMessages(BotUpdate botUpdate, List<MessageEntity> chatMessages, String botName, String suffix) {
        if (chatMessages != null && !chatMessages.isEmpty()) {
            final SendDocument sendDocument = getSendDocumentWithMessages(botName + suffix, chatMessages, true);

            botUpdate.addOutDocument(sendDocument);
        } else {
            var emptyText = langBundleService.getMessage("text.empty"
                    , botUpdate.getUser().getLang());

            botUpdate
                    .addOutMessage(botName + "\n\n \uD83D\uDCAC  " + emptyText);

        }
    }

    public MessageEntity getSavedMessage(String messageId) {
        final var optionalMessageEntity = messageEntityRepository.findById(messageId);
        if (optionalMessageEntity.isPresent()) {
            return optionalMessageEntity.get();
        } else {
            throw new RuntimeException("Message with id=" + messageId + " is not found");
        }
    }

    public boolean isBotInUse(String botId) {
        final var chatIds = chatService.getBotChatIds(botId);

        Date maxDaysBefore = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15));
        return messageEntityRepository.existsByChatIdInAndCreateDateGreaterThan(chatIds, maxDaysBefore);
    }

    public boolean isConnectOperatorFeatureInUse(String chatId) {
        var lastMessages = messageEntityRepository.findFirst2ByChatIdOrderByCreateDateDesc(chatId);
        var maxTimeSinceLastMessage = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(4));
        return maxTimeSinceLastMessage.before(lastMessages.get(1).getCreateDate());
    }

    public void disconnectOperator(String chatId) {
        chatService.resetChat(chatId);
    }

    public boolean isButton(String message, String botId) {
        return intentService.getResponseButton(message, botId).isPresent();
    }

}
