package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.LangEnum;
import com.mykovolod.mando.entity.IntentData;
import com.mykovolod.mando.nlp.IntentDocumentSampleStream;
import com.mykovolod.mando.repository.IntentDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.ml.AbstractTrainer;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.TrainingParameters;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategorizationService {
    private final IntentDataRepository intentDataRepository;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public Map<LangEnum, DocumentCategorizerME> trainModel(List<String> intentIds, Set<LangEnum> supportedLang) {
        Map<LangEnum, DocumentCategorizerME> categorizers = new HashMap<>();

        final var iterableIntentData = intentDataRepository.findByIntentIdInAndLangIn(intentIds, supportedLang);

        final var intentDataByLangMap = iterableIntentData.stream()
                .collect(Collectors.groupingBy(IntentData::getLang));

        intentDataByLangMap.forEach((langEnum, intentDataList) -> {
            categorizers.put(langEnum, createDocumentCatigorizer(langEnum, intentDataList));
        });

        return categorizers;
    }

    private DocumentCategorizerME createDocumentCatigorizer(LangEnum langEnum, List<IntentData> intentDataList) {
        String[] intentParams = "param1, param2".split(",");

        List<ObjectStream<DocumentSample>> categoryStreams = new ArrayList<>();
        intentDataList.forEach(intentData -> {
            if (intentData.getSamples() != null && !intentData.getSamples().isEmpty()) {
                ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream(intentData.getId(), intentData.getSamples());
                categoryStreams.add(documentSampleStream);
            }
        });

        TrainingParameters trainingParams = new TrainingParameters();
        trainingParams.put(TrainingParameters.ITERATIONS_PARAM, 10);
        if (!log.isTraceEnabled()) {
            trainingParams.put(AbstractTrainer.VERBOSE_PARAM, false);
        }
        trainingParams.put(TrainingParameters.CUTOFF_PARAM, 0);

        DoccatModel doccatModel = null;
        final var lang = langEnum.toString().toLowerCase();

        try (ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils.concatenateObjectStream(categoryStreams)) {
            doccatModel = DocumentCategorizerME.train(lang, combinedDocumentSampleStream, trainingParams, new DoccatFactory());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage() + " [lang=" + lang + "]");
        }

//        List<TokenNameFinderModel> tokenNameFinderModels = new ArrayList<TokenNameFinderModel>();
//
//        for (String slot : slots) {
//            List<ObjectStream<NameSample>> nameStreams = new ArrayList<ObjectStream<NameSample>>();
//            for (File trainingFile : trainingDirectory.listFiles()) {
//                ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(trainingFile), "UTF-8");
//                ObjectStream<NameSample> nameSampleStream = new NameSampleDataStream(lineStream);
//                nameStreams.add(nameSampleStream);
//            }
//            ObjectStream<NameSample> combinedNameSampleStream = ObjectStreamUtils.concatenateObjectStream(nameStreams);
//
//            TokenNameFinderModel tokenNameFinderModel = NameFinderME.train("ua", slot, combinedNameSampleStream, trainingParams, new TokenNameFinderFactory());
//            combinedNameSampleStream.close();
//            tokenNameFinderModels.add(tokenNameFinderModel);
//        }

        return new DocumentCategorizerME(doccatModel);
    }

    public SortedMap<Double, Set<String>> categorize(DocumentCategorizerME categorizer, String string) {
        final var tokenizedString = tokenizer.tokenize(string.toLowerCase());
        return categorizer.sortedScoreMap(tokenizedString);
    }

}
