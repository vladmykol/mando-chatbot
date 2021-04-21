package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.BotEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BotDescRepository extends MongoRepository<BotEntity, String> {
}
