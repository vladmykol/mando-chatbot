package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.ResponseButton;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ResponseButtonRepository extends MongoRepository<ResponseButton, String> {
    void deleteAllByBotIdAndTargetIntentDataIdIn(String botId, List<String> targetIntentDataIdList);

    Optional<ResponseButton> findByButtonNameAndBotId(String buttonName, String botId);

    List<ResponseButton> findByBotId(String botId);

    void deleteAllByBotId(String botId);
}
