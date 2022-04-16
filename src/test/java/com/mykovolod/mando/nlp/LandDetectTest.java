package com.mykovolod.mando.nlp;

import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.service.LangDetectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
//@AutoConfigureMockMvc
//@RestClientTest
class LandDetectTest {
    @Autowired
    LangDetectService langDetectService;

    @Test
    void exactLangUkrStart() {
        final var languages = langDetectService.detect("Привіт");
        assertThat(languages).isEqualTo(LangEnum.UKR);
    }

    @Test
    void exactLangRusStart() {
        final var languages = langDetectService.detect("Привет");
        assertThat(languages).isEqualTo(LangEnum.RUS);
    }

    @Test
    void exactLangUkr() {
        final var languages = langDetectService.detect("Привіт, як справи?");
        assertThat(languages).isEqualTo(LangEnum.UKR);
    }

    @Test
    void exactLangRus() {
        final var languages = langDetectService.detect("Привет. Как дела?");
        assertThat(languages).isEqualTo(LangEnum.RUS);
    }

}
