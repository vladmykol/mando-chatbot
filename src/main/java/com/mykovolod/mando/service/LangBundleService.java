package com.mykovolod.mando.service;

import com.mykovolod.mando.conts.LangEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class LangBundleService {
    private final MessageSource messageSource;

    public String getMessage(String messageCode, Object[] args, LangEnum langCode) {
        Locale locale = new Locale(langCode.name(), "");
        return messageSource.getMessage(messageCode, args, locale);
    }

    public String getMessage(String messageCode, LangEnum langCode) {
        return getMessage(messageCode, new Object[0], langCode);
    }
}
