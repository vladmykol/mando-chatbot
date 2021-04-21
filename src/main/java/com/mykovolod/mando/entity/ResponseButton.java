package com.mykovolod.mando.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document
@Builder
public class ResponseButton {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    private String targetIntentDataId;

    private String buttonName;

    @Indexed
    private String botId;
}
