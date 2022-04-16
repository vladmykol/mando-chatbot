package com.mykovolod.mando.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class BotGpt3Use {
    private LocalDate useDate = LocalDate.now();
    private AtomicInteger useTimes = new AtomicInteger(1);
}
