package com.mykovolod.mando;

import com.mykovolod.mando.service.BotFatherService;
import com.mykovolod.mando.service.DbInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class BotApplication {
    private final BotFatherService botFatherService;
    private final DbInitService dbInitService;

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAfterSpringBootStart() throws IOException {
        dbInitService.presetInitialBotConfig();
        botFatherService.initBots();
    }
}
