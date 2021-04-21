package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.IntentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IntentRepository extends MongoRepository<IntentEntity, String> {
    List<IntentEntity> findByBotId(String botId);

    Optional<IntentEntity> findByBotIdAndName(String botId, String name);

    void deleteAllByBotId(String botId);
}
