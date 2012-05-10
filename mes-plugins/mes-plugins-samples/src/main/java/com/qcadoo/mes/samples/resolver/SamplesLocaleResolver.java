package com.qcadoo.mes.samples.resolver;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SamplesLocaleResolver {

    @Value("${samplesDatasetLocale}")
    private String locale;

    public String resolve() {
        if ("pl".equalsIgnoreCase(locale)) {
            return "pl";
        }
        if ("en".equalsIgnoreCase(locale)) {
            return "en";
        }

        return Locale.getDefault().getLanguage();
    }

}
