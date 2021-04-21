package com.mykovolod.mando.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DefaultResponseProperties {
    private Map<String, List<String>> defaultPerLang;
}
