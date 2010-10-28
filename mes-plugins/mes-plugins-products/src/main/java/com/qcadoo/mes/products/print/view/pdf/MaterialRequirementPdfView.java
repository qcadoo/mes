package com.qcadoo.mes.products.print.view.pdf;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;

public final class MaterialRequirementPdfView extends AbstractPdfView {

    @Autowired
    protected TranslationService translationService;

    private static final String PDF_EXTENSION = ".pdf";

    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");
        Object fileName = entity.getField("fileName");
        if (fileName != null) {
            PdfReader reader = new PdfReader((String) fileName + PDF_EXTENSION);
            int n = reader.getNumberOfPages();
            PdfImportedPage page;
            for (int i = 1; i <= n; i++) {
                page = writer.getImportedPage(reader, i);
                Image instance = Image.getInstance(page);
                document.add(instance);
            }
        }
    }

    @Override
    protected void buildPdfMetadata(final Map<String, Object> model, final Document document, final HttpServletRequest request) {
        document.addTitle(translationService.translate("products.materialRequirement.report.title", request.getLocale()));
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("QCADOO");
        document.addCreator("QCADOO");
    }

}
