/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.materialRequirements.internal.print.pdf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementReportDataServiceImpl;
import com.qcadoo.mes.materialRequirements.internal.util.EntityOrderNumberComparator;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class MaterialRequirementPdfService extends PdfDocumentService {

    private final int[] defaultMatReqHeaderColumnWidth = new int[] { 25, 25, 24, 13, 13 };

    private final int[] defaultOrderHeaderColumnWidth = new int[] { 37, 37, 13, 13 };

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialRequirementReportDataServiceImpl materialRequirementReportDataService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = getTranslationService().translate("materialRequirements.materialRequirement.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, entity.getField("name").toString(), documenTitle, documentAuthor,
                (Date) entity.getField("date"), securityService.getCurrentUserName());
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("materialRequirements.materialRequirement.report.paragrah",
                locale), PdfUtil.getArialBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("orders.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.product.label", locale));
        orderHeader.add(getTranslationService().translate("basic.product.unit.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.plannedQuantity.label", locale));
        addOrderSeries(document, entity, orderHeader);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("materialRequirements.materialRequirement.report.paragrah2",
                locale), PdfUtil.getArialBold11Dark()));
        List<String> productHeader = new ArrayList<String>();
        productHeader.add(getTranslationService().translate("basic.product.number.label", locale));
        productHeader.add(getTranslationService().translate("basic.product.name.label", locale));
        productHeader.add(getTranslationService().translate("basic.product.unit.label", locale));
        productHeader.add(getTranslationService().translate("technologies.technologyOperationComponent.quantity.label", locale));
        addTechnologySeries(document, entity, productHeader);
    }

    private void addTechnologySeries(final Document document, final Entity entity, final List<String> productHeader)
            throws DocumentException {
        List<Entity> orders = entity.getManyToManyField("orders");
        Boolean onlyComponents = (Boolean) entity.getField("onlyComponents");
        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForOrdersTechnologyProducts(orders,
                onlyComponents);
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        PdfPTable table = PdfUtil.createTableWithHeader(4, productHeader, true, defaultOrderHeaderColumnWidth);
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            Object unit = entry.getKey().getField("unit");
            if (unit != null) {
                table.addCell(new Phrase(unit.toString(), PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue()), PdfUtil.getArialBold9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        List<Entity> orders = entity.getManyToManyField("orders");
        Collections.sort(orders, new EntityOrderNumberComparator());
        PdfPTable table = PdfUtil.createTableWithHeader(5, orderHeader, true, defaultMatReqHeaderColumnWidth);

        for (Entity order : orders) {
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
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            table.addCell(new Phrase(getDecimalFormat().format(plannedQuantity), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("materialRequirements.materialRequirement.report.title", locale);
    }

}
