package com.mykovolod.mando.nlp;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MatchTest {

    @Test
    void intentIsNotPrice() {
        final var matches = "bot".matches("^[bot]|^[connect]|^[what can you do]");
        assertThat(matches, is(true));
    }

}
