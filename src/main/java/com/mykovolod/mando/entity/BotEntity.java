package com.mykovolod.mando.entity;

import com.mykovolod.mando.conts.BotType;
import com.mykovolod.mando.conts.LangEnum;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;


@Data
@Document
@Builder
public class BotEntity {
    @Id
    private String id;

    @NotNull
    @Indexed
    private String ownerId;

    private String botName;

    @NotNull
    private BotType botType;

    private String botToken;

    @NotNull
    private BotStatus status;

    private String initError;

    private boolean debugMode;

    private Set<LangEnum> supportedLang;

    private boolean useGpt3;

    @CreatedDate
    private Date createDate;

    @LastModifiedDate
    private Date lastModifiedDate;
}
