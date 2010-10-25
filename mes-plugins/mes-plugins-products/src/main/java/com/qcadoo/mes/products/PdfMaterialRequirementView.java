package com.qcadoo.mes.products;

import java.io.IOException;
import java.util.ArrayList;
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
        List<Entity> instructions = addOrderSeries(document, entity, font);
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah2", locale),
                getFontBold(font)));
        addBomSeries(document, entity, instructions, font);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.materialRequirement.report.title", locale));
    }

    private List<Entity> addOrderSeries(final Document document, final Entity entity, final Font font) throws DocumentException {
        List<Entity> orders = (List<Entity>) entity.getField("orders");
        List<Entity> instructions = new ArrayList<Entity>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity instruction = (Entity) order.getField("instruction");
            if (instruction != null) {
                instructions.add(instruction);
            }
            document.add(new Paragraph(order.getField("number") + " " + order.getField("name"), font));
        }
        return instructions;
    }

    private void addBomSeries(final Document document, final DefaultEntity entity, final List<Entity> instructions,
            final Font font) throws DocumentException {
        for (Entity instruction : instructions) {
            List<Entity> bomComponents = (List<Entity>) instruction.getField("bomComponents");
            for (Entity bomComponent : bomComponents) {
                Entity product = (Entity) bomComponent.getField("product");
                if (!(Boolean) entity.getField("onlyComponents") || "component".equals(product.getField("typeOfMaterial"))) {
                    document.add(new Paragraph(product.getField("number") + " " + product.getField("name") + " "
                            + bomComponent.getField("quantity").toString() + " " + product.getField("unit"), font));
                }
            }
        }
    }
}
