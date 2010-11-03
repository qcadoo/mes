package com.qcadoo.mes.products.print.view.pdf;

import java.awt.Color;
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
import com.qcadoo.mes.products.print.service.pdf.PdfPageNumbering;

public abstract class ProductsPdfView extends AbstractPdfView {

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected SecurityService securityService;

    private static final String FONT_PATH = "fonts/Arial.ttf";

    protected Font arialBold19Light;

    protected Font arialBold19Dark;

    protected Font arialRegular9Light;

    protected Font arialRegular9Dark;

    protected Font arialBold9Dark;

    protected Color lineDarkColor;

    protected Color backgroundColor;

    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");
        prepareFontsAndColors();
        String fileName = addContent(document, entity, request.getLocale());
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".pdf");
        writer.addJavaScript("this.print(false);", false);

    }

    @Override
    protected void prepareWriter(final Map<String, Object> model, final PdfWriter writer, final HttpServletRequest request)
            throws DocumentException {
        super.prepareWriter(model, writer, request);
        writer.setMargins(8, 80, 60, 120);
        writer.setPageEvent(new PdfPageNumbering());
    }

    @Override
    protected void buildPdfMetadata(final Map<String, Object> model, final Document document, final HttpServletRequest request) {
        addTitle(document, request.getLocale());
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("QCADOO");
        document.addCreator("QCADOO");
    }

    protected String addContent(final Document document, final DefaultEntity entity, final Locale locale)
            throws DocumentException, IOException {
        document.add(new Paragraph("", arialRegular9Light));
        return "document";
    }

    protected abstract void addTitle(final Document document, final Locale locale);

    private void prepareFontsAndColors() throws DocumentException, IOException {
        ClassPathResource classPathResource = new ClassPathResource(FONT_PATH);
        FontFactory.register(classPathResource.getPath());
        BaseFont baseFont = BaseFont.createFont(classPathResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Color light = new Color(77, 77, 77);
        Color dark = new Color(26, 26, 26);
        lineDarkColor = new Color(102, 102, 102);
        // lineLightColor = new Color(153, 153, 153);
        backgroundColor = new Color(230, 230, 230);
        arialBold19Light = new Font(baseFont, 19);
        arialBold19Light.setStyle(Font.BOLD);
        arialBold19Light.setColor(light);
        arialBold19Dark = new Font(arialBold19Light);
        arialBold19Dark.setColor(dark);
        arialRegular9Light = new Font(baseFont, 9);
        arialRegular9Light.setColor(light);
        arialRegular9Dark = new Font(baseFont, 9);
        arialRegular9Dark.setColor(dark);
        arialBold9Dark = new Font(arialRegular9Dark);
        arialBold9Dark.setStyle(Font.BOLD);
        // arialBold11Dark = new Font(arialBold19Dark);
        // arialBold11Dark.setSize(11);
    }
}
