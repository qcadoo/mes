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
package com.qcadoo.mes.productionCounting.print;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.BalanceOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.BalanceOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.OperationPieceworkComponentFields;
import com.qcadoo.mes.productionCounting.constants.OperationTimeComponentFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class ProductionBalancePdfService extends PdfDocumentService {

    private static final String PCPBRCHB_LITERAL = "productionCounting.productionBalance.report.columnHeader.balance";

    private static final String L_N_A = "";

    private static final String L_TABS = "\t \t \t";

    private static final String L_QCADOO_VIEW_TRUE_LITERAL = "qcadooView.true";

    private static final String L_QCADOO_VIEW_FALSE_LITERAL = "qcadooView.false";

    private static final String L_PRODUCTION_BALANCE_REPORT_COLUMN_HEADER_BALANCE = "productionCounting.productionBalance.report.columnHeader.balance";

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

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("productionCounting.productionBalance.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor,
                productionBalance.getDateField(ProductionBalanceFields.DATE));

        PdfPTable leftPanel = createLeftPanel(productionBalance, locale);
        PdfPTable rightPanel = createRightPanel(productionBalance, locale);

        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
            addInputProductsBalance(document, productionBalance, locale);
        }
        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
            addOutputProductsBalance(document, productionBalance, locale);
        }

        String calculateOperationCostMode = productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        if (productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)) {
            if (productionCountingService.isTypeOfProductionRecordingForEach(order
                    .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                addMachineTimeBalance(document, productionBalance, locale);
                addLaborTimeBalance(document, productionBalance, locale);
            } else {
                addTimeBalanceAsPanel(document, productionBalance, locale);
            }
        } else if (productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostMode)) {
            addPieceworkBalance(document, productionBalance, locale);
        }
    }

    private void addTableCellAsTable(final PdfPTable table, final String label, final Object fieldValue, final String nullValue,
            final Font headerFont, final Font valueFont, final DecimalFormat df) {
        PdfPTable cellTable = new PdfPTable(2);
        cellTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        cellTable.addCell(new Phrase(label, headerFont));
        Object value = fieldValue;
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

    public PdfPTable createLeftPanel(final Entity productionBalance, final Locale locale) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        PdfPTable leftPanel = pdfHelper.createPanelTableWithSimpleFormat(1);

        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.name", locale),
                productionBalance.getStringField(ProductionBalanceFields.NAME), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.order", locale),
                order.getStringField(OrderFields.NAME), null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(),
                null);
        addTableCellAsTable(leftPanel, translationService.translate("productionCounting.productionBalance.report.panel.product",
                locale), order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.NAME), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.trackingsNumber", locale),
                productionBalance.getIntegerField(ProductionBalanceFields.TRACKINGS_NUMBER).toString(), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionBalance.getStringField(ProductionBalanceFields.DESCRIPTION), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel, translationService.translate(
                "productionCounting.productionBalance.report.panel.typeOfProductionCounting", locale),
                translationService.translate("productionCounting.productionBalance.calculateOperationCostsMode.value."
                        + productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE), locale), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        if (productionCountingService.isCalculateOperationCostModeHourly(productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))) {
            addTableCellAsTable(
                    leftPanel,
                    L_TABS + translationService.translate("productionCounting.productionBalance.report.panel.includeTPZ", locale),
                    productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ) ? translationService.translate(
                            L_QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL,
                            locale), null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

            addTableCellAsTable(
                    leftPanel,
                    L_TABS
                            + translationService.translate(
                                    "productionCounting.productionBalance.report.panel.includeAdditionalTime", locale),
                    productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME) ? translationService
                            .translate(L_QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(
                            L_QCADOO_VIEW_FALSE_LITERAL, locale), null, FontUtils.getDejavuBold9Dark(),
                    FontUtils.getDejavuBold9Dark(), null);
        }
        return leftPanel;
    }

    public PdfPTable createRightPanel(final Entity productionBalance, final Locale locale) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        PdfPTable rightPanel = pdfHelper.createPanelTableWithSimpleFormat(1);

        rightPanel
                .addCell(new Phrase(translationService.translate(
                        "productionCounting.productionBalance.report.panel.countingParameters", locale), FontUtils
                        .getDejavuBold10Dark()));
        addTableCellAsTable(
                rightPanel,
                L_TABS
                        + translationService.translate(
                                "productionCounting.productionBalance.report.panel.typeOfProductionRecording", locale),
                translationService.translate(
                        "productionCounting.productionBalance.typeOfProductionRecording."
                                + order.getField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING), locale), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionBalance.report.panel.registerQuantityOutProduct",
                        locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionBalance.report.panel.registerQuantityInProduct",
                        locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService
                        .translate("productionCounting.productionBalance.report.panel.registerProductionTime", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionBalance.report.panel.registerPiecework", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionBalance.report.panel.justOne", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.JUST_ONE) ? translationService.translate(L_QCADOO_VIEW_TRUE_LITERAL,
                        locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL, locale)), FontUtils
                .getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionBalance.report.panel.allowToClose", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE) ? translationService.translate(L_QCADOO_VIEW_TRUE_LITERAL,
                        locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL, locale)), FontUtils
                .getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(L_TABS
                + translationService.translate("productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                + " "
                + (order.getBooleanField(OrderFieldsPC.AUTO_CLOSE_ORDER) ? translationService.translate(
                        L_QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(L_QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));

        return rightPanel;
    }

    public void addInputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        List<String> inputProductsTableHeader = Lists.newArrayList();

        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        inputProductsTableHeader.add(translationService.translate(L_PRODUCTION_BALANCE_REPORT_COLUMN_HEADER_BALANCE, locale));
        inputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        Map<String, HeaderAlignment> alignments = Maps.newHashMap();
        alignments.put(translationService.translate("productionCounting.productionBalance.report.columnHeader.number", locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("productionCounting.productionBalance.report.columnHeader.productionName", locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("productionCounting.productionBalance.report.columnHeader.usedQuantity", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(PCPBRCHB_LITERAL, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate("basic.product.unit.label", locale), HeaderAlignment.LEFT);

        List<Entity> inputProductsList = productionBalance
                .getHasManyField(ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS);

        if (!inputProductsList.isEmpty()) {
            document.add(new Paragraph(translationService.translate("productionCounting.productionBalance.report.paragraph",
                    locale), FontUtils.getDejavuBold11Dark()));

            inputProductsList = Lists.newLinkedList(inputProductsList);

            PdfPTable inputProductsTable = pdfHelper.createTableWithHeader(6, inputProductsTableHeader, false, alignments);

            for (Entity inputProduct : inputProductsList) {
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(
                        BalanceOperationProductInComponentFields.PRODUCT).getStringField(ProductFields.NUMBER), FontUtils
                        .getDejavuRegular9Dark()));
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(
                        BalanceOperationProductInComponentFields.PRODUCT).getStringField(ProductFields.NAME), FontUtils
                        .getDejavuRegular9Dark()));

                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                inputProductsTable
                        .addCell(new Phrase(numberService.format(inputProduct
                                .getField(BalanceOperationProductInComponentFields.PLANNED_QUANTITY)), FontUtils
                                .getDejavuRegular9Dark()));
                if (inputProduct.getField(BalanceOperationProductInComponentFields.USED_QUANTITY) == null) {
                    inputProductsTable.addCell(new Phrase(L_N_A, FontUtils.getDejavuRegular9Dark()));
                    inputProductsTable.addCell(new Phrase(L_N_A, FontUtils.getDejavuRegular9Dark()));
                } else {
                    inputProductsTable
                            .addCell(new Phrase(numberService.format(inputProduct
                                    .getField(BalanceOperationProductInComponentFields.USED_QUANTITY)), FontUtils
                                    .getDejavuRegular9Dark()));
                    inputProductsTable.addCell(new Phrase(numberService.format(inputProduct
                            .getField(BalanceOperationProductInComponentFields.BALANCE)), FontUtils.getDejavuRegular9Dark()));
                }
                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(
                        BalanceOperationProductInComponentFields.PRODUCT).getStringField(ProductFields.UNIT), FontUtils
                        .getDejavuRegular9Dark()));
            }

            document.add(inputProductsTable);
        }
    }

    public void addOutputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {

        List<String> outputProductsTableHeader = Lists.newArrayList();

        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.producedQuantity", locale));
        outputProductsTableHeader.add(translationService.translate(L_PRODUCTION_BALANCE_REPORT_COLUMN_HEADER_BALANCE, locale));
        outputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        Map<String, HeaderAlignment> alignments = Maps.newHashMap();
        alignments.put(translationService.translate("productionCounting.productionBalance.report.columnHeader.number", locale),
                HeaderAlignment.LEFT);

        alignments.put(
                translationService.translate("productionCounting.productionBalance.report.columnHeader.productionName", locale),
                HeaderAlignment.LEFT);

        alignments.put(
                translationService.translate("productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale),
                HeaderAlignment.RIGHT);

        alignments
                .put(translationService.translate("productionCounting.productionBalance.report.columnHeader.producedQuantity",
                        locale), HeaderAlignment.RIGHT);

        alignments.put(translationService.translate(PCPBRCHB_LITERAL, locale), HeaderAlignment.RIGHT);

        alignments.put(translationService.translate("basic.product.unit.label", locale), HeaderAlignment.LEFT);

        List<Entity> outputProductsList = productionBalance
                .getHasManyField(ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS);

        if (!outputProductsList.isEmpty()) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(translationService.translate("productionCounting.productionBalance.report.paragraph2",
                    locale), FontUtils.getDejavuBold11Dark()));

            outputProductsList = Lists.newLinkedList(outputProductsList);

            PdfPTable outputProductsTable = pdfHelper.createTableWithHeader(6, outputProductsTableHeader, false, alignments);

            for (Entity outputProduct : outputProductsList) {
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(
                        BalanceOperationProductOutComponentFields.PRODUCT).getStringField(ProductFields.NUMBER), FontUtils
                        .getDejavuRegular9Dark()));
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(
                        BalanceOperationProductOutComponentFields.PRODUCT).getStringField(ProductFields.NAME), FontUtils
                        .getDejavuRegular9Dark()));

                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                outputProductsTable
                        .addCell(new Phrase(numberService.format(outputProduct
                                .getField(BalanceOperationProductOutComponentFields.PLANNED_QUANTITY)), FontUtils
                                .getDejavuRegular9Dark()));
                if (outputProduct.getField(BalanceOperationProductOutComponentFields.USED_QUANTITY) == null) {
                    outputProductsTable.addCell(new Phrase(L_N_A, FontUtils.getDejavuRegular9Dark()));
                    outputProductsTable.addCell(new Phrase(L_N_A, FontUtils.getDejavuRegular9Dark()));
                } else {
                    outputProductsTable.addCell(new Phrase(numberService.format(outputProduct
                            .getField(BalanceOperationProductOutComponentFields.USED_QUANTITY)), FontUtils
                            .getDejavuRegular9Dark()));
                    outputProductsTable.addCell(new Phrase(numberService.format(outputProduct
                            .getField(BalanceOperationProductOutComponentFields.BALANCE)), FontUtils.getDejavuRegular9Dark()));
                }
                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(
                        BalanceOperationProductOutComponentFields.PRODUCT).getStringField(ProductFields.UNIT), FontUtils
                        .getDejavuRegular9Dark()));
            }

            document.add(outputProductsTable);
        }
    }

    public void addMachineTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                translationService.translate("productionCounting.productionBalance.report.paragraph3", locale), FontUtils
                        .getDejavuBold11Dark()));

        List<String> operationsTimeTableHeader = Lists.newArrayList();

        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationNumber", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(translationService.translate(L_PRODUCTION_BALANCE_REPORT_COLUMN_HEADER_BALANCE, locale));

        PdfPTable machineTimeTable = pdfHelper.createTableWithHeader(5, operationsTimeTableHeader, false);

        List<Entity> operationTimeComponents = productionBalance
                .getHasManyField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS);

        if (!operationTimeComponents.isEmpty()) {
            for (Entity operationTimeComponent : operationTimeComponents) {
                machineTimeTable.addCell(new Phrase(operationTimeComponent.getBelongsToField(
                        OperationTimeComponentFields.TECHNOLOGY_OPERATION_COMPONENT).getStringField(
                        TechnologyOperationComponentFields.NODE_NUMBER), FontUtils.getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(operationTimeComponent
                        .getBelongsToField(OperationTimeComponentFields.TECHNOLOGY_OPERATION_COMPONENT)
                        .getBelongsToField(TechnologyOperationComponentFields.OPERATION).getStringField(OperationFields.NUMBER),
                        FontUtils.getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(operationTimeComponent
                        .getIntegerField(OperationTimeComponentFields.PLANNED_MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(operationTimeComponent
                        .getIntegerField(OperationTimeComponentFields.MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(operationTimeComponent
                        .getIntegerField(OperationTimeComponentFields.MACHINE_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));
            }
        }

        machineTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(productionBalance
                .getIntegerField(ProductionBalanceFields.PLANNED_MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(productionBalance
                .getIntegerField(ProductionBalanceFields.MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(productionBalance
                .getIntegerField(ProductionBalanceFields.MACHINE_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));

        document.add(machineTimeTable);
    }

    public void addLaborTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                translationService.translate("productionCounting.productionBalance.report.paragraph4", locale), FontUtils
                        .getDejavuBold11Dark()));

        List<String> operationsTimeTableHeader = Lists.newArrayList();

        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationNumber", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(translationService.translate(L_PRODUCTION_BALANCE_REPORT_COLUMN_HEADER_BALANCE, locale));

        PdfPTable laborTimeTable = pdfHelper.createTableWithHeader(5, operationsTimeTableHeader, false);

        List<Entity> operationTimeComponents = productionBalance
                .getHasManyField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS);

        if (!operationTimeComponents.isEmpty()) {
            for (Entity operationTimeComponent : operationTimeComponents) {
                laborTimeTable.addCell(new Phrase(operationTimeComponent.getBelongsToField(
                        OperationTimeComponentFields.TECHNOLOGY_OPERATION_COMPONENT).getStringField(
                        TechnologyOperationComponentFields.NODE_NUMBER), FontUtils.getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(operationTimeComponent
                        .getBelongsToField(OperationTimeComponentFields.TECHNOLOGY_OPERATION_COMPONENT)
                        .getBelongsToField(TechnologyOperationComponentFields.OPERATION).getStringField(OperationFields.NUMBER),
                        FontUtils.getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(operationTimeComponent
                        .getIntegerField(OperationTimeComponentFields.PLANNED_LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(operationTimeComponent
                        .getIntegerField(OperationTimeComponentFields.LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(operationTimeComponent
                        .getIntegerField(OperationTimeComponentFields.LABOR_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));
            }
        }

        laborTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(productionBalance
                .getIntegerField(ProductionBalanceFields.PLANNED_LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(productionBalance
                .getIntegerField(ProductionBalanceFields.LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(productionBalance
                .getIntegerField(ProductionBalanceFields.LABOR_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));

        document.add(laborTimeTable);
    }

    public void addPieceworkBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                translationService.translate("productionCounting.productionBalance.report.paragraph5", locale), FontUtils
                        .getDejavuBold11Dark()));

        List<String> operationsTimeTableHeader = Lists.newArrayList();

        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationNumber", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedCycles", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredCycles", locale));
        operationsTimeTableHeader.add(translationService.translate(L_PRODUCTION_BALANCE_REPORT_COLUMN_HEADER_BALANCE, locale));

        PdfPTable pieceworkTable = pdfHelper.createTableWithHeader(5, operationsTimeTableHeader, false);

        List<Entity> operationPieceworkComponents = productionBalance
                .getHasManyField(ProductionBalanceFields.OPERATION_PIECEWORK_COMPONENTS);

        BigDecimal totalPlannedCycles = BigDecimal.ZERO;
        BigDecimal totalCycles = BigDecimal.ZERO;
        BigDecimal totalCyclesBalance = BigDecimal.ZERO;

        if (!operationPieceworkComponents.isEmpty()) {
            for (Entity operationPieceworkComponent : operationPieceworkComponents) {
                BigDecimal plannedCycles = operationPieceworkComponent
                        .getDecimalField(OperationPieceworkComponentFields.PLANNED_CYCLES);
                BigDecimal cycles = operationPieceworkComponent.getDecimalField(OperationPieceworkComponentFields.CYCLES);
                BigDecimal cyclesBalance = operationPieceworkComponent
                        .getDecimalField(OperationPieceworkComponentFields.CYCLES_BALANCE);

                pieceworkTable.addCell(new Phrase(operationPieceworkComponent.getBelongsToField(
                        OperationTimeComponentFields.TECHNOLOGY_OPERATION_COMPONENT).getStringField(
                        TechnologyOperationComponentFields.NODE_NUMBER), FontUtils.getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(operationPieceworkComponent
                        .getBelongsToField(OperationTimeComponentFields.TECHNOLOGY_OPERATION_COMPONENT)
                        .getBelongsToField(TechnologyOperationComponentFields.OPERATION).getStringField(OperationFields.NUMBER),
                        FontUtils.getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(numberService.format(plannedCycles), FontUtils.getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(numberService.format(cycles), FontUtils.getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(numberService.format(cyclesBalance), FontUtils.getDejavuRegular9Dark()));

                totalPlannedCycles = totalPlannedCycles.add(plannedCycles, numberService.getMathContext());
                totalCycles = totalPlannedCycles.add(cycles, numberService.getMathContext());
                totalCyclesBalance = totalPlannedCycles.add(cyclesBalance, numberService.getMathContext());
            }
        }

        pieceworkTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase(numberService.format(totalPlannedCycles), FontUtils.getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase(numberService.format(totalCycles), FontUtils.getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase(numberService.format(totalCyclesBalance), FontUtils.getDejavuRegular9Dark()));

        document.add(pieceworkTable);
    }

    public void addTimeBalanceAsPanel(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate(
                "productionCounting.productionBalanceDetails.window.productionTime", locale), FontUtils.getDejavuBold11Dark()));

        PdfPTable timePanel = pdfHelper.createPanelTable(3);

        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.plannedMachineTime",
                        locale)
                        + ":", timeConverterService.convertTimeToString(productionBalance
                        .getIntegerField(ProductionBalanceFields.PLANNED_MACHINE_TIME)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.machineTime", locale)
                        + ":", timeConverterService.convertTimeToString(productionBalance
                        .getIntegerField(ProductionBalanceFields.MACHINE_TIME)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.machineTimeBalance",
                        locale)
                        + ":", timeConverterService.convertTimeToString(productionBalance
                        .getIntegerField(ProductionBalanceFields.MACHINE_TIME_BALANCE)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.plannedLaborTime",
                        locale)
                        + ":", timeConverterService.convertTimeToString(productionBalance
                        .getIntegerField(ProductionBalanceFields.PLANNED_LABOR_TIME)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.laborTime", locale)
                        + ":", timeConverterService.convertTimeToString(productionBalance
                        .getIntegerField(ProductionBalanceFields.LABOR_TIME)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.laborTimeBalance",
                        locale)
                        + ":", timeConverterService.convertTimeToString(productionBalance
                        .getIntegerField(ProductionBalanceFields.LABOR_TIME_BALANCE)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);

        timePanel.setSpacingBefore(10);
        document.add(timePanel);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.title", locale);
    }

}
