/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DESCRIPTION;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_TPZ;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.NAME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.RECORDS_NUMBER;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceReportDataService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public final class ProductionBalancePdfService extends PdfDocumentService {

    private static final String PCPBRCHB_LITERAL = "productionCounting.productionBalance.report.columnHeader.balance";

    private static final String ORDER_OPERATION_COMPONENT_LITERAL = "orderOperationComponent";

    private static final String N_A_LITERAL = "N/A";

    private static final String USED_QUANTITY_LITERAL = "usedQuantity";

    private static final String STATE_LITERAL = "state";

    private static final String QCADOO_VIEW_TRUE_LITERAL = "qcadooView.true";

    private static final String QCADOO_VIEW_FALSE_LITERAL = "qcadooView.false";

    private static final String TABS_LITERAL = "\t \t \t";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

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
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionBalance.getField("date"),
                securityService.getCurrentUserName());

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

        if (productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE).equals(HOURLY.getStringValue())) {
            if (productionBalance.getBelongsToField(ORDER).getStringField("typeOfProductionRecording").equals("03forEach")) {
                addMachineTimeBalance(document, productionBalance, locale);
                addLaborTimeBalance(document, productionBalance, locale);
            } else {
                addTimeBalanceAsPanel(document, productionBalance, locale);
            }
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
                translationService.translate("productionCounting.productionBalance.report.panel.name", locale) + ":",
                productionBalance.getStringField(NAME), null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(),
                null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.order", locale),
                productionBalance.getBelongsToField(ORDER).getStringField(NAME), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.product", locale),
                productionBalance.getBelongsToField(PRODUCT).getStringField(NAME), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                productionBalance.getField(RECORDS_NUMBER).toString(), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionBalance.getStringField(DESCRIPTION), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);
        if (productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE).equals(
                HOURLY.getStringValue())) {
            addTableCellAsTable(leftPanel, translationService.translate(
                    "productionCounting.productionBalance.report.panel.typeOfProductionCounting", locale), null, null,
                    FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
            addTableCellAsTable(
                    leftPanel,
                    TABS_LITERAL
                            + translationService
                                    .translate("productionCounting.productionBalance.report.panel.includeTPZ", locale) + ":",
                    productionBalance.getBooleanField(INCLUDE_TPZ) ? translationService.translate(QCADOO_VIEW_TRUE_LITERAL,
                            locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale), null,
                    FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

            addTableCellAsTable(
                    leftPanel,
                    TABS_LITERAL
                            + translationService.translate(
                                    "productionCounting.productionBalance.report.panel.includeAdditionalTime", locale) + ":",
                    productionBalance.getBooleanField(INCLUDE_ADDITIONAL_TIME) ? translationService.translate(
                            QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale),
                    null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
        }
        return leftPanel;
    }

    public PdfPTable createRightPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable rightPanel = pdfHelper.createPanelTable(1);

        rightPanel.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalance.report.panel.countingParameters", locale) + ":", FontUtils
                .getDejavuBold10Dark()));
        addTableCellAsTable(
                rightPanel,
                TABS_LITERAL
                        + translationService.translate(
                                "productionCounting.productionBalance.report.panel.typeOfProductionRecording", locale) + ":",
                translationService.translate("productionCounting.productionBalance.typeOfProductionRecording."
                        + productionBalance.getBelongsToField(ORDER).getField("typeOfProductionRecording"), locale), null,
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

    private void addInputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
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

        List<Entity> inputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ORDER, productionBalance.getBelongsToField(ORDER)))
                .add(SearchRestrictions.eq(STATE_LITERAL, ProductionCountingStates.ACCEPTED.getStringValue())).list()
                .getEntities();
        for (Entity productionRecord : productionRecords) {
            inputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductInComponents"));
        }

        if (!inputProductsList.isEmpty()) {
            document.add(new Paragraph(translationService.translate("productionCounting.productionBalance.report.paragraph",
                    locale), FontUtils.getDejavuBold11Dark()));

            Collections.sort(inputProductsList, new EntityProductInOutComparator());

            PdfPTable inputProductsTable = pdfHelper.createTableWithHeader(6, inputProductsTableHeader, false);

            for (Entity inputProduct : productionBalanceReportDataService.groupProductInOutComponentsByProduct(inputProductsList)) {
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(PRODUCT).getStringField("number"), FontUtils
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

    private void addOutputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
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

        List<Entity> outputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ORDER, productionBalance.getBelongsToField(ORDER)))
                .add(SearchRestrictions.eq(STATE_LITERAL, ProductionCountingStates.ACCEPTED.getStringValue())).list()
                .getEntities();
        for (Entity productionRecord : productionRecords) {
            if (productionRecord.getStringField(STATE_LITERAL).equals(ProductionCountingStates.ACCEPTED.getStringValue())) {
                outputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductOutComponents"));
            }
        }

        if (!outputProductsList.isEmpty()) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(translationService.translate("productionCounting.productionBalance.report.paragraph2",
                    locale), FontUtils.getDejavuBold11Dark()));

            Collections.sort(outputProductsList, new EntityProductInOutComparator());

            PdfPTable outputProductsTable = pdfHelper.createTableWithHeader(6, outputProductsTableHeader, false);

            for (Entity outputProduct : productionBalanceReportDataService
                    .groupProductInOutComponentsByProduct(outputProductsList)) {
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(PRODUCT).getStringField("number"),
                        FontUtils.getDejavuRegular9Dark()));
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

    private void addMachineTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
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

        Integer plannedTimeSum = 0;
        Integer registeredTimeSum = 0;
        Integer timeBalanceSum = 0;

        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ORDER, productionBalance.getBelongsToField(ORDER)))
                .add(SearchRestrictions.eq(STATE_LITERAL, ProductionCountingStates.ACCEPTED.getStringValue())).list()
                .getEntities();
        Collections.sort(productionRecords, new EntityProductionRecordOperationComparator());

        for (Entity productionRecord : productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecords)) {
            machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getStringField("nodeNumber"), FontUtils.getDejavuRegular9Dark()));
            machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getBelongsToField("operation").getStringField("number"), FontUtils.getDejavuRegular9Dark()));
            machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("plannedMachineTime")), FontUtils.getDejavuRegular9Dark()));
            machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("machineTime")), FontUtils.getDejavuRegular9Dark()));
            machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("machineTimeBalance")), FontUtils.getDejavuRegular9Dark()));
            plannedTimeSum += (Integer) productionRecord.getField("plannedMachineTime");
            registeredTimeSum += (Integer) productionRecord.getField("machineTime");
            timeBalanceSum += (Integer) productionRecord.getField("machineTimeBalance");
        }

        machineTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(plannedTimeSum), FontUtils
                .getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(registeredTimeSum), FontUtils
                .getDejavuRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(timeBalanceSum), FontUtils
                .getDejavuRegular9Dark()));

        document.add(machineTimeTable);
    }

    private void addLaborTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
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

        Integer plannedTimeSum = 0;
        Integer registeredTimeSum = 0;
        Integer timeBalanceSum = 0;
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ORDER, productionBalance.getBelongsToField(ORDER)))
                .add(SearchRestrictions.eq(STATE_LITERAL, ProductionCountingStates.ACCEPTED.getStringValue())).list()
                .getEntities();
        Collections.sort(productionRecords, new EntityProductionRecordOperationComparator());
        for (Entity productionRecord : productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecords)) {
            laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getStringField("nodeNumber"), FontUtils.getDejavuRegular9Dark()));
            laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getBelongsToField("operation").getStringField("number"), FontUtils.getDejavuRegular9Dark()));
            laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("plannedLaborTime")), FontUtils.getDejavuRegular9Dark()));
            laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("laborTime")), FontUtils.getDejavuRegular9Dark()));
            laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("laborTimeBalance")), FontUtils.getDejavuRegular9Dark()));
            plannedTimeSum += (Integer) productionRecord.getField("plannedLaborTime");
            registeredTimeSum += (Integer) productionRecord.getField("laborTime");
            timeBalanceSum += (Integer) productionRecord.getField("laborTimeBalance");
        }

        laborTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(plannedTimeSum), FontUtils
                .getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(registeredTimeSum), FontUtils
                .getDejavuRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(timeBalanceSum), FontUtils
                .getDejavuRegular9Dark()));

        document.add(laborTimeTable);
    }

    private void addTimeBalanceAsPanel(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate(
                "productionCounting.productionBalanceDetails.window.productionTime", locale), FontUtils.getDejavuBold11Dark()));

        BigDecimal plannedMachineTime = BigDecimal.ZERO;
        BigDecimal plannedLaborTime = BigDecimal.ZERO;
        BigDecimal machineTime = BigDecimal.ZERO;
        BigDecimal laborTime = BigDecimal.ZERO;
        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo("order", productionBalance.getBelongsToField("order"))).list().getEntities();

        for (Entity productionRecord : productionRecordsList) {
            plannedMachineTime = plannedMachineTime.add(
                    new BigDecimal((Integer) productionRecord.getField("plannedMachineTime")), numberService.getMathContext());
            plannedLaborTime = plannedLaborTime.add(new BigDecimal((Integer) productionRecord.getField("plannedLaborTime")),
                    numberService.getMathContext());
            machineTime = machineTime.add(new BigDecimal((Integer) productionRecord.getField("machineTime")),
                    numberService.getMathContext());
            laborTime = laborTime.add(new BigDecimal((Integer) productionRecord.getField("laborTime")),
                    numberService.getMathContext());
        }

        BigDecimal machineTimeBalance = machineTime.subtract(plannedMachineTime, numberService.getMathContext());
        BigDecimal laborTimeBalance = laborTime.subtract(plannedLaborTime, numberService.getMathContext());

        PdfPTable timePanel = pdfHelper.createPanelTable(3);

        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.timeTab.operationsTime.column.plannedMachineTime",
                        locale)
                        + ":", timeConverterService.convertTimeToString(plannedMachineTime.intValue()), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.timeTab.operationsTime.column.machineTime", locale)
                        + ":", timeConverterService.convertTimeToString(machineTime.intValue()), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.timeTab.operationsTime.column.machineTimeBalance",
                        locale)
                        + ":", timeConverterService.convertTimeToString(machineTimeBalance.intValue()), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.timeTab.operationsTime.column.plannedLaborTime",
                        locale)
                        + ":", timeConverterService.convertTimeToString(plannedLaborTime.intValue()), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.timeTab.operationsTime.column.laborTime", locale)
                        + ":", timeConverterService.convertTimeToString(laborTime.intValue()), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.timeTab.operationsTime.column.laborTimeBalance",
                        locale)
                        + ":", timeConverterService.convertTimeToString(laborTimeBalance.intValue()), null,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(), null);

        timePanel.setSpacingBefore(10);
        document.add(timePanel);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.title", locale);
    }

}
