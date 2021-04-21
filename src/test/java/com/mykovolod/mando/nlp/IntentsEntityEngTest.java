package com.mykovolod.mando.nlp;

import com.mykovolod.mando.service.IntentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
//@AutoConfigureMockMvc
//@RestClientTest
class IntentsEntityEngTest {
    @Autowired
    IntentService intentService;

    private String getIntent(String string) {
//        final var botInfo = new BotInfo(BotConst.MAIN_BOT_ID, "test", "test");
//        botInfo.setCategorizers(intentService.trainModel(botInfo.getBotId()));
//
//        final var chat = Chat.builder().telegramChatId("1").build();
//        final var botUpdate = new BotUpdate(botInfo, chat, string, null, null);

//        final var intent = intentService.detectIntent(botUpdate, LangEnum.ENG);
//        final var bestIntentDataId = intent.getBestIntentDataId();
//        return intentService.getIntentNameByIntentDataId(bestIntentDataId);
        return null;
    }

    @Test
    void intentHi() {

        final var intent = getIntent("hello bot");
        assertThat(intent, is("hi"));
    }

    @Test
    void intentHowAreYou() {
        final var intent = getIntent("how are you?");
        assertThat(intent, is("how are you"));
    }


    @Test
    void intentPrice() {
        final var intent = getIntent("I want to know the price for product X");
        assertThat(intent, is("price"));
    }

    @Test
    void intentIsNotPrice() {
        final var intent = getIntent("Thank you but your prices are very height for me");
        assertThat(intent, not("price"));
    }

}
