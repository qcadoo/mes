/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.materialRequirements.print.pdf;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.util.EntityOrderNumberComparator;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public final class MaterialRequirementPdfService extends PdfDocumentService {

    private final int[] defaultMatReqHeaderColumnWidth = new int[] { 25, 25, 24, 13, 13 };

    private final int[] defaultOrderHeaderColumnWidth = new int[] { 37, 37, 13, 13 };

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity materialRequirement, final Locale locale)
            throws DocumentException {
        String documenTitle = translationService.translate("materialRequirements.materialRequirement.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documenTitle, documentAuthor,
                materialRequirement.getDateField(MaterialRequirementFields.DATE));

        addPanel(document, materialRequirement, locale);

        document.add(new Paragraph(translationService.translate("materialRequirements.materialRequirement.report.paragrah",
                locale), FontUtils.getDejavuBold11Dark()));

        List<String> orderHeader = Lists.newArrayList();

        orderHeader.add(translationService.translate("orders.order.number.label", locale));
        orderHeader.add(translationService.translate("orders.order.name.label", locale));
        orderHeader.add(translationService.translate("orders.order.product.label", locale));
<<<<<<< HEAD:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/internal/print/pdf/MaterialRequirementPdfService.java
        orderHeader.add(translationService.translate("materialRequirements.materialRequirement.report.order.plannedQuantity",
                locale));
        orderHeader.add(translationService.translate("materialRequirements.materialRequirement.report.product.unit", locale));
        addOrderSeries(document, entity, orderHeader);
=======
        orderHeader.add(translationService.translate("basic.product.unit.label", locale));
        orderHeader.add(translationService.translate("orders.order.plannedQuantity.label", locale));
        addOrderSeries(document, materialRequirement, orderHeader);

>>>>>>> dev:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/print/pdf/MaterialRequirementPdfService.java
        document.add(new Paragraph(translationService.translate("materialRequirements.materialRequirement.report.paragrah2",
                locale), FontUtils.getDejavuBold11Dark()));

        List<String> productHeader = Lists.newArrayList();

        productHeader.add(translationService.translate("basic.product.number.label", locale));
        productHeader.add(translationService.translate("basic.product.name.label", locale));
        productHeader.add(translationService.translate("technologies.technologyOperationComponent.quantity.label", locale));
<<<<<<< HEAD:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/internal/print/pdf/MaterialRequirementPdfService.java
        productHeader.add(translationService.translate("materialRequirements.materialRequirement.report.product.unit", locale));
        addTechnologySeries(document, entity, productHeader);
=======
        addTechnologySeries(document, materialRequirement, productHeader);
>>>>>>> dev:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/print/pdf/MaterialRequirementPdfService.java
    }

    private void addPanel(final Document document, final Entity materialRequirement, final Locale locale)
            throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialRequirements.materialRequirement.report.panel.number", locale),
                materialRequirement.getStringField(MaterialRequirementFields.NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "materialRequirements.materialRequirement.report.panel.name", locale),
                StringUtils.isEmpty(materialRequirement.getStringField(MaterialRequirementFields.NAME)) ? ""
                        : materialRequirement.getStringField(MaterialRequirementFields.NAME));
        pdfHelper.addTableCellAsOneColumnTable(
                panelTable,
                translationService.translate("materialRequirements.materialRequirement.report.panel.mrpAlgorithm", locale),
                translationService.translate(
                        "materialRequirements.materialRequirement.mrpAlgorithm.value."
                                + materialRequirement.getStringField(MaterialRequirementFields.MRP_ALGORITHM), locale));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
    }

    private void addTechnologySeries(final Document document, final Entity materialRequirement, final List<String> productHeader)
            throws DocumentException {
        List<Entity> orders = materialRequirement.getManyToManyField(MaterialRequirementFields.ORDERS);
        MrpAlgorithm algorithm = MrpAlgorithm.parseString(materialRequirement
                .getStringField(MaterialRequirementFields.MRP_ALGORITHM));

        Map<Long, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(orders, algorithm,
                true);

        // TODO LUPO fix comparator
        // neededProductQuantities = SortUtil.sortMapUsingComparator(neededProductQuantities, new EntityNumberComparator());

        PdfPTable table = pdfHelper.createTableWithHeader(4, productHeader, true, defaultOrderHeaderColumnWidth);
<<<<<<< HEAD:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/internal/print/pdf/MaterialRequirementPdfService.java
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField(NUMBER_FIELD).toString(), FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(entry.getKey().getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular7Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(numberService.format(entry.getValue()), FontUtils.getDejavuBold7Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            Object unit = entry.getKey().getField(UNIT_FIELD);
=======

        for (Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            table.addCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular9Dark()));
            String unit = product.getStringField(ProductFields.UNIT);
>>>>>>> dev:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/print/pdf/MaterialRequirementPdfService.java
            if (unit == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            } else {
<<<<<<< HEAD:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/internal/print/pdf/MaterialRequirementPdfService.java
                table.addCell(new Phrase(unit.toString(), FontUtils.getDejavuRegular7Dark()));
            }

=======
                table.addCell(new Phrase(unit, FontUtils.getDejavuRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(numberService.format(neededProductQuantity.getValue()), FontUtils.getDejavuBold9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
>>>>>>> dev:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/print/pdf/MaterialRequirementPdfService.java
        }
        document.add(table);
    }

    private void addOrderSeries(final Document document, final Entity materialRequirement, final List<String> orderHeader)
            throws DocumentException {
        List<Entity> orders = materialRequirement.getManyToManyField(MaterialRequirementFields.ORDERS);
        Collections.sort(orders, new EntityOrderNumberComparator());
        PdfPTable table = pdfHelper.createTableWithHeader(5, orderHeader, true, defaultMatReqHeaderColumnWidth);

        for (Entity order : orders) {
<<<<<<< HEAD:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/internal/print/pdf/MaterialRequirementPdfService.java
            table.addCell(new Phrase(order.getField(NUMBER_FIELD).toString(), FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(order.getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular7Dark()));
            Entity product = (Entity) order.getField(PRODUCT_FIELD);
=======
            table.addCell(new Phrase(order.getStringField(OrderFields.NUMBER), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(order.getStringField(OrderFields.NAME), FontUtils.getDejavuRegular9Dark()));
            Entity product = (Entity) order.getField(OrderFields.PRODUCT);
>>>>>>> dev:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/print/pdf/MaterialRequirementPdfService.java
            if (product == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            } else {
<<<<<<< HEAD:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/internal/print/pdf/MaterialRequirementPdfService.java
                table.addCell(new Phrase(product.getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular7Dark()));
=======
                table.addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular9Dark()));
>>>>>>> dev:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/print/pdf/MaterialRequirementPdfService.java
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            BigDecimal plannedQuantity = (BigDecimal) order.getField(PLANNED_QUANTITY_FIELD);
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            table.addCell(new Phrase(numberService.format(plannedQuantity), FontUtils.getDejavuRegular7Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            if (product == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            } else {
                String unit = product.getStringField(ProductFields.UNIT);
                if (unit == null) {
                    table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
                } else {
<<<<<<< HEAD:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/internal/print/pdf/MaterialRequirementPdfService.java
                    table.addCell(new Phrase(unit.toString(), FontUtils.getDejavuRegular7Dark()));
                }
            }
=======
                    table.addCell(new Phrase(unit, FontUtils.getDejavuRegular9Dark()));
                }
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            table.addCell(new Phrase(numberService.format(plannedQuantity), FontUtils.getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
>>>>>>> dev:mes-plugins/mes-plugins-material-requirements/src/main/java/com/qcadoo/mes/materialRequirements/print/pdf/MaterialRequirementPdfService.java
        }
        document.add(table);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("materialRequirements.materialRequirement.report.title", locale);
    }

}
