package com.mykovolod.mando.nlp;

import com.mykovolod.mando.service.BotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
//@AutoConfigureMockMvc
//@RestClientTest
class IntentEntityRulTest {
    @Autowired
    BotService botService;
//    @Autowired
//    IntentService intentService;
//
//    private String getIntent(String string) {
//        return intentService.detectIntent(string, LangEnum.RUS);
//    }
//
//    @Test
//    void intentHi() {
//        final var intent = getIntent("привет бот");
//        assertThat(intent, is("привет"));
//    }
//
//    @Test
//    void intentHowAreYou() {
//        final var intent = getIntent("как у тебя дела?");
//        assertThat(intent, is("как дела"));
//    }
//
//
//    @Test
//    void intentPrice() {
//        final var intent = getIntent("какова цена проката?");
//        assertThat(intent, is("цена"));
//    }

    @Test
    void testDelete(){
        botService.deleteBot("5ff4e843f6eb154594fddaa4");
    }

}
