package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.service.ChatService;
import org.glassfish.grizzly.utils.Pair;
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
        assertThat(tennis.getDate("післязавтра 5-7"), is(ZonedDateTime.now(ZoneId.of("Canada/Eastern"))
                .truncatedTo(ChronoUnit.DAYS).plusDays(1)));
    }

    @Test
    void parseTime() {
        assertThat(tennis.getTime("завтра 8-9"), is(List.of("20:00", "21:00")));
        assertThat(tennis.getTime("глянути час 5 - 7"), is(List.of("17:00", "19:00")));
        assertThat(tennis.getTime("глянути 9 - 10 Ранку"), is(List.of("09:00", "10:00")));
        assertThat(tennis.getTime("зранку 11 13"), is(List.of("11:00", "13:00")));
        assertThat(tennis.getTime("корт 3"), is(List.of("15:00", "00:00")));
    }

    @Test
    void createUiUrl() {
        assertThat(tennis.getSearchUrl("2023-09-23T00:00:00.000-04:00", List.of("2023-09-22T21:00:00.000-04:00", "2023-09-22T00:00:00.000-04:00"), new Pair<Integer, String>(7, "La Fontaine")),
                is("https://loisirs.montreal.ca/IC3/#/U6510/search/?searchParam=%7B%22filter%22:%7B%22isCollapsed%22:false,%22value%22:%7B%22startTime%22:%222023-09-22T21:00:00.000-04:00%22,%22endTime%22:%222023-09-22T00:00:00.000-04:00%22,%22dates%22:%5B%222023-09-23T00:00:00.000-04:00%22%5D,%22boroughIds%22:%227%22%7D%7D,%22search%22:%22tennis%20La%20Fontaine%22,%22sortable%22:%7B%22isOrderAsc%22:true,%22column%22:%22startDateTime%22%7D%7D&bids=20,55&hasBoroughFilter=true"));
    }
}