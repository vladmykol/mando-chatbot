package com.mykovolod.mando.dto;

import lombok.Data;

@Data
public class BotInfo {
    private final String botId;
    private final String name;
    private final String key;
    private final String ownerId;
    private boolean debugMode;
    private boolean useGpt3;

    public BotInfo(String botId, String name, String key, String ownerId, boolean debugMode, boolean useGpt3) {
        this.botId = botId;
        this.name = name;
        this.key = key;
        this.ownerId = ownerId;
        this.debugMode = debugMode;
        this.useGpt3 = useGpt3;
    }
}
