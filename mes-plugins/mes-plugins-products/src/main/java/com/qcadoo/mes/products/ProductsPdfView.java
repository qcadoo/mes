package com.qcadoo.mes.products;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;

public abstract class ProductsPdfView extends AbstractPdfView {

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected SecurityService securityService;

    private static final String FONT_PATH = "fonts/Arial.ttf";

    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        DefaultEntity entity = (DefaultEntity) model.get("entity");

        ClassPathResource classPathResource = new ClassPathResource(FONT_PATH);
        FontFactory.register(classPathResource.getPath());
        BaseFont baseFont = BaseFont.createFont(classPathResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font12 = new Font(baseFont, 12);

        addContent(document, entity, request.getLocale(), font12);
        addTitle(document, request.getLocale());
        addMetaData(document, request.getLocale());
        writer.addJavaScript("this.print(false);", false);

    }

    private void addMetaData(final Document document, final Locale locale) {
        // TODO KRNA add to properties ?
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("QCADOO");
        document.addCreator("QCADOO");
    }

    protected void addContent(final Document document, final DefaultEntity order, final Locale locale, final Font font)
            throws DocumentException, IOException {
        document.add(new Paragraph("", font));
    }

    protected Font getFontBold(final Font font) {
        Font fontBold = new Font(font);
        fontBold.setStyle(Font.BOLD);
        return fontBold;
    }

    protected abstract void addTitle(final Document document, final Locale locale);
}
