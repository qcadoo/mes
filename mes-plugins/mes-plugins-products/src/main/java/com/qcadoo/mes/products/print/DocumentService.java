package com.qcadoo.mes.products.print;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.types.internal.DateType;

public abstract class DocumentService {

    private static final SimpleDateFormat D_T_F = new SimpleDateFormat(DateType.REPORT_DATE_TIME_FORMAT);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Value("${windowsFonts}")
    private String windowsFontsPath;

    @Value("${macosFonts}")
    private String macosFontsPath;

    @Value("${linuxFonts}")
    private String linuxFontsPath;

    @Value("${reportPath}")
    private String path;

    protected final String getFullFileName(final Date date, final String fileName, final String suffix) {
        return path + fileName + "_" + D_T_F.format(date) + suffix;
    }

    protected final void updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        dataDefinitionService.get("products", entityName).save(entity);
    }

    public abstract void generateDocument(final Entity entity, final Locale locale, final boolean save) throws IOException,
            DocumentException;

    protected abstract String getFileName();

    protected abstract String getSuffix();

    protected final TranslationService getTranslationService() {
        return translationService;
    }

    protected final String getFontsPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return windowsFontsPath;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            return macosFontsPath;
        } else if (SystemUtils.IS_OS_LINUX) {
            return linuxFontsPath;
        }
        return null;
    }

}
