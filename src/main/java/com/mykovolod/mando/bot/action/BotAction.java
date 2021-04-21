package com.mykovolod.mando.bot.action;

import com.mykovolod.mando.dto.BotUpdate;

public interface BotAction {
    String START = "start";
    String STOP = "stop";

    String getName();

    void handle(BotUpdate botUpdate);
}
