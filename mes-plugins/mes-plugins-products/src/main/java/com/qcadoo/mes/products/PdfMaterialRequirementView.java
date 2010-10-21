package com.qcadoo.mes.products;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;

public final class PdfMaterialRequirementView extends ProductsPdfView {

    @Override
    protected void addContent(final Document document, final DefaultEntity entity, final Locale locale, final Font font)
            throws DocumentException, IOException {
        UsersUser user = securityService.getCurrentUser();
        document.add(new Paragraph(entity.getField("date").toString(), font));
        document.add(new Paragraph(user.getUserName(), font));
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah", locale),
                getFontBold(font)));
        List<Entity> orders = (List<Entity>) entity.getField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            document.add(new Paragraph((String) order.getField("number") + " " + (String) order.getField("name")));
        }
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah2", locale),
                getFontBold(font)));

        for (int i = 0; i < 5; i++) {
            document.add(new Paragraph("<numer" + i + "><nazwa" + i + "><ilosc><jednostka>"));
        }
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.materialRequirement.report.title", locale));
    }
}
