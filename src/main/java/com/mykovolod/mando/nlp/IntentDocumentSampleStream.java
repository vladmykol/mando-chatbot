package com.mykovolod.mando.nlp;

import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.ObjectStream;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class IntentDocumentSampleStream implements ObjectStream<DocumentSample> {
    String category;
    List<String> samples;
    int currentLine = 0;

    public IntentDocumentSampleStream(String category, List<String> samples) {
        this.category = category;
        this.samples = samples;
    }

    public DocumentSample read() throws IOException {
        if (currentLine < samples.size()) {
            final var documentSample = getDocumentSample(samples.get(currentLine));
            currentLine++;
            return documentSample;
        } else {
            return null;
        }
    }

    private DocumentSample getDocumentSample(String sampleString) throws IOException {
        // Whitespace tokenize entire string
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sampleString.toLowerCase());

        //remove entities
        Vector<String> vector = new Vector<String>(tokens.length);
        boolean skip = false;
        for (String token : tokens) {
            if (!token.startsWith("<")) {
                vector.add(token);
            }
        }

        tokens = new String[vector.size()];
        vector.copyInto(tokens);

        DocumentSample sample;

        if (tokens.length > 0) {
            sample = new DocumentSample(category, tokens);
        } else {
            throw new IOException("Empty lines are not allowed!");
        }

        return sample;
    }

    public void reset() {
    }

    public void close() {
    }
}
