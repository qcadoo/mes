package com.qcadoo.mes.products.print.pdf;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

public final class MaterialRequirementPdfView extends AbstractPdfView {

    @Autowired
    private TranslationService translationService;

    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");
        Object fileName = entity.getField("fileName");
        if (fileName != null && !"".equals(fileName.toString().trim())) {
            copyPdf(document, writer, (String) fileName);
            String fileNameWithoutPath = ((String) fileName).substring(((String) fileName).lastIndexOf("/") + 1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileNameWithoutPath + PdfUtil.PDF_EXTENSION);
        }
    }

    @Override
    protected void buildPdfMetadata(final Map<String, Object> model, final Document document, final HttpServletRequest request) {
        addTitle(document, request.getLocale());
        PdfUtil.addMetaData(document);
    }

    private void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.materialRequirement.report.title", locale));
    }

    private void copyPdf(final Document document, final PdfWriter writer, final String existingWorkbookFileName)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(existingWorkbookFileName + PdfUtil.PDF_EXTENSION);
        int n = reader.getNumberOfPages();
        PdfImportedPage page;
        for (int i = 1; i <= n; i++) {
            page = writer.getImportedPage(reader, i);
            Image instance = Image.getInstance(page);
            document.add(instance);
        }
    }

}
