package com.mykovolod.mando.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentenceDetectService {
    private SentenceDetectorME detector;

    @PostConstruct
    public void init() throws IOException {
        log.debug("Init sentence detection service");
        //Loading sentence detector model
        final var classPathResource = new ClassPathResource("nlp" + File.separator + "en-sent.bin");
        SentenceModel model = new SentenceModel(classPathResource.getInputStream());
        detector = new SentenceDetectorME(model);
    }

    public String[] detect(String sentence) {
        return detector.sentDetect(sentence);
    }
}
