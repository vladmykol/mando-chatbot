package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.entity.Chat;
import com.mykovolod.mando.entity.MessageEntity;
import com.mykovolod.mando.repository.ChatRepository;
import com.mykovolod.mando.repository.MessageEntityRepository;
import com.mykovolod.mando.utils.StringParamUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageEntityRepository messageEntityRepository;
    private final LangBundleService langBundleService;

    public Chat saveChat(String telegramChatId, String botId) {
        final var chat = chatRepository.findByTelegramChatIdAndBotId(telegramChatId, botId);

        if (chat == null) {
            final var newChat = Chat.builder()
                    .botId(botId)
                    .telegramChatId(telegramChatId)
                    .whatsNewMsg(langBundleService.getMessage("bot.main.whats_new", LangEnum.ENG))
                    .build();
            return chatRepository.save(newChat);
        } else {
            return chat;
        }
    }

    public void setPendingCommand(String chatId, String commandName, String... params) {
        final var optionalChat = chatRepository.findById(chatId);
        optionalChat.ifPresent(chat -> {
            final var command = StringParamUtil.constructCommand(commandName, params);
            chat.setPendingCommand(command);
            chatRepository.save(chat);
        });
    }

    public void removePendingCommand(Chat chat) {
        chat.setPendingCommand("");
        chatRepository.save(chat);
    }

    public Chat setConnectOperator(String chatId) {
        final var optionalChat = chatRepository.findById(chatId);
        if (optionalChat.isPresent()) {
            optionalChat.get().setIsOperatorConnected(true);
            return chatRepository.save(optionalChat.get());
        } else {
            return null;
        }
    }

    public boolean isOperatorConnectedToChat(String chatId) {
        final var optionalChat = chatRepository.findById(chatId);
        if (optionalChat.isPresent()) {
            final var isOperatorConnected = optionalChat.get().getIsOperatorConnected();
            if (isOperatorConnected == null) {
                return false;
            } else {
                return isOperatorConnected;
            }
        } else {
            return false;
        }
    }

    public void resetChat(String id) {
        final var optionalChat = chatRepository.findById(id);
        optionalChat.ifPresent(chat -> {
            chat.setIsOperatorConnected(false);
            chatRepository.save(chat);
        });
    }

    public List<String> getBotChatIds(String botId) {
        final var chatList = chatRepository.findByBotId(botId);

        return chatList.stream().map(Chat::getId).collect(Collectors.toList());
    }

    public List<MessageEntity> getLastBotMessagesForUser(String botId, String userId, int pageSize) {
        final var chatIds = getBotChatIds(botId);

        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createDate"));
        return messageEntityRepository.findByChatIdInAndUserIdAndOutIntentResponseIsNotNull(chatIds, userId, pageable);
    }

    public List<MessageEntity> getLastBotMessages(String botId, int pageSize) {
        final var chatIds = getBotChatIds(botId);
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createDate"));
        return messageEntityRepository.findByChatIdInAndOutIntentResponseIsNotNull(chatIds, pageable);
    }

    public List<MessageEntity> getLastAllBotMessages(String botId) {
        final var chatList = chatRepository.findByBotId(botId);

        final var chatIds = chatList.stream()
                .map(Chat::getId)
                .collect(Collectors.toList());
        return messageEntityRepository.findByChatIdInAndOutIntentResponseIsNotNullOrderByCreateDateDesc(chatIds);
    }

    public void deleteAllByBot(String botId) {
        final var chatList = chatRepository.findByBotId(botId);

        final var chatIds = chatList.stream()
                .map(Chat::getId)
                .collect(Collectors.toList());
        messageEntityRepository.deleteAllByChatIdIn(chatIds);

        chatRepository.deleteAllByBotId(botId);
    }
}
