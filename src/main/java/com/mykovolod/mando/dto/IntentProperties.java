package com.mykovolod.mando.dto;

import lombok.Data;

import java.util.Map;

@Data
public class IntentProperties {
    private String intentName;
    private Map<String, IntentDataProperties> dataPerLang;
}
