/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.10
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceReportDataService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public final class ProductionBalancePdfService extends PdfDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductionBalancePdfService.class);

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

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = getTranslationService().translate("productionCounting.productionBalance.report.title", locale)
                + " " + productionBalance.getId().toString();
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
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

        Entity technology = productionBalance.getBelongsToField("order").getBelongsToField("technology");

        if ((Boolean) productionBalance.getBelongsToField("order").getField("registerQuantityInProduct")) {
            addInputProductsBalance(document, productionBalance, technology, locale);
        }
        if ((Boolean) productionBalance.getBelongsToField("order").getField("registerQuantityOutProduct")) {
            addOutputProductsBalance(document, productionBalance, technology, locale);
        }

        if ((Boolean) productionBalance.getBelongsToField("order").getField("registerProductionTime")) {
            if (productionBalance.getBelongsToField("order").getStringField("typeOfProductionRecording").equals("03forEach")) {
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

        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.title", locale) + ":",
                productionBalance.getId().toString(), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.order", locale),
                productionBalance.getBelongsToField("order").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.product", locale),
                productionBalance.getBelongsToField("product").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                productionBalance.getField("recordsNumber").toString(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionBalance.getStringField("description"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                null);

        return leftPanel;
    }

    private PdfPTable createRightPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable rightPanel = PdfUtil.createPanelTable(1);

        rightPanel.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityOutProduct", locale)
                                + " "
                                + ((Boolean) productionBalance.getBelongsToField("order").getField("registerQuantityInProduct") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityInProduct", locale)
                                + " "
                                + ((Boolean) productionBalance.getBelongsToField("order").getField("registerQuantityOutProduct") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerProductionTime", locale)
                                + " "
                                + ((Boolean) productionBalance.getBelongsToField("order").getField("registerProductionTime") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.justOne", locale)
                + " "
                + ((Boolean) productionBalance.getBelongsToField("order").getField("justOne") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.allowToClose", locale)
                + " "
                + ((Boolean) productionBalance.getBelongsToField("order").getField("allowToClose") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                + " "
                + ((Boolean) productionBalance.getBelongsToField("order").getField("autoCloseOrder") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));

        return rightPanel;
    }

    private void addInputProductsBalance(final Document document, final Entity productionBalance, final Entity technology,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> inputProductsTableHeader = new ArrayList<String>();
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));
        inputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));

        PdfPTable inputProductsTable = PdfUtil.createTableWithHeader(7, inputProductsTableHeader, false);

        List<Entity> inputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", productionBalance.getBelongsToField("order")))
                .add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue())).list().getEntities();
        for (Entity productionRecord : productionRecords) {
            inputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductInComponents"));
        }
        Collections.sort(inputProductsList, new EntityProductInOutComparator());

        for (Entity inputProduct : productionBalanceReportDataService.groupProductInOutComponentsByProduct(inputProductsList)) {
            inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("number"), PdfUtil
                    .getArialRegular9Dark()));
            inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("name"), PdfUtil
                    .getArialRegular9Dark()));

            String type = technologyService.getProductType(inputProduct.getBelongsToField("product"), technology);
            inputProductsTable.addCell(new Phrase(this.getTranslationService().translate(
                    "basic.product.typeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

            inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            inputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("plannedQuantity")), PdfUtil
                    .getArialRegular9Dark()));
            if (inputProduct.getField("usedQuantity") != null) {
                inputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("usedQuantity")), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("balance")), PdfUtil
                        .getArialRegular9Dark()));
            } else {
                inputProductsTable.addCell(new Phrase("N/A", PdfUtil.getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase("N/A", PdfUtil.getArialRegular9Dark()));
            }
            inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("unit"), PdfUtil
                    .getArialRegular9Dark()));
        }

        document.add(inputProductsTable);
    }

    private void addOutputProductsBalance(final Document document, final Entity productionBalance, final Entity technology,
            final Locale locale) throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph2",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> outputProductsTableHeader = new ArrayList<String>();
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.producedQuantity", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));
        outputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));

        PdfPTable outputProductsTable = PdfUtil.createTableWithHeader(7, outputProductsTableHeader, false);

        List<Entity> outputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", productionBalance.getBelongsToField("order")))
                .add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue())).list().getEntities();
        for (Entity productionRecord : productionRecords) {
            if (productionRecord.getStringField("state").equals(ProductionCountingStates.ACCEPTED.getStringValue())) {
                outputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductOutComponents"));
            }
        }
        Collections.sort(outputProductsList, new EntityProductInOutComparator());

        for (Entity outputProduct : productionBalanceReportDataService.groupProductInOutComponentsByProduct(outputProductsList)) {
            outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField("product").getStringField("number"), PdfUtil
                    .getArialRegular9Dark()));
            outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField("product").getStringField("name"), PdfUtil
                    .getArialRegular9Dark()));

            String type = technologyService.getProductType(outputProduct.getBelongsToField("product"), technology);
            outputProductsTable.addCell(new Phrase(this.getTranslationService().translate(
                    "basic.product.typeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

            outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            outputProductsTable.addCell(new Phrase(getDecimalFormat().format(outputProduct.getField("plannedQuantity")), PdfUtil
                    .getArialRegular9Dark()));
            if (outputProduct.getField("usedQuantity") != null) {
                outputProductsTable.addCell(new Phrase(getDecimalFormat().format(outputProduct.getField("usedQuantity")), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(getDecimalFormat().format(outputProduct.getField("balance")), PdfUtil
                        .getArialRegular9Dark()));
            } else {
                outputProductsTable.addCell(new Phrase("N/A", PdfUtil.getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase("N/A", PdfUtil.getArialRegular9Dark()));
            }
            outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            outputProductsTable.addCell(new Phrase(outputProduct.getBelongsToField("product").getStringField("unit"), PdfUtil
                    .getArialRegular9Dark()));
        }

        document.add(outputProductsTable);
    }

    private void addMachineTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph3",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opLevel", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opName", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable machineTimeTable = PdfUtil.createTableWithHeader(5, operationsTimeTableHeader, false);

        Integer plannedTimeSum = 0;
        Integer registeredTimeSum = 0;
        Integer timeBalanceSum = 0;

        List<Entity> productionRecords = new ArrayList<Entity>(productionBalance.getBelongsToField("order").getHasManyField(
                "productionRecords"));
        Collections.sort(productionRecords, new EntityProductionRecordOperationComparator());

        for (Entity productionRecord : productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecords)) {
            machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent").getStringField(
                    "nodeNumber"), PdfUtil.getArialRegular9Dark()));
            machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent")
                    .getBelongsToField("operation").getStringField("name"), PdfUtil.getArialRegular9Dark()));
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

        machineTimeTable.addCell(new Phrase(getTranslationService().translate(
                "productionCounting.productionBalance.report.total", locale), PdfUtil.getArialRegular9Dark()));
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
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph4",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opLevel", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opName", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable laborTimeTable = PdfUtil.createTableWithHeader(5, operationsTimeTableHeader, false);

        Integer plannedTimeSum = 0;
        Integer registeredTimeSum = 0;
        Integer timeBalanceSum = 0;
        // List<Entity> productionRecords = new ArrayList<Entity>(productionBalance.getBelongsToField("order").getHasManyField(
        // "productionRecords"));
        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", productionBalance.getBelongsToField("order")))
                .add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue())).list().getEntities();
        Collections.sort(productionRecords, new EntityProductionRecordOperationComparator());
        for (Entity productionRecord : productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecords)) {
            laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent").getStringField(
                    "nodeNumber"), PdfUtil.getArialRegular9Dark()));
            laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent")
                    .getBelongsToField("operation").getStringField("name"), PdfUtil.getArialRegular9Dark()));
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

        laborTimeTable.addCell(new Phrase(getTranslationService().translate("productionCounting.productionBalance.report.total",
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
        document.add(new Paragraph(getTranslationService().translate(
                "productionCounting.productionBalanceDetails.window.productionTime.tabLabel", locale), PdfUtil
                .getArialBold11Dark()));

        BigDecimal plannedTime;
        BigDecimal machinePlannedTime = BigDecimal.ZERO;
        BigDecimal laborPlannedTime = BigDecimal.ZERO;

        List<Entity> orderOperationComponents = productionBalance.getBelongsToField("order").getTreeField(
                "orderOperationComponents");
        for (Entity orderOperationComponent : orderOperationComponents) {
            plannedTime = ((BigDecimal) orderOperationComponent.getField("productionInOneCycle")).multiply(
                    new BigDecimal((Integer) orderOperationComponent.getField("tj"))).add(
                    new BigDecimal((Integer) orderOperationComponent.getField("tpz")));
            machinePlannedTime = machinePlannedTime.add(plannedTime.multiply((BigDecimal) orderOperationComponent
                    .getField("machineUtilization")));
            laborPlannedTime = laborPlannedTime.add(plannedTime.multiply((BigDecimal) orderOperationComponent
                    .getField("laborUtilization")));
        }

        BigDecimal machineRegisteredTime = BigDecimal.ZERO;
        BigDecimal laborRegisteredTime = BigDecimal.ZERO;
        for (Entity productionRecord : productionBalance.getBelongsToField("order").getHasManyField("productionRecords")) {
            machineRegisteredTime = machineRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("machineTime")));
            laborRegisteredTime = laborRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("laborTime")));
        }

        BigDecimal machineTimeBalance = machineRegisteredTime.subtract(machinePlannedTime);
        BigDecimal laborTimeBalance = laborRegisteredTime.subtract(laborPlannedTime);

        PdfPTable timePanel = PdfUtil.createPanelTable(3);

        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machinePlannedTime.label", locale)
                        + ":", timeConverterService.convertTimeToString(machinePlannedTime.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machineRegisteredTime.label", locale)
                        + ":", timeConverterService.convertTimeToString(machineRegisteredTime.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machineTimeBalance.label", locale)
                        + ":", timeConverterService.convertTimeToString(machineTimeBalance.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborPlannedTime.label", locale)
                        + ":", timeConverterService.convertTimeToString(laborPlannedTime.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborRegisteredTime.label", locale)
                        + ":", timeConverterService.convertTimeToString(laborRegisteredTime.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborTimeBalance.label", locale)
                        + ":", timeConverterService.convertTimeToString(laborTimeBalance.intValue()), null,
                PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);

        timePanel.setSpacingBefore(10);
        document.add(timePanel);
    }

    @Override
    protected String getSuffix() {
        return "";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("productionCounting.productionBalance.report.title", locale);
    }

}
