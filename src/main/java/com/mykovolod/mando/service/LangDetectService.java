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

@Service
@RequiredArgsConstructor
@Slf4j
public class LangDetectService {
    private LanguageDetector langDetect;

    @PostConstruct
    public void init() throws IOException {
        log.debug("Init lang detection service");
//        InputStreamFactory dataIn
//                = new MarkableFileInputStreamFactory(
//                new File("src/main/resources/nlp/LangSample.txt"));
//        ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
//        LanguageDetectorSampleStream sampleStream
//                = new LanguageDetectorSampleStream(lineStream);
//        TrainingParameters params = new TrainingParameters();
//        params.put(TrainingParameters.ITERATIONS_PARAM, 100);
//        params.put(TrainingParameters.CUTOFF_PARAM, 5);
//        params.put("DataIndexer", "TwoPass");
//        params.put(TrainingParameters.ALGORITHM_PARAM, "NAIVEBAYES");
//
//        LanguageDetectorModel model = LanguageDetectorME
//                .train(sampleStream, params, new LanguageDetectorFactory());

        final var classPathResource = new ClassPathResource("nlp" + File.separator + "langdetect-183.bin");
        // Load serialized trained model
        LanguageDetectorModel model = new LanguageDetectorModel(classPathResource.getInputStream());

        langDetect = new LanguageDetectorME(model);
    }

    public LangEnum detect(Set<LangEnum> supportedLang, String string) {
        final var languages = detectAllLang(string);

        for (Language language : languages) {
            final var langEnum = LangEnum.getEnum(language.getLang());
            if (langEnum != null && supportedLang.contains(langEnum)) {
                return langEnum;
            }
        }

        final var defaultLang = LangEnum.ENG;
        log.warn("Not able to detect language so fall back to default {}", defaultLang);
        return defaultLang;
    }

    private Language[] detectAllLang(String string) {
        final var languages = langDetect.predictLanguages(string);
        log.trace("predict languages {}", Arrays.asList(languages));
        return languages;
    }


}
