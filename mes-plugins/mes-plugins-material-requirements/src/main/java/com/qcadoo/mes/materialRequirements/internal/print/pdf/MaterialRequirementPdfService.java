/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.materialRequirements.internal.constants.MaterialRequirementFields.MRP_ALGORITHM;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialRequirements.internal.util.EntityOrderNumberComparator;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.security.api.SecurityService;

@Service
public final class MaterialRequirementPdfService extends PdfDocumentService {

    private static final String PLANNED_QUANTITY_FIELD = "plannedQuantity";

    private static final String PRODUCT_FIELD = "product";

    private static final String UNIT_FIELD = "unit";

    private static final String NUMBER_FIELD = "number";

    private static final String ORDERS_FIELD = "orders";

    private static final String DATE_FIELD = "date";

    private static final String NAME_FIELD = "name";

    private final int[] defaultMatReqHeaderColumnWidth = new int[] { 25, 25, 24, 13, 13 };

    private final int[] defaultOrderHeaderColumnWidth = new int[] { 37, 37, 13, 13 };

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = translationService.translate("materialRequirements.materialRequirement.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documenTitle, documentAuthor, (Date) entity.getField(DATE_FIELD),
                securityService.getCurrentUserName());
        addPanel(document, entity, locale);
        document.add(new Paragraph(translationService.translate("materialRequirements.materialRequirement.report.paragrah",
                locale), FontUtils.getDejavuBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("orders.order.number.label", locale));
        orderHeader.add(translationService.translate("orders.order.name.label", locale));
        orderHeader.add(translationService.translate("orders.order.product.label", locale));
        orderHeader.add(translationService.translate("basic.product.unit.label", locale));
        orderHeader.add(translationService.translate("orders.order.plannedQuantity.label", locale));
        addOrderSeries(document, entity, orderHeader);
        document.add(new Paragraph(translationService.translate("materialRequirements.materialRequirement.report.paragrah2",
                locale), FontUtils.getDejavuBold11Dark()));
        List<String> productHeader = new ArrayList<String>();
        productHeader.add(translationService.translate("basic.product.number.label", locale));
        productHeader.add(translationService.translate("basic.product.name.label", locale));
        productHeader.add(translationService.translate("basic.product.unit.label", locale));
        productHeader.add(translationService.translate("technologies.technologyOperationComponent.quantity.label", locale));
        addTechnologySeries(document, entity, productHeader);
    }

    private void addPanel(final Document document, final Entity materialRequirement, final Locale locale)
            throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialRequirements.materialRequirement.report.panel.number", locale),
                materialRequirement.getStringField("number"));
        pdfHelper
                .addTableCellAsOneColumnTable(panelTable, translationService.translate(
                        "materialRequirements.materialRequirement.report.panel.name", locale), StringUtils
                        .isEmpty(materialRequirement.getStringField("name")) ? "" : materialRequirement.getStringField("name"));
        pdfHelper.addTableCellAsOneColumnTable(
                panelTable,
                translationService.translate("materialRequirements.materialRequirement.report.panel.mrpAlgorithm", locale),
                translationService.translate(
                        "materialRequirements.materialRequirement.mrpAlgorithm.value."
                                + materialRequirement.getStringField("mrpAlgorithm"), locale));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
    }

    private void addTechnologySeries(final Document document, final Entity entity, final List<String> productHeader)
            throws DocumentException {
        List<Entity> orders = entity.getManyToManyField(ORDERS_FIELD);
        MrpAlgorithm algorithm = MrpAlgorithm.parseString(entity.getStringField(MRP_ALGORITHM));

        Map<Entity, BigDecimal> products = productQuantitiesService.getNeededProductQuantities(orders, algorithm);

        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        PdfPTable table = pdfHelper.createTableWithHeader(4, productHeader, true, defaultOrderHeaderColumnWidth);
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField(NUMBER_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
            Object unit = entry.getKey().getField(UNIT_FIELD);
            if (unit == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
            } else {
                table.addCell(new Phrase(unit.toString(), FontUtils.getDejavuRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(numberService.format(entry.getValue()), FontUtils.getDejavuBold9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        List<Entity> orders = entity.getManyToManyField(ORDERS_FIELD);
        Collections.sort(orders, new EntityOrderNumberComparator());
        PdfPTable table = pdfHelper.createTableWithHeader(5, orderHeader, true, defaultMatReqHeaderColumnWidth);

        for (Entity order : orders) {
            table.addCell(new Phrase(order.getField(NUMBER_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(order.getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
            Entity product = (Entity) order.getField(PRODUCT_FIELD);
            if (product == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
            } else {
                table.addCell(new Phrase(product.getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
            }
            if (product == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
            } else {
                Object unit = product.getField(UNIT_FIELD);
                if (unit == null) {
                    table.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
                } else {
                    table.addCell(new Phrase(unit.toString(), FontUtils.getDejavuRegular9Dark()));
                }
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            BigDecimal plannedQuantity = (BigDecimal) order.getField(PLANNED_QUANTITY_FIELD);
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            table.addCell(new Phrase(numberService.format(plannedQuantity), FontUtils.getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("materialRequirements.materialRequirement.report.title", locale);
    }

}
