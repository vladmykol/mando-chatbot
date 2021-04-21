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
public class ResponseButtonMapping {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    @Indexed
    private String buttonId;

    @Indexed
    private String intentDataId;
}
