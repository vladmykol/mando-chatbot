package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.LangEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class LangDetectService {
    private LanguageDetector langDetect;

    @PostConstruct
    public void init() throws IOException {
        log.debug("Init lang detection service");

        final var classPathResource = new ClassPathResource("nlp" + File.separator + "langdetect-183.bin");
        // Load serialized trained model
        LanguageDetectorModel model = new LanguageDetectorModel(classPathResource.getInputStream());

        langDetect = new LanguageDetectorME(model);
    }

    public LangEnum detect(String string) {
        final var languages = detectAllLang(string);

        for (int i = 0; i < languages.length && i <= 20; i++) {
            final var langEnum = LangEnum.getEnum(languages[i].getLang());
            if (langEnum != null) {
                return langEnum;
            }
        }

        final var defaultLang = LangEnum.OTHER;
        log.warn("Not able to detect language so fall back to default {}", defaultLang);
        return defaultLang;
    }

    private Language[] detectAllLang(String string) {
        final var languages = langDetect.predictLanguages(string);
        log.trace("predict languages {}", Arrays.asList(languages));
        return languages;
    }


}
