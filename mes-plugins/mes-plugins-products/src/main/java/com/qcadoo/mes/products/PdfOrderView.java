package com.qcadoo.mes.products;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.types.internal.DateType;

public class PdfOrderView extends AbstractPdfView {

    @Autowired
    private SecurityService securityService;

    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        DefaultEntity order = (DefaultEntity) model.get("order");

        UsersUser user = securityService.getCurrentUser();

        addContent(document, order, user);
        addMetaData(document);
    }

    private static void addMetaData(final Document document) {
        document.addTitle("Order PDF");
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("QCADOO");
        document.addCreator("QCADOO");
    }

    private static void addContent(final Document document, final DefaultEntity order, final UsersUser user)
            throws DocumentException {
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_FORMAT);
        document.add(new Paragraph(df.format(new Date())));
        document.add(new Paragraph("Wygenerowane przez: " + user.getUserName()));
        document.add(new Paragraph("Zlecenie produkcyjne"));
        document.add(new Paragraph("Nr: " + order.getField("number")));
        document.add(new Paragraph("Nazwa: " + order.getField("name")));
        Entity product = (Entity) order.getField("product");
        document.add(new Paragraph((String) product.getField("name")));
        document.add(new Paragraph("Status : " + order.getField("state")));

    }
}
