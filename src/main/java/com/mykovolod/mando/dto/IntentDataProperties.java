package com.mykovolod.mando.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntentDataProperties {
    private List<String> examples;
    private List<String> responses;
}
