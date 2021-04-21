package com.mykovolod.mando.nlp;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
//@AutoConfigureMockMvc
//@RestClientTest
class SentenceDetectionServiceTest {
//    @Autowired
//    IntentService intentService;

    private void detect(String string) throws IOException {
        String sentence = "Привет. Какие есть фари?";

        //Loading sentence detector model
        final var classPathResource = new ClassPathResource("nlp" + File.separator + "en-sent.bin");
        SentenceModel model = new SentenceModel(classPathResource.getInputStream());

        //Instantiating the SentenceDetectorME class
        SentenceDetectorME detector = new SentenceDetectorME(model);

        //Detecting the sentence
        String sentences[] = detector.sentDetect(sentence);

        //Printing the sentences
        for (String sent : sentences)
            System.out.println(sent);
    }

    @Test
    void simpleSentenceTest() throws IOException {

        detect("hello bot");
    }

}
