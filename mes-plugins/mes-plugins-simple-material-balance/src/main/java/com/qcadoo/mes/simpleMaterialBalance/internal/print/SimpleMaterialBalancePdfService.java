package com.qcadoo.mes.simpleMaterialBalance.internal.print;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class SimpleMaterialBalancePdfService extends PdfDocumentService {

    @Autowired
    private SecurityService securityService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = getTranslationService().translate("simpleMaterialBalance.simpleMaterialBalance.report.title",
                locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documenTitle, documentAuthor, (Date) entity.getField("date"),
                securityService.getCurrentUserName());

        // REPORT CONTENTS
    }

    @Override
    protected String getSuffix() {
        return "";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
