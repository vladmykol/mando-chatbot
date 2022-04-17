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
import java.util.Set;
import java.util.SortedSet;

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

    public LangEnum detect(String string, Set<LangEnum> supportedLang, LangEnum userPreferredLang) {
        final var languages = detectAllLang(string);

        //match detected lang with any supported lang
        for (int i = 0; i < languages.length && i <= 20; i++) {
            final var langEnum = LangEnum.getEnum(languages[i].getLang());
            if (langEnum != null && supportedLang.contains(langEnum)) {
                return langEnum;
            }
        }

        log.warn("Not able to detect language for message '{}' from list of supported '{}' so fall back to default user lang {}", string, supportedLang, userPreferredLang);
        return userPreferredLang;
    }

    private Language[] detectAllLang(String string) {
        final var languages = langDetect.predictLanguages(string);
        log.trace("predict languages {}", Arrays.asList(languages));
        return languages;
    }


}
