package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.service.ChatService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TennisTest {
    @Mock
    ChatService chatService;
    Tennis tennis = new Tennis(chatService, new RestTemplate());

    @Test
    void parseDate() {
        assertThat(tennis.getDate("завтра 8-9"), is(ZonedDateTime.now(ZoneId.of("Canada/Eastern"))
                .truncatedTo(ChronoUnit.DAYS).plusDays(1)));
        assertThat(tennis.getDate("8-9"), is(ZonedDateTime.now(ZoneId.of("Canada/Eastern"))
                .truncatedTo(ChronoUnit.DAYS)));
    }

    @Test
    void parseTime() {
        assertThat(tennis.getTime("завтра 8-9"), is(List.of("20:00","21:00")));
        assertThat(tennis.getTime("глянути час 5 - 7"), is(List.of("17:00","19:00")));

        assertThat(tennis.getTime("глянути 5 - 7 Ранку"), is(List.of("05:00","07:00")));
        assertThat(tennis.getTime("зранку 11 13"), is(List.of("11:00","13:00")));
    }
}