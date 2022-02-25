package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MessageEntityRepository extends MongoRepository<MessageEntity, String> {
    List<MessageEntity> findAllByChatId(String chatId);

    long countAllByChatIdIn(List<String> chatIdList);

    long countAllByChatIdInAndCreateDateGreaterThan(List<String> chatIdList, Date date);

    boolean existsByChatIdInAndCreateDateGreaterThan(List<String> chatIdList, Date date);

    List<MessageEntity> findFirst2ByChatIdOrderByCreateDateDesc(String chatId);

    List<MessageEntity> findByChatIdInAndOutIntentResponseIsNotNull(List<String> chatIds, Pageable pageable);

    List<MessageEntity> findByChatIdInAndOutIntentResponseIsNotNullOrderByCreateDateDesc(List<String> chatIds);

   void deleteAllByChatIdIn(List<String> chatIds);
}
