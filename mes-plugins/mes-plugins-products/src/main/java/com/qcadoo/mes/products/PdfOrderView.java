package com.qcadoo.mes.products;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.types.internal.DateType;

public final class PdfOrderView extends ProductsPdfView {

    @Override
    protected void addContent(final Document document, final DefaultEntity entity, final Locale locale, final Font font)
            throws DocumentException, IOException {
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_FORMAT);
        UsersUser user = securityService.getCurrentUser();
        document.add(new Paragraph(df.format(new Date()), font));
        document.add(new Paragraph(
                translationService.translate("products.order.report.login", locale) + " " + user.getUserName(), font));
        document.add(new Paragraph(translationService.translate("products.order.report.order", locale), getFontBold(font)));
        document.add(new Paragraph(translationService.translate("products.ordeentityort.number", locale) + " "
                + entity.getField("number"), font));
        document.add(new Paragraph(translationService.translate("products.order.report.name", locale) + " "
                + entity.getField("name"), font));
        Entity product = (Entity) entity.getField("product");
        if (product == null) {
            document.add(new Paragraph("", font));
        } else {
            document.add(new Paragraph((String) product.getField("name"), font));
        }
        document.add(new Paragraph(translationService.translate("products.order.report.state", locale) + " "
                + entity.getField("state"), font));
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.order.report.title", locale));
    }
}
