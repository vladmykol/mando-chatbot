package com.mykovolod.mando.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document
@Builder
public class Chat {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    @Indexed
    private String telegramChatId;

    @Indexed
    private String botId;

    private String whatsNewMsg;

    private String chatError;

    private String pendingCommand;

    private Boolean isOperatorConnected;
}
