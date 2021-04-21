package com.mykovolod.mando.repository;

import com.mykovolod.mando.conts.BotType;
import com.mykovolod.mando.entity.BotEntity;
import com.mykovolod.mando.entity.BotStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BotEntityRepository extends MongoRepository<BotEntity, String> {
    BotEntity findByOwnerIdAndBotType(String ownerId, BotType botType);

    BotEntity findByIdAndBotType(String botId, BotType botType);

    BotEntity findByBotName(String botName);

    boolean existsByOwnerIdAndBotType(String ownerId, BotType botType);

    List<BotEntity> findAllByBotTypeIsNot(BotType botType);

    List<BotEntity> findByStatus(BotStatus botStatus);

    List<BotEntity> findAllByOrderByStatus();
}
