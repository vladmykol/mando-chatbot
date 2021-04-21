package com.mykovolod.mando.dto;

import lombok.Data;

@Data
public class BotInfo {
    private final String botId;
    private final String name;
    private final String key;
    private boolean debugMode;

    public BotInfo(String botId, String name, String key, boolean debugMode) {
        this.botId = botId;
        this.name = name;
        this.key = key;
        this.debugMode = debugMode;
    }
}
