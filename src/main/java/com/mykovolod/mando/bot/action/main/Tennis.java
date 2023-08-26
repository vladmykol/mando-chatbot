package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Test action to quickly find tennis courts
 */
@Service
@RequiredArgsConstructor
public class Tennis implements MainBotAction {
    private static final String WHEN = "when";

    private final ChatService chatService;
    private final RestTemplate restTemplate;

    private final Random random = new Random();

    @Override
    public String getName() {
        return "tennis";
    }

    @Override
    public void handle(BotUpdate botUpdate) {
        if (botUpdate.hasCommandParams()) {
            final var commandFromUser = botUpdate.getFirstCommandParam();

            switch (commandFromUser) {
                case WHEN:
                    var whenFromUser = botUpdate.getSecondCommandParam();
                    var date = getDate(whenFromUser);
                    var time = getTime(whenFromUser);

                    var schedule = getSchedule(date, time);
                    String scheduleString = "Нічого нема";
                    if (!schedule.isEmpty()) {
                        scheduleString = schedule.keySet().stream()
                                .map(key -> key + "=" + schedule.get(key))
                                .collect(Collectors.joining("\n"));
                    }

                    botUpdate.addOutMessage(scheduleString);
                    chatService.removePendingCommand(botUpdate.getChat());
                    break;

                default:
                    chatService.removePendingCommand(botUpdate.getChat());
                    String[] myArray = {"Не потянто", "Не ясно", "Ой лишенько. Давай спочатку"};
                    String randomElement = myArray[random.nextInt(myArray.length)];
                    botUpdate.addOutMessage(randomElement);
            }
        } else {
            botUpdate.addOutMessage("Коли?");
            chatService.setPendingCommand(botUpdate.getChat().getId(), getName(), WHEN);
        }
    }

    TreeMap<LocalTime, SortedSet<Integer>> getSchedule(ZonedDateTime date, List<String> time) {
        var perHourSchedule = new TreeMap<LocalTime, SortedSet<Integer>>();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        SearchRequest searchRequest = new SearchRequest(100, 0, "startDateTime", true,
                "tennis MLK", null, "9", null, List.of(formatter.format(date)),
                time.get(0), time.get(1));
        var request = new HttpEntity<>(searchRequest, headers);

        var response =
                restTemplate.postForObject("https://loisirs.montreal.ca/IC3/api/U6510/public/search", request, SearchResponse.class);

        if (response != null && response.recordCount > 0) {
            var responseResults = response.results.stream().filter(f -> (f.canReserve.value)).toList();

            for (SearchResponseResult responseResult : responseResults) {
                var foundTime = LocalDateTime.ofInstant(responseResult.startDateTime, ZoneId.of("Canada/Eastern")).toLocalTime();
                var courtNum = getCourtNumber(responseResult.facility.name);
                if (courtNum != null) {
                    if (perHourSchedule.containsKey(foundTime)) {
                        perHourSchedule.get(foundTime).add(courtNum);
                    } else {
                        var set = new TreeSet<Integer>();
                        set.add(courtNum);
                        perHourSchedule.put(foundTime, set);
                    }
                }
            }
        }

        return perHourSchedule;
    }

    Integer getCourtNumber(String name) {
        var sep = name.indexOf(',');
        if (sep > 0) {
            return Integer.valueOf(name.substring(sep - 2, sep).trim());
        } else {
            return null;
        }
    }

    List<String> getTime(String query) {
        var timeFromTo = Arrays.stream(query.replaceAll("[^0-9 -]", "")
                        .replace("-", " ")
                        .split(" "))
                .filter(Predicate.not(String::isEmpty))
                .map(Integer::valueOf).toList();
        var from = ":00";
        var to = ":00";
        var afterNoon = !query.toLowerCase(Locale.ROOT).contains("ранку") && !query.toLowerCase(Locale.ROOT).contains("ранок");
        if (!timeFromTo.isEmpty()) {
            if (timeFromTo.get(0) < 12 && afterNoon) {
                from = (timeFromTo.get(0) + 12) + from;
            } else {
                from = timeFromTo.get(0) + from;
            }
            if (timeFromTo.get(1) < 12 && afterNoon) {
                to = (timeFromTo.get(1) + 12) + to;
            } else {
                to = timeFromTo.get(1) + to;
            }
        }
        if (from.length() == 4) {
            from = "0" + from;
        }
        if (to.length() == 4) {
            to = "0" + to;
        }
        return List.of(from, to);
    }

    ZonedDateTime getDate(String query) {
        var now = ZonedDateTime.now(ZoneId.of("Canada/Eastern")).truncatedTo(ChronoUnit.DAYS);

        if (query.contains("завтра")) {
            now = now.plus(1, ChronoUnit.DAYS);
        }
        return now;
    }

    record SearchRequest(int limit, int offset, String sortColumn, boolean isSortOrderAsc, String searchString,
                         String facilityTypeIds, String boroughIds, String siteId, List<String> dates, String startTime,
                         String endTime) {
    }

    record SearchResponse(int recordCount, boolean warningRecordCountClipped, List<SearchResponseResult> results) {
    }

    record SearchResponseResult(SearchResponseResultFacility facility,
                                SearchResponseResultCanReserve canReserve,
                                Instant startDateTime,
                                Instant endDateTime
    ) {
    }

    record SearchResponseResultFacility(String name) {
    }

    record SearchResponseResultCanReserve(Boolean value) {
    }
}
