package com.mykovolod.mando.service;

import com.mykovolod.mando.dto.BotGpt3Use;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class Gpt3Service {
    private final OpenAiService service;
    private final Map<String, BotGpt3Use> Gpt3UsePerBot
            = new ConcurrentHashMap<>(20);

    public String getAiResponse(String context, String stopWord, String message) throws URISyntaxException, IOException, InterruptedException {
        List<String> stopWords = Stream.of(stopWord.split(","))
                .map(str -> " " + str)
                .collect(Collectors.toList());
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(context + " " + message + "\n")
                .temperature(0.9)
                .maxTokens(150)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.6)
                .stop(stopWords)
                .echo(false)
                .build();

        log.debug("Request to GPT3: {}", completionRequest);

        CompletionResult completionResult = service.createCompletion("text-davinci-002", completionRequest);

        log.debug("Response from GPT3: {}", completionResult.getChoices());

        return completionResult.getChoices().stream()
                .map((choice) -> choice.getText().substring(4))
                .collect(Collectors.joining(". "));
    }

    public boolean isNotRateLimited(String botId) {
        Gpt3UsePerBot.merge(botId, new BotGpt3Use(), (prev, curr) -> {
            if (prev.getUseDate().equals(LocalDate.now())) {
                prev.getUseTimes().getAndIncrement();
            } else {
                prev.setUseDate(LocalDate.now());
                prev.getUseTimes().set(1);
            }
            return prev;
        });
        return Gpt3UsePerBot.get(botId).getUseTimes().get() <= 31;
    }
}
