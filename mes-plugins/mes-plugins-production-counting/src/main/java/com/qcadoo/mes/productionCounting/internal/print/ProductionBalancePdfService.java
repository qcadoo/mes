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
package com.qcadoo.mes.productionCounting.internal.print;

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.BalanceOperationProductInComponentFields.PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields.CYCLES;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields.CYCLES_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields.PLANNED_CYCLES;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.LABOR_TIME_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.MACHINE_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.MACHINE_TIME_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.PLANNED_LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.PLANNED_MACHINE_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DESCRIPTION;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_TPZ;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.NAME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.RECORDS_NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class ProductionBalancePdfService extends PdfDocumentService {

    private static final String PCPBRCHB_LITERAL = "productionCounting.productionBalance.report.columnHeader.balance";

    private static final String TECHNOLOGY_INSTANCE_OPERATION_COMPONENT_LITERAL = "technologyInstanceOperationComponent";

    private static final String N_A_LITERAL = "";

    private static final String USED_QUANTITY_LITERAL = "usedQuantity";

    private static final String QCADOO_VIEW_TRUE_LITERAL = "qcadooView.true";

    private static final String QCADOO_VIEW_FALSE_LITERAL = "qcadooView.false";

    private static final String TABS_LITERAL = "\t \t \t";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TimeConverterService timeConverterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("productionCounting.productionBalance.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionBalance.getField("date"));

        PdfPTable leftPanel = createLeftPanel(productionBalance, locale);
        PdfPTable rightPanel = createRightPanel(productionBalance, locale);

        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        if (productionBalance.getBelongsToField(ORDER).getBooleanField("registerQuantityInProduct")) {
            addInputProductsBalance(document, productionBalance, locale);
        }
        if (productionBalance.getBelongsToField(ORDER).getBooleanField("registerQuantityOutProduct")) {
            addOutputProductsBalance(document, productionBalance, locale);
        }

        if (HOURLY.getStringValue().equals(
                productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))) {
            if (FOR_EACH.getStringValue().equals(
                    productionBalance.getBelongsToField(ORDER).getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                addMachineTimeBalance(document, productionBalance, locale);
                addLaborTimeBalance(document, productionBalance, locale);
            } else {
                addTimeBalanceAsPanel(document, productionBalance, locale);
            }
        } else if (CalculateOperationCostsMode.PIECEWORK.getStringValue().equals(
                productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))) {
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
        PdfPTable leftPanel = pdfHelper.createPanelTable(1);

        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.name", locale),
                productionBalance.getStringField(NAME), null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(),
                null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.order", locale),
                productionBalance.getBelongsToField(ORDER).getStringField(NAME), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.product", locale),
                productionBalance.getBelongsToField(ORDER).getBelongsToField(OrderFields.PRODUCT).getStringField(NAME), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                productionBalance.getField(RECORDS_NUMBER).toString(), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionBalance.getStringField(DESCRIPTION), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel, translationService.translate(
                "productionCounting.productionBalance.report.panel.typeOfProductionCounting", locale),
                translationService.translate("productionCounting.productionBalance.calculateOperationCostsMode.value."
                        + productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE), locale), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        if (productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE).equals(
                HOURLY.getStringValue())) {

            addTableCellAsTable(
                    leftPanel,
                    TABS_LITERAL
                            + translationService
                                    .translate("productionCounting.productionBalance.report.panel.includeTPZ", locale),
                    productionBalance.getBooleanField(INCLUDE_TPZ) ? translationService.translate(QCADOO_VIEW_TRUE_LITERAL,
                            locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale), null,
                    FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

            addTableCellAsTable(
                    leftPanel,
                    TABS_LITERAL
                            + translationService.translate(
                                    "productionCounting.productionBalance.report.panel.includeAdditionalTime", locale),
                    productionBalance.getBooleanField(INCLUDE_ADDITIONAL_TIME) ? translationService.translate(
                            QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale),
                    null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        }
        return leftPanel;
    }

    public PdfPTable createRightPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable rightPanel = pdfHelper.createPanelTable(1);

        rightPanel
                .addCell(new Phrase(translationService.translate(
                        "productionCounting.productionBalance.report.panel.countingParameters", locale), FontUtils
                        .getDejavuBold10Dark()));
        addTableCellAsTable(
                rightPanel,
                TABS_LITERAL
                        + translationService.translate(
                                "productionCounting.productionBalance.report.panel.typeOfProductionRecording", locale),
                translationService.translate("productionCounting.productionBalance.typeOfProductionRecording."
                        + productionBalance.getBelongsToField(ORDER).getField(TYPE_OF_PRODUCTION_RECORDING), locale), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.registerQuantityOutProduct",
                        locale)
                + " "
                + (productionBalance.getBelongsToField(ORDER).getBooleanField("registerQuantityInProduct") ? translationService
                        .translate(QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL,
                        locale)), FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.registerQuantityInProduct",
                        locale)
                + " "
                + (productionBalance.getBelongsToField(ORDER).getBooleanField("registerQuantityOutProduct") ? translationService
                        .translate(QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL,
                        locale)), FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService
                        .translate("productionCounting.productionBalance.report.panel.registerProductionTime", locale)
                + " "
                + (productionBalance.getBelongsToField(ORDER).getBooleanField("registerProductionTime") ? translationService
                        .translate(QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL,
                        locale)), FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.registerPiecework", locale)
                + " "
                + (productionBalance.getBelongsToField(ORDER).getBooleanField("registerPiecework") ? translationService
                        .translate(QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL,
                        locale)), FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.justOne", locale)
                + " "
                + (productionBalance.getBelongsToField(ORDER).getBooleanField("justOne") ? translationService.translate(
                        QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.allowToClose", locale)
                + " "
                + (productionBalance.getBelongsToField(ORDER).getBooleanField("allowToClose") ? translationService.translate(
                        QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                + " "
                + (productionBalance.getBelongsToField(ORDER).getBooleanField("autoCloseOrder") ? translationService.translate(
                        QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale)),
                FontUtils.getDejavuBold9Dark()));

        return rightPanel;
    }

    public void addInputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {

        List<String> inputProductsTableHeader = new ArrayList<String>();
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        inputProductsTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));
        inputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        List<Entity> inputProductsList = productionBalance
                .getHasManyField(ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS);

        if (!inputProductsList.isEmpty()) {
            document.add(new Paragraph(translationService.translate("productionCounting.productionBalance.report.paragraph",
                    locale), FontUtils.getDejavuBold11Dark()));

            // TODO mici, same thing again.
            inputProductsList = Lists.newLinkedList(inputProductsList);

            PdfPTable inputProductsTable = pdfHelper.createTableWithHeader(6, inputProductsTableHeader, false);

            for (Entity inputProduct : inputProductsList) {
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(PRODUCT).getStringField(NUMBER), FontUtils
                        .getDejavuRegular9Dark()));
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(PRODUCT).getStringField(NAME), FontUtils
                        .getDejavuRegular9Dark()));

                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                inputProductsTable.addCell(new Phrase(numberService.format(inputProduct.getField("plannedQuantity")), FontUtils
                        .getDejavuRegular9Dark()));
                if (inputProduct.getField(USED_QUANTITY_LITERAL) == null) {
                    inputProductsTable.addCell(new Phrase(N_A_LITERAL, FontUtils.getDejavuRegular9Dark()));
                    inputProductsTable.addCell(new Phrase(N_A_LITERAL, FontUtils.getDejavuRegular9Dark()));
                } else {
                    inputProductsTable.addCell(new Phrase(numberService.format(inputProduct.getField(USED_QUANTITY_LITERAL)),
                            FontUtils.getDejavuRegular9Dark()));
                    inputProductsTable.addCell(new Phrase(numberService.format(inputProduct.getField("balance")), FontUtils
                            .getDejavuRegular9Dark()));
                }
                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(PRODUCT).getStringField("unit"), FontUtils
                        .getDejavuRegular9Dark()));
            }

            document.add(inputProductsTable);
        }
    }

    public void addOutputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {

        List<String> outputProductsTableHeader = new ArrayList<String>();
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.producedQuantity", locale));
        outputProductsTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));
        outputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        List<Entity> outputProductsList = productionBalance
                .getHasManyField(ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS);

        if (!outputProductsList.isEmpty()) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(translationService.translate("productionCounting.productionBalance.report.paragraph2",
                    locale), FontUtils.getDejavuBold11Dark()));

            // TODO mici, same thing again.
            outputProductsList = Lists.newLinkedList(outputProductsList);

            PdfPTable outputProductsTable = pdfHelper.createTableWithHeader(6, outputProductsTableHeader, false);

            for (Entity outputProduct : outputProductsList) {
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(PRODUCT).getStringField(NUMBER), FontUtils
                        .getDejavuRegular9Dark()));
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(PRODUCT).getStringField(NAME), FontUtils
                        .getDejavuRegular9Dark()));

                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                outputProductsTable.addCell(new Phrase(numberService.format(outputProduct.getField("plannedQuantity")), FontUtils
                        .getDejavuRegular9Dark()));
                if (outputProduct.getField(USED_QUANTITY_LITERAL) == null) {
                    outputProductsTable.addCell(new Phrase(N_A_LITERAL, FontUtils.getDejavuRegular9Dark()));
                    outputProductsTable.addCell(new Phrase(N_A_LITERAL, FontUtils.getDejavuRegular9Dark()));
                } else {
                    outputProductsTable.addCell(new Phrase(numberService.format(outputProduct.getField(USED_QUANTITY_LITERAL)),
                            FontUtils.getDejavuRegular9Dark()));
                    outputProductsTable.addCell(new Phrase(numberService.format(outputProduct.getField("balance")), FontUtils
                            .getDejavuRegular9Dark()));
                }
                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(PRODUCT).getStringField("unit"), FontUtils
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

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationNumber", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));

        PdfPTable machineTimeTable = pdfHelper.createTableWithHeader(5, operationsTimeTableHeader, false);

        List<Entity> operationTimeComponents = productionBalance
                .getHasManyField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS);

        if (!operationTimeComponents.isEmpty()) {
            for (Entity operationTimeComponent : operationTimeComponents) {
                machineTimeTable.addCell(new Phrase(operationTimeComponent.getBelongsToField(
                        TECHNOLOGY_INSTANCE_OPERATION_COMPONENT_LITERAL).getStringField("nodeNumber"), FontUtils
                        .getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(operationTimeComponent
                        .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT_LITERAL).getBelongsToField("operation")
                        .getStringField("number"), FontUtils.getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) operationTimeComponent
                        .getField(PLANNED_MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) operationTimeComponent
                        .getField(MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
                machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) operationTimeComponent
                        .getField(MACHINE_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));
            }
        }

        machineTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionBalance
                .getField(PLANNED_MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionBalance
                .getField(MACHINE_TIME)), FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionBalance
                .getField(MACHINE_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));

        document.add(machineTimeTable);
    }

    public void addLaborTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                translationService.translate("productionCounting.productionBalance.report.paragraph4", locale), FontUtils
                        .getDejavuBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationNumber", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));

        PdfPTable laborTimeTable = pdfHelper.createTableWithHeader(5, operationsTimeTableHeader, false);

        List<Entity> operationTimeComponents = productionBalance
                .getHasManyField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS);

        if (!operationTimeComponents.isEmpty()) {
            for (Entity operationTimeComponent : operationTimeComponents) {
                laborTimeTable.addCell(new Phrase(operationTimeComponent.getBelongsToField(
                        TECHNOLOGY_INSTANCE_OPERATION_COMPONENT_LITERAL).getStringField("nodeNumber"), FontUtils
                        .getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(operationTimeComponent
                        .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT_LITERAL).getBelongsToField("operation")
                        .getStringField("number"), FontUtils.getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) operationTimeComponent
                        .getField(PLANNED_LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) operationTimeComponent
                        .getField(LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
                laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) operationTimeComponent
                        .getField(LABOR_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));
            }
        }

        laborTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionBalance
                .getField(PLANNED_LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionBalance
                .getField(LABOR_TIME)), FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionBalance
                .getField(LABOR_TIME_BALANCE)), FontUtils.getDejavuRegular9Dark()));

        document.add(laborTimeTable);
    }

    public void addPieceworkBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                translationService.translate("productionCounting.productionBalance.report.paragraph5", locale), FontUtils
                        .getDejavuBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.operationNumber", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedCycles", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredCycles", locale));
        operationsTimeTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));

        PdfPTable pieceworkTable = pdfHelper.createTableWithHeader(5, operationsTimeTableHeader, false);

        List<Entity> operationPieceworkComponents = productionBalance
                .getHasManyField(ProductionBalanceFields.OPERATION_PIECEWORK_COMPONENTS);

        if (!operationPieceworkComponents.isEmpty()) {
            for (Entity operationPieceworkComponent : operationPieceworkComponents) {
                pieceworkTable.addCell(new Phrase(operationPieceworkComponent.getBelongsToField(
                        TECHNOLOGY_INSTANCE_OPERATION_COMPONENT_LITERAL).getStringField("nodeNumber"), FontUtils
                        .getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(operationPieceworkComponent
                        .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT_LITERAL).getBelongsToField("operation")
                        .getStringField("number"), FontUtils.getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(numberService.format(operationPieceworkComponent.getField(PLANNED_CYCLES)),
                        FontUtils.getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(numberService.format(operationPieceworkComponent.getField(CYCLES)), FontUtils
                        .getDejavuRegular9Dark()));
                pieceworkTable.addCell(new Phrase(numberService.format(operationPieceworkComponent.getField(CYCLES_BALANCE)),
                        FontUtils.getDejavuRegular9Dark()));
            }
        }

        pieceworkTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase(numberService.format(productionBalance.getField(PLANNED_CYCLES)), FontUtils
                .getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase(numberService.format(productionBalance.getField(CYCLES)), FontUtils
                .getDejavuRegular9Dark()));
        pieceworkTable.addCell(new Phrase(numberService.format(productionBalance.getField(CYCLES_BALANCE)), FontUtils
                .getDejavuRegular9Dark()));

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
                        + ":", timeConverterService.convertTimeToString((Integer) productionBalance
                        .getField(PLANNED_MACHINE_TIME)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.machineTime", locale)
                        + ":", timeConverterService.convertTimeToString((Integer) productionBalance.getField(MACHINE_TIME)),
                null, FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.machineTimeBalance",
                        locale)
                        + ":", timeConverterService.convertTimeToString((Integer) productionBalance
                        .getField(MACHINE_TIME_BALANCE)), null, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.plannedLaborTime",
                        locale)
                        + ":",
                timeConverterService.convertTimeToString((Integer) productionBalance.getField(PLANNED_LABOR_TIME)), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.laborTime", locale)
                        + ":", timeConverterService.convertTimeToString((Integer) productionBalance.getField(LABOR_TIME)), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.workTab.operationsTime.column.laborTimeBalance",
                        locale)
                        + ":",
                timeConverterService.convertTimeToString((Integer) productionBalance.getField(LABOR_TIME_BALANCE)), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);

        timePanel.setSpacingBefore(10);
        document.add(timePanel);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.title", locale);
    }

}
