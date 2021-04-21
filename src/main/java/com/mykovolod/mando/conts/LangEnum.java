package com.mykovolod.mando.conts;

import java.util.Arrays;

public enum LangEnum {
    UKR,
    RUS,
    ENG,
    FRA,
    POR;

    public static LangEnum valueOf2LetterLang(String lang) {
        if (lang == null) return LangEnum.ENG;
        for (LangEnum value : LangEnum.values()) {
            if (value.name().startsWith(lang.toUpperCase())) {
                return value;
            }
        }
        return LangEnum.ENG;
    }

    public static LangEnum getEnum(String value) {
        return Arrays.stream(LangEnum.values()).filter(m -> m.name().equalsIgnoreCase(value)).findAny().orElse(null);
    }

    public static LangEnum getEnumOrDefault(String value) {
        final var langEnum = getEnum(value);
        if (langEnum == null) {
            return LangEnum.ENG;
        } else {
            return langEnum;
        }
    }
}
