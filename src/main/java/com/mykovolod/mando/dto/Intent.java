package com.mykovolod.mando.dto;

import com.google.common.collect.ImmutableSet;
import com.mykovolod.mando.conts.LangEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Data
@Slf4j
public class Intent {
    private final SortedMap<Double, Set<String>> allScoredIntentDataIds;
    private final String bestIntentDataId;
    private final LangEnum detectedLang;
    private String response;
    private String name;
    private List<String> responseButtons;

    public Intent(LangEnum detectedLang) {
        this.detectedLang = detectedLang;
        this.allScoredIntentDataIds = new TreeMap<>();
        this.bestIntentDataId = null;
    }

    public Intent(SortedMap<Double, Set<String>> allScoredIntentDataIds, LangEnum detectedLang) {
        this.detectedLang = detectedLang;
        this.allScoredIntentDataIds = allScoredIntentDataIds;
        this.bestIntentDataId = calcBestIntentDataId();
    }

    public Intent(String bestIntentDataId, LangEnum detectedLang) {
        this.detectedLang = detectedLang;
        final var allScoredIntentDataIds = new TreeMap<Double, Set<String>>();
        allScoredIntentDataIds.put(1d, ImmutableSet.of(bestIntentDataId));
        this.allScoredIntentDataIds = allScoredIntentDataIds;
        this.bestIntentDataId = bestIntentDataId;
    }

    private String calcBestIntentDataId() {
        final var intentWithMaxScore = allScoredIntentDataIds.get(allScoredIntentDataIds.lastKey());

        if (allScoredIntentDataIds.size() == 1) {
            log.debug("cannot determinate intent as they have the same score");
            return null;
        } else if (intentWithMaxScore.size() > 1) {
            log.debug("cannot determinate intent as there are couple intent with max score");
            return null;
        } else {
            // TODO: 11/9/2020 should be better way to return firs value from set
            for (String intentId : intentWithMaxScore) {
                return intentId;
            }
            return null;
        }
    }
}
