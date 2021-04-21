package com.mykovolod.mando.repository;

import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.entity.IntentData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IntentDataRepository extends MongoRepository<IntentData, String> {
    void deleteAllByIntentId(String intentId);

    List<IntentData> findByIntentId(String intentId);

    List<IntentData> findByIntentIdInAndLangIn(List<String> intentIdList, Set<LangEnum> langSet);

    Optional<IntentData> findByIntentIdAndLang(String intentId, LangEnum lang);

    void deleteAllByIntentIdIn(List<String> intentIds);
}
