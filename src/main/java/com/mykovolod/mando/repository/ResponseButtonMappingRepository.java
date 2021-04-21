package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.ResponseButtonMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResponseButtonMappingRepository extends MongoRepository<ResponseButtonMapping, String> {
    void deleteAllByButtonId(String buttonId);

    void deleteAllByIntentDataIdIn(List<String> intentDataIds);

    List<ResponseButtonMapping> findAllByIntentDataId(String intentDataId);

    List<ResponseButtonMapping> findAllByButtonId(String buttonId);

    boolean existsByButtonId(String buttonId);

    boolean existsByButtonIdAndIntentDataId(String buttonId, String intentDataId);

    void deleteByButtonIdIn(List<String> buttonIds);
}
