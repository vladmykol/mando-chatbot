package com.mykovolod.mando.entity;

import com.mykovolod.mando.conts.LangEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;
import java.util.SortedMap;


@Data
@Document
@Builder
public class MessageEntity {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    @Indexed
    private String chatId;

    private LangEnum detectedLang;

    private String inMessage;

    private String outIntentResponse;

    private SortedMap<Double, Set<String>> intentScore;

    private boolean intentDetermined;

    @Indexed
    private String userId;

    @CreatedDate
    private Date createDate;

    @LastModifiedDate
    private Date lastModifiedDate;
}
