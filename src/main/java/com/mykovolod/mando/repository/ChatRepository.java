package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {
    Chat findByTelegramChatIdAndBotId(String chatId, String botId);

    List<Chat> findByBotId(String botId);

    void deleteAllByBotId(String botId);
}
