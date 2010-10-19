package com.qcadoo.mes.products;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.types.internal.DateType;

public class PdfOrderView extends AbstractPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        DefaultEntity order = (DefaultEntity) model.get("order");

        addContent(document, order, request.getLocale());
        addMetaData(document, request.getLocale());
        writer.addJavaScript("this.print(false);", false);
    }

    private void addMetaData(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.order.report.title", locale));
        // TODO KRNA add to properties ?
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("QCADOO");
        document.addCreator("QCADOO");
    }

    private void addContent(final Document document, final DefaultEntity order, final Locale locale) throws DocumentException,
            IOException {
        // TODO KRNA add to properties
        String fontDir = "/Library/Fonts";
        FontFactory.registerDirectory(fontDir);
        BaseFont times = BaseFont.createFont(fontDir + "/Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font t12 = new Font(times, 12);
        Font tb12 = new Font(times, 12, Font.BOLD);
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_FORMAT);
        document.add(new Paragraph(df.format(new Date()), t12));
        UsersUser user = securityService.getCurrentUser();
        document.add(new Paragraph(
                translationService.translate("products.order.report.login", locale) + " " + user.getUserName(), t12));
        document.add(new Paragraph(translationService.translate("products.order.report.order", locale), tb12));
        document.add(new Paragraph(translationService.translate("products.order.report.number", locale) + " "
                + order.getField("number"), t12));
        document.add(new Paragraph(translationService.translate("products.order.report.name", locale) + " "
                + order.getField("name"), t12));
        Entity product = (Entity) order.getField("product");
        if (product == null) {
            document.add(new Paragraph("", t12));
        } else {
            document.add(new Paragraph((String) product.getField("name"), t12));
        }
        document.add(new Paragraph(translationService.translate("products.order.report.state", locale) + " "
                + order.getField("state"), t12));
    }
}
