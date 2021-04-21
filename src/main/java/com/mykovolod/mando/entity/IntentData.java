package com.mykovolod.mando.entity;

import com.mykovolod.mando.conts.LangEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.List;


@Data
@Document
@Builder
public class IntentData {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    @Indexed
    private String intentId;

    @NotBlank
    private List<String> samples;

    @NotBlank
    private List<String>  responses;

    private LangEnum lang;
}
