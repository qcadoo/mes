package com.qcadoo.mes.products.print;

import java.io.IOException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;

public abstract class DocumentService {

    @Autowired
    private TranslationService translationService;

    public abstract void generateDocument(final Entity entity, final Locale locale) throws IOException, DocumentException;

    protected abstract String getSuffix(final Locale locale);

    protected final TranslationService getTranslationService() {
        return translationService;
    }

}
