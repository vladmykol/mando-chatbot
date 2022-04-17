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
class LandDetectTest {
    @Autowired
    LangDetectService langDetectService;
    Set<LangEnum> supportedLang = Set.of(LangEnum.values());

    @Test
    void exactLangUkrStart() {
        final var languages = langDetectService.detect("Привіт", supportedLang, LangEnum.ENG);
        assertThat(languages).isEqualTo(LangEnum.UKR);
    }

    @Test
    void exactLangRusStart() {
        final var languages = langDetectService.detect("Привет", supportedLang, LangEnum.ENG);
        assertThat(languages).isEqualTo(LangEnum.RUS);
    }

    @Test
    void exactLangUkr() {
        final var languages = langDetectService.detect("Привіт, як справи?", supportedLang, LangEnum.ENG);
        assertThat(languages).isEqualTo(LangEnum.UKR);
    }

    @Test
    void exactLangEng() {
        final var languages = langDetectService.detect("Hi there", supportedLang, LangEnum.ENG);
        assertThat(languages).isEqualTo(LangEnum.ENG);
    }

    @Test
    void exactLangOther() {
        final var languages = langDetectService.detect("नमस्ते नमस्ते", supportedLang, LangEnum.ENG);
        assertThat(languages).isEqualTo(LangEnum.ENG);
    }

}
