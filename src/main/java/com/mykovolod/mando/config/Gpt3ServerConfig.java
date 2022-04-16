package com.mykovolod.mando.config;

import com.theokanning.openai.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Gpt3ServerConfig {

    @Bean
    OpenAiService createOpenAiServer(@Value("${gpt3.token}") String gpt3Token) {
        return new OpenAiService(gpt3Token);
    }
}
