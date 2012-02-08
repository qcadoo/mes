/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
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

    private static final String PRODUCT_LITERAL = "product";

    private static final String NAME_LITERAL = "name";

    private static final String ORDER_LITERAL = "order";

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    SecurityService securityService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

    @Autowired
    private TimeConverterService timeConverterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("productionCounting.productionBalance.report.title", locale) + " "
                + productionBalance.getId().toString();
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionBalance.getField("date"),
                securityService.getCurrentUserName());

        PdfPTable leftPanel = createLeftPanel(productionBalance, locale);
        PdfPTable rightPanel = createRightPanel(productionBalance, locale);

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        Entity technology = productionBalance.getBelongsToField(ORDER_LITERAL).getBelongsToField("technology");

        if ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField("registerQuantityInProduct")) {
            addInputProductsBalance(document, productionBalance, technology, locale);
        }
        if ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField("registerQuantityOutProduct")) {
            addOutputProductsBalance(document, productionBalance, technology, locale);
        }

        if ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField("registerProductionTime")) {
            if (productionBalance.getBelongsToField(ORDER_LITERAL).getStringField("typeOfProductionRecording")
                    .equals("03forEach")) {
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

    private PdfPTable createLeftPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable leftPanel = PdfUtil.createPanelTable(1);

        addTableCellAsTable(leftPanel, translationService.translate("productionCounting.productionBalance.report.title", locale)
                + ":", productionBalance.getId().toString(), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.order", locale),
                productionBalance.getBelongsToField(ORDER_LITERAL).getStringField(NAME_LITERAL), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.product", locale),
                productionBalance.getBelongsToField(PRODUCT_LITERAL).getStringField(NAME_LITERAL), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                productionBalance.getField("recordsNumber").toString(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                translationService.translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionBalance.getStringField("description"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                null);

        return leftPanel;
    }

    private PdfPTable createRightPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable rightPanel = PdfUtil.createPanelTable(1);

        rightPanel.addCell(new Phrase(translationService.translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale) + ":", PdfUtil
                .getArialBold10Dark()));
        rightPanel
                .addCell(new Phrase(
                        TABS_LITERAL
                                + translationService.translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityOutProduct", locale)
                                + " "
                                + ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField(
                                        "registerQuantityInProduct") ? translationService.translate(QCADOO_VIEW_TRUE_LITERAL,
                                        locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale)), PdfUtil
                                .getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        TABS_LITERAL
                                + translationService.translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityInProduct", locale)
                                + " "
                                + ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField(
                                        "registerQuantityOutProduct") ? translationService.translate(QCADOO_VIEW_TRUE_LITERAL,
                                        locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL, locale)), PdfUtil
                                .getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        TABS_LITERAL
                                + translationService.translate(
                                        "productionCounting.productionBalance.report.panel.registerProductionTime", locale)
                                + " "
                                + ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL)
                                        .getField("registerProductionTime") ? translationService.translate(
                                        QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(
                                        QCADOO_VIEW_FALSE_LITERAL, locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.justOne", locale)
                + " "
                + ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField("justOne") ? translationService
                        .translate(QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL,
                        locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.allowToClose", locale)
                + " "
                + ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField("allowToClose") ? translationService
                        .translate(QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL,
                        locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase(TABS_LITERAL
                + translationService.translate("productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                + " "
                + ((Boolean) productionBalance.getBelongsToField(ORDER_LITERAL).getField("autoCloseOrder") ? translationService
                        .translate(QCADOO_VIEW_TRUE_LITERAL, locale) : translationService.translate(QCADOO_VIEW_FALSE_LITERAL,
                        locale)), PdfUtil.getArialBold9Dark()));

        return rightPanel;
    }

    private void addInputProductsBalance(final Document document, final Entity productionBalance, final Entity technology,
            final Locale locale) throws DocumentException {

        List<String> inputProductsTableHeader = new ArrayList<String>();
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        inputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        inputProductsTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));
        inputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        List<Entity> inputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ORDER_LITERAL, productionBalance.getBelongsToField(ORDER_LITERAL)))
                .add(SearchRestrictions.eq(STATE_LITERAL, ProductionCountingStates.ACCEPTED.getStringValue())).list()
                .getEntities();
        for (Entity productionRecord : productionRecords) {
            inputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductInComponents"));
        }

        if (!inputProductsList.isEmpty()) {
            document.add(new Paragraph(translationService.translate("productionCounting.productionBalance.report.paragraph",
                    locale), PdfUtil.getArialBold11Dark()));

            Collections.sort(inputProductsList, new EntityProductInOutComparator());

            PdfPTable inputProductsTable = PdfUtil.createTableWithHeader(7, inputProductsTableHeader, false);

            for (Entity inputProduct : productionBalanceReportDataService.groupProductInOutComponentsByProduct(inputProductsList)) {
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(PRODUCT_LITERAL).getStringField("number"),
                        PdfUtil.getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(PRODUCT_LITERAL)
                        .getStringField(NAME_LITERAL), PdfUtil.getArialRegular9Dark()));

                String type = technologyService.getProductType(inputProduct.getBelongsToField(PRODUCT_LITERAL), technology);
                inputProductsTable.addCell(new Phrase(this.translationService.translate(
                        "basic.product.globalTypeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                inputProductsTable.addCell(new Phrase(numberService.getDecimalFormat().format(
                        inputProduct.getField("plannedQuantity")), PdfUtil.getArialRegular9Dark()));
                if (inputProduct.getField(USED_QUANTITY_LITERAL) == null) {
                    inputProductsTable.addCell(new Phrase(N_A_LITERAL, PdfUtil.getArialRegular9Dark()));
                    inputProductsTable.addCell(new Phrase(N_A_LITERAL, PdfUtil.getArialRegular9Dark()));
                } else {
                    inputProductsTable.addCell(new Phrase(numberService.getDecimalFormat().format(
                            inputProduct.getField(USED_QUANTITY_LITERAL)), PdfUtil.getArialRegular9Dark()));
                    inputProductsTable.addCell(new Phrase(numberService.getDecimalFormat().format(
                            inputProduct.getField("balance")), PdfUtil.getArialRegular9Dark()));
                }
                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField(PRODUCT_LITERAL).getStringField("unit"),
                        PdfUtil.getArialRegular9Dark()));
            }

            document.add(inputProductsTable);
        }
    }

    private void addOutputProductsBalance(final Document document, final Entity productionBalance, final Entity technology,
            final Locale locale) throws DocumentException {

        List<String> outputProductsTableHeader = new ArrayList<String>();
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        outputProductsTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.producedQuantity", locale));
        outputProductsTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));
        outputProductsTableHeader.add(translationService.translate("basic.product.unit.label", locale));

        List<Entity> outputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ORDER_LITERAL, productionBalance.getBelongsToField(ORDER_LITERAL)))
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
                    locale), PdfUtil.getArialBold11Dark()));

            Collections.sort(outputProductsList, new EntityProductInOutComparator());

            PdfPTable outputProductsTable = PdfUtil.createTableWithHeader(7, outputProductsTableHeader, false);

            for (Entity outputProduct : productionBalanceReportDataService
                    .groupProductInOutComponentsByProduct(outputProductsList)) {
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(PRODUCT_LITERAL).getStringField("number"),
                        PdfUtil.getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(PRODUCT_LITERAL).getStringField(
                        NAME_LITERAL), PdfUtil.getArialRegular9Dark()));

                String type = technologyService.getProductType(outputProduct.getBelongsToField(PRODUCT_LITERAL), technology);
                outputProductsTable.addCell(new Phrase(this.translationService.translate(
                        "basic.product.globalTypeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                outputProductsTable.addCell(new Phrase(numberService.getDecimalFormat().format(
                        outputProduct.getField("plannedQuantity")), PdfUtil.getArialRegular9Dark()));
                if (outputProduct.getField(USED_QUANTITY_LITERAL) == null) {
                    outputProductsTable.addCell(new Phrase(N_A_LITERAL, PdfUtil.getArialRegular9Dark()));
                    outputProductsTable.addCell(new Phrase(N_A_LITERAL, PdfUtil.getArialRegular9Dark()));
                } else {
                    outputProductsTable.addCell(new Phrase(numberService.getDecimalFormat().format(
                            outputProduct.getField(USED_QUANTITY_LITERAL)), PdfUtil.getArialRegular9Dark()));
                    outputProductsTable.addCell(new Phrase(numberService.getDecimalFormat().format(
                            outputProduct.getField("balance")), PdfUtil.getArialRegular9Dark()));
                }
                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField(PRODUCT_LITERAL).getStringField("unit"),
                        PdfUtil.getArialRegular9Dark()));
            }

            document.add(outputProductsTable);
        }
    }

    private void addMachineTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                translationService.translate("productionCounting.productionBalance.report.paragraph3", locale), PdfUtil
                        .getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.opLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.opName", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));

        PdfPTable machineTimeTable = PdfUtil.createTableWithHeader(5, operationsTimeTableHeader, false);

        Integer plannedTimeSum = 0;
        Integer registeredTimeSum = 0;
        Integer timeBalanceSum = 0;

        List<Entity> productionRecords = new ArrayList<Entity>(productionBalance.getBelongsToField(ORDER_LITERAL)
                .getHasManyField("productionRecords"));
        Collections.sort(productionRecords, new EntityProductionRecordOperationComparator());

        for (Entity productionRecord : productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecords)) {
            machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getStringField("nodeNumber"), PdfUtil.getArialRegular9Dark()));
            machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getBelongsToField("operation").getStringField(NAME_LITERAL), PdfUtil.getArialRegular9Dark()));
            machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("plannedMachineTime")), PdfUtil.getArialRegular9Dark()));
            machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("machineTime")), PdfUtil.getArialRegular9Dark()));
            machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("machineTimeBalance")), PdfUtil.getArialRegular9Dark()));
            plannedTimeSum += (Integer) productionRecord.getField("plannedMachineTime");
            registeredTimeSum += (Integer) productionRecord.getField("machineTime");
            timeBalanceSum += (Integer) productionRecord.getField("machineTimeBalance");
        }

        machineTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), PdfUtil.getArialRegular9Dark()));
        machineTimeTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(plannedTimeSum), PdfUtil
                .getArialRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(registeredTimeSum), PdfUtil
                .getArialRegular9Dark()));
        machineTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(timeBalanceSum), PdfUtil
                .getArialRegular9Dark()));

        document.add(machineTimeTable);
    }

    private void addLaborTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                translationService.translate("productionCounting.productionBalance.report.paragraph4", locale), PdfUtil
                        .getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.opLevel", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.opName", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(translationService.translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(translationService.translate(PCPBRCHB_LITERAL, locale));

        PdfPTable laborTimeTable = PdfUtil.createTableWithHeader(5, operationsTimeTableHeader, false);

        Integer plannedTimeSum = 0;
        Integer registeredTimeSum = 0;
        Integer timeBalanceSum = 0;
        // List<Entity> productionRecords = new ArrayList<Entity>(productionBalance.getBelongsToField("order").getHasManyField(
        // "productionRecords"));
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ORDER_LITERAL, productionBalance.getBelongsToField(ORDER_LITERAL)))
                .add(SearchRestrictions.eq(STATE_LITERAL, ProductionCountingStates.ACCEPTED.getStringValue())).list()
                .getEntities();
        Collections.sort(productionRecords, new EntityProductionRecordOperationComparator());
        for (Entity productionRecord : productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecords)) {
            laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getStringField("nodeNumber"), PdfUtil.getArialRegular9Dark()));
            laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_LITERAL)
                    .getBelongsToField("operation").getStringField(NAME_LITERAL), PdfUtil.getArialRegular9Dark()));
            laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("plannedLaborTime")), PdfUtil.getArialRegular9Dark()));
            laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("laborTime")), PdfUtil.getArialRegular9Dark()));
            laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString((Integer) productionRecord
                    .getField("laborTimeBalance")), PdfUtil.getArialRegular9Dark()));
            plannedTimeSum += (Integer) productionRecord.getField("plannedLaborTime");
            registeredTimeSum += (Integer) productionRecord.getField("laborTime");
            timeBalanceSum += (Integer) productionRecord.getField("laborTimeBalance");
        }

        laborTimeTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                locale), PdfUtil.getArialRegular9Dark()));
        laborTimeTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(plannedTimeSum), PdfUtil
                .getArialRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(registeredTimeSum), PdfUtil
                .getArialRegular9Dark()));
        laborTimeTable.addCell(new Phrase(timeConverterService.convertTimeToString(timeBalanceSum), PdfUtil
                .getArialRegular9Dark()));

        document.add(laborTimeTable);
    }

    private void addTimeBalanceAsPanel(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate(
                "productionCounting.productionBalanceDetails.window.productionTime.tabLabel", locale), PdfUtil
                .getArialBold11Dark()));

        BigDecimal plannedTime;
        BigDecimal machinePlannedTime = BigDecimal.ZERO;
        BigDecimal laborPlannedTime = BigDecimal.ZERO;

        List<Entity> orderOperationComponents = productionBalance.getBelongsToField(ORDER_LITERAL).getTreeField(
                "orderOperationComponents");
        for (Entity orderOperationComponent : orderOperationComponents) {
            plannedTime = ((BigDecimal) orderOperationComponent.getField("productionInOneCycle")).multiply(
                    new BigDecimal((Integer) orderOperationComponent.getField("tj"), numberService.getMathContext())).add(
                    new BigDecimal((Integer) orderOperationComponent.getField("tpz")), numberService.getMathContext());
            machinePlannedTime = machinePlannedTime.add(
                    plannedTime.multiply((BigDecimal) orderOperationComponent.getField("machineUtilization"),
                            numberService.getMathContext()), numberService.getMathContext());
            laborPlannedTime = laborPlannedTime.add(
                    plannedTime.multiply((BigDecimal) orderOperationComponent.getField("laborUtilization"),
                            numberService.getMathContext()), numberService.getMathContext());
        }

        BigDecimal machineRegisteredTime = BigDecimal.ZERO;
        BigDecimal laborRegisteredTime = BigDecimal.ZERO;
        for (Entity productionRecord : productionBalance.getBelongsToField(ORDER_LITERAL).getHasManyField("productionRecords")) {
            machineRegisteredTime = machineRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("machineTime")),
                    numberService.getMathContext());
            laborRegisteredTime = laborRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("laborTime")),
                    numberService.getMathContext());
        }

        BigDecimal machineTimeBalance = machineRegisteredTime.subtract(machinePlannedTime, numberService.getMathContext());
        BigDecimal laborTimeBalance = laborRegisteredTime.subtract(laborPlannedTime, numberService.getMathContext());

        PdfPTable timePanel = PdfUtil.createPanelTable(3);

        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machinePlannedTime.label", locale)
                        + ":", timeConverterService.convertTimeToString(machinePlannedTime.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machineRegisteredTime.label", locale)
                        + ":", timeConverterService.convertTimeToString(machineRegisteredTime.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machineTimeBalance.label", locale)
                        + ":", timeConverterService.convertTimeToString(machineTimeBalance.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborPlannedTime.label", locale) + ":",
                timeConverterService.convertTimeToString(laborPlannedTime.intValue()), null, PdfUtil.getArialRegular9Dark(),
                PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborRegisteredTime.label", locale)
                        + ":", timeConverterService.convertTimeToString(laborRegisteredTime.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                translationService.translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborTimeBalance.label", locale) + ":",
                timeConverterService.convertTimeToString(laborTimeBalance.intValue()), null, PdfUtil.getArialRegular9Dark(),
                PdfUtil.getArialRegular9Dark(), null);

        timePanel.setSpacingBefore(10);
        document.add(timePanel);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.title", locale);
    }

}
