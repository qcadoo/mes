/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.products.print.pdf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.products.print.ProductReportService;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

@Service
public final class MaterialRequirementPdfService extends PdfDocumentService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductReportService reportDataService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = getTranslationService().translate("products.materialRequirement.report.title", locale);
        String documentAuthor = getTranslationService().translate("products.materialRequirement.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity, documenTitle, documentAuthor, (Date) entity.getField("date"), user);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("products.materialRequirement.report.paragrah", locale),
                PdfUtil.getArialBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("products.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.product.label", locale));
        orderHeader.add(getTranslationService().translate("products.product.unit.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.plannedQuantity.label", locale));
        addOrderSeries(document, entity, orderHeader);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("products.materialRequirement.report.paragrah2", locale),
                PdfUtil.getArialBold11Dark()));
        List<String> productHeader = new ArrayList<String>();
        productHeader.add(getTranslationService().translate("products.product.number.label", locale));
        productHeader.add(getTranslationService().translate("products.product.name.label", locale));
        productHeader.add(getTranslationService().translate("products.product.unit.label", locale));
        productHeader.add(getTranslationService().translate("products.technologyOperationComponent.quantity.label", locale));
        addTechnologySeries(document, (DefaultEntity) entity, productHeader);
    }

    @Override
    protected void buildPdfMetadata(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("products.materialRequirement.report.title", locale));
        PdfUtil.addMetaData(document);
    }

    private void addTechnologySeries(final Document document, final DefaultEntity entity, final List<String> productHeader)
            throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
        Map<ProxyEntity, BigDecimal> products = reportDataService.getTechnologySeries(entity, orders);
        PdfPTable table = PdfUtil.createTableWithHeader(4, productHeader);
        for (Entry<ProxyEntity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            Object unit = entry.getKey().getField("unit");
            if (unit != null) {
                table.addCell(new Phrase(unit.toString(), PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(df.format(entry.getValue().stripTrailingZeros()), PdfUtil.getArialBold9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
        PdfPTable table = PdfUtil.createTableWithHeader(5, orderHeader);
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            Entity product = (Entity) order.getField("product");
            if (product != null) {
                table.addCell(new Phrase(product.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
            if (product != null) {
                Object unit = product.getField("unit");
                if (unit != null) {
                    table.addCell(new Phrase(unit.toString(), PdfUtil.getArialRegular9Dark()));
                } else {
                    table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                }
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            plannedQuantity = (plannedQuantity == null) ? new BigDecimal(0) : plannedQuantity.stripTrailingZeros();
            table.addCell(new Phrase(df.format(plannedQuantity), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    @Override
    protected String getFileName() {
        return "MaterialRequirement";
    }

    @Override
    protected String getSuffix() {
        return "";
    }

}
