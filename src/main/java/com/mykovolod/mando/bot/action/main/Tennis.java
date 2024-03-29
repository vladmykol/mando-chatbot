package com.mykovolod.mando.bot.action.main;

import com.mykovolod.mando.dto.BotUpdate;
import com.mykovolod.mando.service.ChatService;
import com.mykovolod.mando.service.TelegramInlineKeyboard;
import lombok.RequiredArgsConstructor;
import org.glassfish.grizzly.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
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
    private static final String GO_TO_BOOKING = "go_booking";

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
                    var whenFromUser = botUpdate.getSecondCommandParam().toLowerCase();

                    var date = getDate(whenFromUser);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                    var dateString = formatter.format(date);
                    var time = getTime(whenFromUser);
                    var place = getPlace(whenFromUser);

                    var schedule = getSchedule(dateString, time, place);
                    String scheduleString = "Нічого нема";
                    if (!schedule.isEmpty()) {
                        scheduleString = place.getSecond() + " - " + date.format(DateTimeFormatter.ofPattern("E d MMM")) + "\n" +
                                schedule.keySet().stream()
                                        .map(key -> key + "=" + schedule.get(key))
                                        .collect(Collectors.joining("\n"));
                    }

                    var urlKeyboard = new TelegramInlineKeyboard(getName())
                            .addUrlButton("До букінгу", getSearchUrl(dateString, time, place));

                    botUpdate
                            .addOutEditMessage(scheduleString)
                            .setKeyBoard(urlKeyboard.getMarkup());

//                    chatService.removePendingCommand(botUpdate.getChat());
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

    TreeMap<LocalTime, SortedSet<Integer>> getSchedule(String date, List<String> time, Pair<Integer, String> place) {
        var perHourSchedule = new TreeMap<LocalTime, SortedSet<Integer>>();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        SearchRequest searchRequest = new SearchRequest(9000, 0, "startDateTime", true,
                "tennis " + place.getSecond(), null, place.getFirst().toString(), null, List.of(date),
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
        return Integer.valueOf(name.replaceAll("[^0-9]", ""));
    }

    String getSearchUrl(String date, List<String> time, Pair<Integer, String> place) {
        var urlTemplate = "https://loisirs.montreal.ca/IC3/#/U6510/search/?searchParam=%7B%22filter%22:%7B%22isCollapsed%22:false,%22value%22:%7B%22startTime%22:%22{0}%22,%22endTime%22:%22{1}%22,%22dates%22:%5B%22{2}%22%5D,%22boroughIds%22:%22{3}%22%7D%7D,%22search%22:%22tennis%20{4}%22,%22sortable%22:%7B%22isOrderAsc%22:true,%22column%22:%22startDateTime%22%7D%7D&bids=20,55&hasBoroughFilter=true";
        return MessageFormat.format(urlTemplate, time.get(0), time.get(1), date, place.getFirst(), place.getSecond().replace(" ","%20"));
    }

    List<String> getTime(String query) {
        var timeFromTo = Arrays.stream(query.replaceAll("[^0-9 -]", "")
                        .replace("-", " ")
                        .split(" "))
                .filter(Predicate.not(String::isEmpty))
                .map(Integer::valueOf).toList();
        var from = ":00";
        var to = ":00";
        var afterNoon = !query.toLowerCase(Locale.ROOT).contains("ран");//ранок
        if (!timeFromTo.isEmpty()) {
            if (timeFromTo.get(0) < 12 && afterNoon) {
                from = (timeFromTo.get(0) + 12) + from;
            } else {
                from = timeFromTo.get(0) + from;
            }
            if (timeFromTo.size() == 1) {
                to = "00:00";
            } else if (timeFromTo.get(1) < 12 && afterNoon) {
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
        } else if (query.contains("післязавтра")) {
            now = now.plus(2, ChronoUnit.DAYS);
        }
        return now;
    }

    @NotNull Pair<Integer, String> getPlace(@NotNull String query) {
        var place = 7;
        var name = "La Fontaine";

        if (query.contains("МЛК")) {
            place = 9;
            name = "MLK";
        }
        return new Pair<>(place, name);
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


