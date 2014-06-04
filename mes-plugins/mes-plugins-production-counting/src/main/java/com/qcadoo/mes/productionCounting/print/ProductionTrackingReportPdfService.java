/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionCounting.print;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingReportFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.print.utils.EntityProductionTrackingComparator;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeFields;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class ProductionTrackingReportPdfService extends PdfDocumentService {

    private static final String L_N_A = "";

    private static final String L_TABS = "\t \t \t";

    private static final String L_QCADOO_VIEW_FALSE = "qcadooView.false";

    private static final String L_QCADOO_VIEW_TRUE = "qcadooView.true";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TimeConverterService timeConverterService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private TechnologyService technologyService;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionTrackingReport, final Locale locale)
            throws DocumentException {
        final String documentTitle = translationService.translate("productionCounting.productionTrackingReport.report.title",
                locale) + " " + productionTrackingReport.getId().toString();
        final String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor,
                productionTrackingReport.getDateField(ProductionTrackingReportFields.DATE));

        final PdfPTable leftPanel = createLeftPanel(productionTrackingReport, locale);
        final PdfPTable rightPanel = createRightPanel(productionTrackingReport, locale);

        final PdfPTable panelTable = pdfHelper.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        Entity order = productionTrackingReport.getBelongsToField(ProductionTrackingReportFields.ORDER);

        final List<Entity> productionTrackings = productionCountingService.getProductionTrackingsForOrder(order);

        Collections.sort(productionTrackings, new EntityProductionTrackingComparator());

        for (Entity productionTracking : productionTrackings) {
            addProductionTracking(document, productionTracking, locale);
        }
    }

    private void addTableCellAsTable(final PdfPTable table, final String label, final Object fieldValue, final String nullValue,
            final Font headerFont, final Font valueFont, final DecimalFormat df) {
        final PdfPTable cellTable = new PdfPTable(2);
        cellTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        cellTable.addCell(new Phrase(label, headerFont));
        final Object value = fieldValue;
        if (value == null) {
            cellTable.addCell(new Phrase(nullValue, valueFont));
        } else {
            if (value instanceof BigDecimal && df != null) {
                cellTable.addCell(new Phrase(df.format(value), valueFont));
            } else {
                cellTable.addCell(new Phrase(value.toString(), valueFont));
            }
        }
        table.addCell(cellTable);
    }

    private PdfPTable createLeftPanel(final Entity productionTrackingReport, final Locale locale) {
        Entity order = productionTrackingReport.getBelongsToField(ProductionTrackingReportFields.ORDER);

        final PdfPTable leftPanel = pdfHelper.createPanelTable(1);

        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionTrackingReport.report.title", locale) + ":",
                productionTrackingReport.getId().toString(), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionTrackingReport.report.panel.order", locale),
                order.getStringField(OrderFields.NAME), null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(),
                null);
        addTableCellAsTable(
                leftPanel,
                translationService.translate("productionCounting.productionTrackingReport.report.panel.product", locale),
                productionTrackingReport.getBelongsToField(ProductionTrackingReportFields.PRODUCT).getStringField(
                        ProductFields.NAME), null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionTrackingReport.report.panel.trackingsNumber", locale),
                String.valueOf(order.getHasManyField(OrderFieldsPC.PRODUCTION_TRACKINGS).size()), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionTrackingReport.description.label", locale) + ":",
                productionTrackingReport.getStringField(ProductionTrackingReportFields.DESCRIPTION), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

        return leftPanel;
    }

    private PdfPTable createRightPanel(final Entity productionTrackingReport, final Locale locale) {
        Entity order = productionTrackingReport.getBelongsToField(ProductionTrackingReportFields.ORDER);

        final PdfPTable rightPanel = pdfHelper.createPanelTable(1);

        rightPanel.addCell(new Phrase(translationService.translate(
                "productionCounting.productionTrackingReport.report.panel.countingParameters", locale), FontUtils
                .getDejavuBold10Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate(
                        "productionCounting.productionTrackingReport.report.panel.registerQuantityOutProduct", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE, locale) : translationService.translate(L_QCADOO_VIEW_FALSE, locale)), FontUtils
                .getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate(
                        "productionCounting.productionTrackingReport.report.panel.registerQuantityInProduct", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE, locale) : translationService.translate(L_QCADOO_VIEW_FALSE, locale)), FontUtils
                .getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionTrackingReport.report.panel.registerProductionTime",
                        locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE, locale) : translationService.translate(L_QCADOO_VIEW_FALSE, locale)), FontUtils
                .getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionTrackingReport.report.panel.justOne", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.JUST_ONE) ? translationService.translate(L_QCADOO_VIEW_TRUE, locale)
                        : translationService.translate(L_QCADOO_VIEW_FALSE, locale)), FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionTrackingReport.report.panel.allowToClose", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE) ? translationService.translate(L_QCADOO_VIEW_TRUE, locale)
                        : translationService.translate(L_QCADOO_VIEW_FALSE, locale)), FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionTrackingReport.report.panel.autoCloseOrder", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.AUTO_CLOSE_ORDER) ? translationService.translate(L_QCADOO_VIEW_TRUE,
                        locale) : translationService.translate(L_QCADOO_VIEW_FALSE, locale)), FontUtils.getDejavuBold9Dark()));

        return rightPanel;
    }

    private void addProductionTracking(final Document document, final Entity productionTracking, final Locale locale)
            throws DocumentException {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        document.add(new Paragraph(translationService.translate("productionCounting.productionTrackingReport.report.paragraph",
                locale) + " " + productionTracking.getStringField(ProductionTrackingFields.NUMBER), FontUtils
                .getDejavuBold19Dark()));

        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        addTableCellAsTable(
                panelTable,
                translationService.translate("productionCounting.productionTrackingReport.report.panel.trackingType", locale),
                productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING) ? translationService.translate(
                        "productionCounting.productionTrackingReport.report.panel.trackingType.final", locale)
                        : translationService.translate(
                                "productionCounting.productionTrackingReport.report.panel.trackingType.partial", locale), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        if (productionCountingService.isTypeOfProductionRecordingCumulated(order
                .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            addTableCellAsTable(panelTable, translationService.translate(
                    "productionCounting.productionTrackingReport.report.panel.operationAndLevel", locale), L_N_A, null,
                    FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        } else {
            addTableCellAsTable(
                    panelTable,
                    translationService.translate("productionCounting.productionTrackingReport.report.panel.operationAndLevel",
                            locale),
                    productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT).getStringField(
                            TechnologyOperationComponentFields.NODE_NUMBER)
                            + " "
                            + productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)
                                    .getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                                    .getStringField(OperationFields.NAME), null, FontUtils.getDejavuBold9Dark(),
                    FontUtils.getDejavuBold9Dark(), null);
            addTableCellAsTable(
                    panelTable,
                    translationService.translate("productionCounting.productionTrackingReport.report.panel.dateAndTime", locale),
                    new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, locale).format(productionTracking
                            .getHasManyField(ProductionTrackingFields.STATE_CHANGES).get(0)
                            .getDateField(ProductionTrackingStateChangeFields.DATE_AND_TIME)), null,
                    FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
            if (order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
                addTableCellAsTable(panelTable, translationService.translate(
                        "productionCounting.productionTrackingReport.report.panel.machineOperationTime", locale),
                        timeConverterService.convertTimeToString(productionTracking
                                .getIntegerField(ProductionTrackingFields.MACHINE_TIME)), null, FontUtils.getDejavuBold9Dark(),
                        FontUtils.getDejavuBold9Dark(), null);
            } else {
                addTableCellAsTable(panelTable, translationService.translate(
                        "productionCounting.productionTrackingReport.report.panel.machineOperationTime", locale), L_N_A, null,
                        FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
            }
        }
        addTableCellAsTable(
                panelTable,
                translationService.translate("productionCounting.productionTrackingReport.report.panel.worker", locale),
                productionTracking.getHasManyField(ProductionTrackingFields.STATE_CHANGES).get(0)
                        .getStringField(ProductionTrackingStateChangeFields.WORKER), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        if (order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
            addTableCellAsTable(panelTable, translationService.translate(
                    "productionCounting.productionTrackingReport.report.panel.laborOperationTime", locale),
                    timeConverterService.convertTimeToString(productionTracking
                            .getIntegerField(ProductionTrackingFields.LABOR_TIME)), null, FontUtils.getDejavuBold9Dark(),
                    FontUtils.getDejavuBold9Dark(), null);
        } else {
            addTableCellAsTable(panelTable, translationService.translate(
                    "productionCounting.productionTrackingReport.report.panel.laborOperationTime", locale), L_N_A, null,
                    FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        }
        panelTable.setSpacingBefore(10);
        document.add(panelTable);

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
            addInputProducts(document, productionTracking, technology, locale);
        }

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
            addOutputProducts(document, productionTracking, technology, locale);
        }
    }

    private void addInputProducts(final Document document, final Entity productionTracking, final Entity technology,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService.translate("productionCounting.productionTrackingReport.report.paragraph2",
                locale), FontUtils.getDejavuBold11Dark()));

        List<String> inputProductsTableHeader = Lists.newArrayList();

        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.number", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.productionName", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.type", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.quantity", locale));
        inputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        PdfPTable inputProductsTable = pdfHelper.createTableWithHeader(5, inputProductsTableHeader, false);

        if (productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS) != null) {
            List<Entity> trackingOperationProductInComponents = Lists.newArrayList(productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS));
            Collections.sort(trackingOperationProductInComponents, new EntityProductInOutComparator());
            for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
                inputProductsTable.addCell(new Phrase(trackingOperationProductInComponent.getBelongsToField(
                        TrackingOperationProductInComponentFields.PRODUCT).getStringField(ProductFields.NUMBER), FontUtils
                        .getDejavuRegular9Dark()));
                inputProductsTable.addCell(new Phrase(trackingOperationProductInComponent.getBelongsToField(
                        TrackingOperationProductInComponentFields.PRODUCT).getStringField(ProductFields.NAME), FontUtils
                        .getDejavuRegular9Dark()));

                String type = technologyService.getProductType(
                        trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT),
                        technology);
                inputProductsTable.addCell(new Phrase(translationService.translate("basic.product.globalTypeOfMaterial.value."
                        + type, locale), FontUtils.getDejavuRegular9Dark()));

                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                if (trackingOperationProductInComponent.getField(TrackingOperationProductInComponentFields.USED_QUANTITY) == null) {
                    inputProductsTable.addCell(new Phrase(L_N_A, FontUtils.getDejavuRegular9Dark()));
                } else {
                    inputProductsTable.addCell(new Phrase(numberService.format(trackingOperationProductInComponent
                            .getField(TrackingOperationProductInComponentFields.USED_QUANTITY)), FontUtils
                            .getDejavuRegular9Dark()));
                }
                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                inputProductsTable.addCell(new Phrase(trackingOperationProductInComponent.getBelongsToField(
                        TrackingOperationProductInComponentFields.PRODUCT).getStringField(ProductFields.UNIT), FontUtils
                        .getDejavuRegular9Dark()));
            }
        }

        document.add(inputProductsTable);
    }

    private void addOutputProducts(final Document document, final Entity productionTracking, final Entity technology,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService.translate("productionCounting.productionTrackingReport.report.paragraph3",
                locale), FontUtils.getDejavuBold11Dark()));

        List<String> outputProductsTableHeader = Lists.newArrayList();

        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.number", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.productionName", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.type", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionTrackingReport.report.columnHeader.quantity", locale));
        outputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        PdfPTable outputProductsTable = pdfHelper.createTableWithHeader(5, outputProductsTableHeader, false);

        if (productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS) != null) {
            List<Entity> trackingOperationProductOutComponents = Lists.newArrayList(productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS));
            Collections.sort(trackingOperationProductOutComponents, new EntityProductInOutComparator());
            for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
                outputProductsTable.addCell(new Phrase(trackingOperationProductOutComponent.getBelongsToField(
                        TrackingOperationProductOutComponentFields.PRODUCT).getStringField(ProductFields.NUMBER), FontUtils
                        .getDejavuRegular9Dark()));
                outputProductsTable.addCell(new Phrase(trackingOperationProductOutComponent.getBelongsToField(
                        TrackingOperationProductOutComponentFields.PRODUCT).getStringField(ProductFields.NAME), FontUtils
                        .getDejavuRegular9Dark()));

                String type = technologyService.getProductType(trackingOperationProductOutComponent
                        .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT), technology);
                outputProductsTable.addCell(new Phrase(translationService.translate("basic.product.globalTypeOfMaterial.value."
                        + type, locale), FontUtils.getDejavuRegular9Dark()));

                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                if (trackingOperationProductOutComponent.getField(TrackingOperationProductOutComponentFields.USED_QUANTITY) == null) {
                    outputProductsTable.addCell(new Phrase(L_N_A, FontUtils.getDejavuRegular9Dark()));
                } else {
                    outputProductsTable.addCell(new Phrase(numberService.format(trackingOperationProductOutComponent
                            .getField(TrackingOperationProductOutComponentFields.USED_QUANTITY)), FontUtils
                            .getDejavuRegular9Dark()));
                }
                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                outputProductsTable.addCell(new Phrase(trackingOperationProductOutComponent.getBelongsToField(
                        TrackingOperationProductOutComponentFields.PRODUCT).getStringField(ProductFields.UNIT), FontUtils
                        .getDejavuRegular9Dark()));
            }
        }

        document.add(outputProductsTable);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionTrackingReport.report.title", locale);
    }

}
