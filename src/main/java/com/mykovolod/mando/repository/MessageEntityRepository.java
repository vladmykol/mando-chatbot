package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface MessageEntityRepository extends MongoRepository<MessageEntity, String> {
    List<MessageEntity> findAllByChatId(String chatId);

    long countAllByChatIdIn(List<String> chatIdList);

    long countAllByChatIdInAndCreateDateGreaterThan(List<String> chatIdList, Date date);

    boolean existsByChatIdInAndCreateDateGreaterThan(List<String> chatIdList, Date date);

    List<MessageEntity> findByChatIdInAndOutIntentResponseIsNotNull(List<String> chatIds, Pageable pageable);

   void deleteAllByChatIdIn(List<String> chatIds);
}
