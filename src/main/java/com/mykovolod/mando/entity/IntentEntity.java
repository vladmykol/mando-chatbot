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
public class IntentEntity {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    @Indexed
    private String botId;

    private String name;
}
