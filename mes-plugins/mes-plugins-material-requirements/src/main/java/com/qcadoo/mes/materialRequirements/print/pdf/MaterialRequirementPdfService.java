/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialRequirements.print.pdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementProductFields;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementDataService;
import com.qcadoo.mes.materialRequirements.util.EntityOrderNumberComparator;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Service
public final class MaterialRequirementPdfService extends PdfDocumentService {

    private final int[] defaultMatReqHeaderColumnWidth = new int[]{25, 25, 24, 13, 13};

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialRequirementDataService materialRequirementDataService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    private Map<Entity, List<Entity>> replacements = new HashMap<>();

    public void generateDocument(final Entity entity, Map<Entity, List<Entity>> replacements, final Locale locale) throws IOException, DocumentException {
        this.replacements = replacements;
        generateDocument(entity, locale, PageSize.A4);
    }

    @Override
    protected void buildPdfContent(final Document document, final Entity materialRequirement, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("materialRequirements.materialRequirement.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor,
                materialRequirement.getDateField(MaterialRequirementFields.DATE));

        addPanel(document, materialRequirement, locale);

        document.add(
                new Paragraph(translationService.translate("materialRequirements.materialRequirement.report.paragrah", locale),
                        FontUtils.getDejavuBold11Dark()));

        Map<String, HeaderAlignment> orderHeadersWithAlignments = Maps.newLinkedHashMap();

        orderHeadersWithAlignments.put(translationService.translate("orders.order.number.label", locale), HeaderAlignment.LEFT);
        orderHeadersWithAlignments.put(translationService.translate("orders.order.name.label", locale), HeaderAlignment.LEFT);
        orderHeadersWithAlignments.put(translationService.translate("orders.order.product.label", locale), HeaderAlignment.LEFT);
        orderHeadersWithAlignments.put(
                translationService.translate("materialRequirements.materialRequirement.report.order.plannedQuantity", locale),
                HeaderAlignment.RIGHT);
        orderHeadersWithAlignments.put(
                translationService.translate("materialRequirements.materialRequirement.report.product.unit", locale),
                HeaderAlignment.LEFT);

        addOrderSeries(document, materialRequirement, orderHeadersWithAlignments);

        document.add(
                new Paragraph(translationService.translate("materialRequirements.materialRequirement.report.paragrah2", locale),
                        FontUtils.getDejavuBold11Dark()));

        addDataSeries(document, materialRequirement, locale);
    }

    private void addPanel(final Document document, final Entity materialRequirement, final Locale locale)
            throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(2);

        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialRequirements.materialRequirement.report.panel.number", locale),
                materialRequirement.getStringField(MaterialRequirementFields.NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialRequirements.materialRequirement.report.panel.name", locale),
                StringUtils.isEmpty(materialRequirement.getStringField(MaterialRequirementFields.NAME)) ? ""
                        : materialRequirement.getStringField(MaterialRequirementFields.NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialRequirements.materialRequirement.report.panel.mrpAlgorithm", locale),
                translationService.translate("materialRequirements.materialRequirement.mrpAlgorithm.value."
                        + materialRequirement.getStringField(MaterialRequirementFields.MRP_ALGORITHM), locale));
        Entity stockLevelLocation = materialRequirement.getBelongsToField(MaterialRequirementFields.STOCK_LEVEL_LOCATION);
        if (stockLevelLocation != null) {
            pdfHelper.addTableCellAsOneColumnTable(panelTable,
                    translationService.translate("materialRequirements.materialRequirement.report.panel.stockLevelLocationNumber", locale),
                    stockLevelLocation.getStringField(LocationFields.NUMBER));
        } else {
            pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");
        }

        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);

        document.add(panelTable);
    }

    private void addOrderSeries(final Document document, final Entity materialRequirement,
                                final Map<String, HeaderAlignment> headersWithAlignments) throws DocumentException {
        List<Entity> orders = materialRequirement.getManyToManyField(MaterialRequirementFields.ORDERS);

        orders.sort(new EntityOrderNumberComparator());

        List<String> headers = Lists.newLinkedList(headersWithAlignments.keySet());

        PdfPTable table = pdfHelper.createTableWithHeader(headersWithAlignments.size(), headers, true,
                defaultMatReqHeaderColumnWidth, headersWithAlignments);

        for (Entity order : orders) {
            table.addCell(new Phrase(order.getStringField(OrderFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(order.getStringField(OrderFields.NAME), FontUtils.getDejavuRegular7Dark()));

            Entity product = (Entity) order.getField(OrderFields.PRODUCT);

            if (Objects.nonNull(product)) {
                BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                plannedQuantity = Objects.isNull(plannedQuantity) ? BigDecimal.ZERO : plannedQuantity;

                table.addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(numberService.format(plannedQuantity), FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                String unit = product.getStringField(ProductFields.UNIT);

                if (Objects.isNull(unit)) {
                    table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
                } else {
                    table.addCell(new Phrase(unit, FontUtils.getDejavuRegular7Dark()));
                }
            } else {
                BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                plannedQuantity = Objects.isNull(plannedQuantity) ? BigDecimal.ZERO : plannedQuantity;

                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(numberService.format(plannedQuantity), FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            }
        }

        document.add(table);
    }

    private void addDataSeries(final Document document, final Entity materialRequirement, final Locale locale)
            throws DocumentException {
        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        boolean showCurrentStockLevel = materialRequirement.getBooleanField(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);

        List<Integer> defaultOrderHeaderColumnWidth = Lists.newArrayList();

        Map<String, HeaderAlignment> headersWithAlignments = Maps.newLinkedHashMap();

        if (includeWarehouse) {
            defaultOrderHeaderColumnWidth.add(20);
            headersWithAlignments.put(
                    translationService.translate("materialRequirements.materialRequirement.report.locationNumber", locale),
                    HeaderAlignment.LEFT);
        }

        if (includeStartDateOrder) {
            defaultOrderHeaderColumnWidth.add(20);
            headersWithAlignments.put(
                    translationService.translate("materialRequirements.materialRequirement.report.orderStartDate", locale),
                    HeaderAlignment.LEFT);
        }

        defaultOrderHeaderColumnWidth.add(30);
        headersWithAlignments.put(
                translationService.translate("materialRequirements.materialRequirement.report.productNumber", locale),
                HeaderAlignment.LEFT);

        defaultOrderHeaderColumnWidth.add(30);
        headersWithAlignments.put(
                translationService.translate("materialRequirements.materialRequirement.report.productName", locale),
                HeaderAlignment.LEFT);

        defaultOrderHeaderColumnWidth.add(19);
        headersWithAlignments.put(
                translationService.translate("materialRequirements.materialRequirement.report.quantity", locale),
                HeaderAlignment.RIGHT);
        defaultOrderHeaderColumnWidth.add(13);

        headersWithAlignments.put(
                translationService.translate("materialRequirements.materialRequirement.report.product.unit", locale),
                HeaderAlignment.LEFT);

        if (showCurrentStockLevel) {
            defaultOrderHeaderColumnWidth.add(23);
            headersWithAlignments.put(
                    translationService.translate("materialRequirements.materialRequirement.report.currentStock", locale),
                    HeaderAlignment.RIGHT);
        }

        defaultOrderHeaderColumnWidth.add(20);
        headersWithAlignments.put(
                translationService.translate("materialRequirements.materialRequirement.report.batch", locale),
                HeaderAlignment.LEFT);

        if (showCurrentStockLevel) {
            defaultOrderHeaderColumnWidth.add(23);
            headersWithAlignments.put(
                    translationService.translate("materialRequirements.materialRequirement.report.batchStock", locale),
                    HeaderAlignment.RIGHT);
        }

        List<String> headers = Lists.newLinkedList(headersWithAlignments.keySet());

        int[] defaultOrderHeaderColumnWidthInt = new int[defaultOrderHeaderColumnWidth.size()];
        for (int i = 0; i < defaultOrderHeaderColumnWidth.size(); i++) {
            defaultOrderHeaderColumnWidthInt[i] = defaultOrderHeaderColumnWidth.get(i);
        }

        PdfPTable table = pdfHelper.createTableWithHeader(headersWithAlignments.size(), headers, true,
                defaultOrderHeaderColumnWidthInt, headersWithAlignments);

        List<Entity> materialRequirementProducts = Lists.newArrayList(materialRequirement.getHasManyField(MaterialRequirementFields.MATERIAL_REQUIREMENT_PRODUCTS));

        if (includeWarehouse) {
            materialRequirementProducts.sort(Comparator.comparing(MaterialRequirementPdfService::extractLocationNumber, nullsFirst(naturalOrder()))
                    .thenComparing(MaterialRequirementPdfService::extractOrderStartDate, nullsFirst(naturalOrder()))
                    .thenComparing(MaterialRequirementPdfService::extractProductNumber)
            );
        } else {
            materialRequirementProducts.sort(Comparator.comparing(MaterialRequirementPdfService::extractOrderStartDate, nullsFirst(naturalOrder()))
                    .thenComparing(MaterialRequirementPdfService::extractProductNumber));
        }

        String actualProduct = null;
        String actualLocation = "";
        Date actualDate = null;

        for (Entity materialRequirementProduct : materialRequirementProducts) {
            Entity product = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.PRODUCT);
            Entity location = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.LOCATION);
            Entity batch = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.BATCH);
            BigDecimal quantity = materialRequirementProduct.getDecimalField(MaterialRequirementProductFields.QUANTITY);
            BigDecimal currentStock = materialRequirementProduct.getDecimalField(MaterialRequirementProductFields.CURRENT_STOCK);
            BigDecimal batchStock = materialRequirementProduct.getDecimalField(MaterialRequirementProductFields.BATCH_STOCK);
            Date orderStartDate = materialRequirementProduct.getDateField(MaterialRequirementProductFields.ORDER_START_DATE);
            String productNumber = product.getStringField(ProductFields.NUMBER);
            String productName = product.getStringField(ProductFields.NAME);
            String unit = product.getStringField(ProductFields.UNIT);

            table.getDefaultCell().disableBorderSide(PdfCell.BOTTOM);
            table.getDefaultCell().disableBorderSide(PdfCell.TOP);

            boolean fillDateIfWarehouseChanged = false;

            if (includeWarehouse) {
                String locationNumber = "";

                if (Objects.nonNull(location)) {
                    locationNumber = location.getStringField(LocationFields.NUMBER);
                }

                if (!actualLocation.equals(locationNumber)) {
                    table.getDefaultCell().enableBorderSide(PdfCell.TOP);
                    table.addCell(new Phrase(locationNumber, FontUtils.getDejavuRegular7Dark()));

                    actualLocation = locationNumber;

                    fillDateIfWarehouseChanged = true;
                } else {
                    table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
                }
            }

            if (includeStartDateOrder) {
                if (Objects.isNull(actualDate) || !actualDate.equals(orderStartDate) || fillDateIfWarehouseChanged) {
                    if (Objects.nonNull(orderStartDate)) {
                        table.getDefaultCell().enableBorderSide(PdfCell.TOP);
                        table.addCell(new Phrase(DateUtils.toDateString(orderStartDate), FontUtils.getDejavuRegular7Dark()));

                        actualDate = new Date(orderStartDate.getTime());
                    } else {
                        table.getDefaultCell().enableBorderSide(PdfCell.TOP);
                        table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));

                        actualDate = null;
                    }
                } else {
                    table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
                }
            }

            List<Entity> substituteComponents = replacements.get(materialRequirementProduct);

            if (substituteComponents == null) {
                table.getDefaultCell().enableBorderSide(PdfCell.BOTTOM);
            }
            table.getDefaultCell().enableBorderSide(PdfCell.TOP);

            if (Objects.isNull(actualProduct) || !actualProduct.equals(productNumber)) {
                table.addCell(new Phrase(productNumber, FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase(productName, FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(numberService.format(quantity), FontUtils.getDejavuBold7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(new Phrase(unit, FontUtils.getDejavuRegular7Dark()));

                if (showCurrentStockLevel) {
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(new Phrase(numberService.format(currentStock), FontUtils.getDejavuBold7Dark()));
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                }

                actualProduct = productNumber;
            } else {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase("", FontUtils.getDejavuBold7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));

                if (showCurrentStockLevel) {
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(new Phrase("", FontUtils.getDejavuBold7Dark()));
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                }
            }

            if (Objects.nonNull(batch)) {
                table.addCell(new Phrase(batch.getStringField(BatchFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            }

            if (showCurrentStockLevel) {
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(numberService.format(batchStock), FontUtils.getDejavuBold7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            }

            if (substituteComponents != null) {
                table.getDefaultCell().disableBorderSide(PdfCell.BOTTOM);
                table.getDefaultCell().disableBorderSide(PdfCell.TOP);
                for (Entity substituteComponent : substituteComponents) {
                    if (includeWarehouse) {
                        table.addCell(new Phrase("", FontUtils.getDejavuRegular7Light()));
                    }

                    if (includeStartDateOrder) {
                        table.addCell(new Phrase("", FontUtils.getDejavuRegular7Light()));
                    }

                    product = substituteComponent.getBelongsToField(MaterialRequirementProductFields.PRODUCT);
                    table.addCell(new Phrase("- " + product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Light()));
                    table.addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Light()));
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(new Phrase(numberService.format(substituteComponent.getDecimalField(MaterialRequirementProductFields.QUANTITY)), FontUtils.getDejavuRegular7Light()));
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.addCell(new Phrase(product.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Light()));

                    if (showCurrentStockLevel) {
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        table.addCell(new Phrase(numberService.format(substituteComponent.getDecimalField(MaterialRequirementProductFields.CURRENT_STOCK)), FontUtils.getDejavuRegular7Light()));
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    table.addCell(new Phrase("", FontUtils.getDejavuRegular7Light()));
                    if (showCurrentStockLevel) {
                        table.addCell(new Phrase("", FontUtils.getDejavuRegular7Light()));
                    }
                }
            }
        }

        document.add(table);
    }

    private static String extractProductNumber(final Entity materialRequirementProduct) {
        Entity product = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.PRODUCT);

        return Objects.nonNull(product) ? product.getStringField(ProductFields.NUMBER) : null;
    }

    private static String extractLocationNumber(final Entity materialRequirementProduct) {
        Entity location = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.LOCATION);

        return Objects.nonNull(location) ? location.getStringField(LocationFields.NUMBER) : null;
    }

    private static Date extractOrderStartDate(final Entity materialRequirementProduct) {
        return materialRequirementProduct.getDateField(MaterialRequirementProductFields.ORDER_START_DATE);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("materialRequirements.materialRequirement.report.title", locale);
    }

}
