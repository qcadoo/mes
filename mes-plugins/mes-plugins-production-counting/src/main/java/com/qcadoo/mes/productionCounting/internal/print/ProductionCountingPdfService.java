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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordComparator;
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
public class ProductionCountingPdfService extends PdfDocumentService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    SecurityService securityService;

    @Autowired
    TimeConverterService timeConverterService;

    @Autowired
    private TechnologyService technologyService;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionCounting, final Locale locale)
            throws DocumentException {
        String documentTitle = getTranslationService().translate("productionCounting.productionCounting.report.title", locale)
                + " " + productionCounting.getId().toString();
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionCounting.getField("date"),
                securityService.getCurrentUserName());

        PdfPTable leftPanel = createLeftPanel(productionCounting, locale);
        PdfPTable rightPanel = createRightPanel(productionCounting, locale);

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo("order", productionCounting.getBelongsToField("order"))).list().getEntities();
        Collections.sort(productionRecordsList, new EntityProductionRecordComparator());
        for (Entity productionRecord : productionRecordsList) {
            addProductionRecord(document, productionRecord, locale);
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

    private PdfPTable createLeftPanel(final Entity productionCounting, final Locale locale) {
        PdfPTable leftPanel = PdfUtil.createPanelTable(1);

        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionCounting.report.title", locale) + ":",
                productionCounting.getId().toString(), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.order", locale),
                productionCounting.getBelongsToField("order").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.product", locale),
                productionCounting.getBelongsToField("product").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                String.valueOf(productionCounting.getBelongsToField("order").getHasManyField("productionRecords").size()), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionCounting.getStringField("description"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                null);

        return leftPanel;
    }

    private PdfPTable createRightPanel(final Entity productionCounting, final Locale locale) {
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
                                + ((Boolean) productionCounting.getBelongsToField("order").getField("registerQuantityInProduct") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityInProduct", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField("order").getField("registerQuantityOutProduct") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerProductionTime", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField("order").getField("registerProductionTime") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.justOne", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField("order").getField("justOne") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.allowToClose", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField("order").getField("allowToClose") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField("order").getField("autoCloseOrder") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));

        return rightPanel;
    }

    private void addProductionRecord(Document document, final Entity productionRecord, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionCounting.report.paragraph",
                locale)
                + " " + productionRecord.getStringField("number"), PdfUtil.getArialBold19Dark()));

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        addTableCellAsTable(
                panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.recordType", locale),
                !(Boolean) productionRecord.getField("lastRecord") ? getTranslationService().translate(
                        "productionCounting.productionCounting.report.panel.recordType.partial", locale)
                        : getTranslationService().translate(
                                "productionCounting.productionCounting.report.panel.recordType.final", locale), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        if (productionRecord.getBelongsToField("order").getStringField("typeOfProductionRecording").equals("02cumulated")) {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.operationAndLevel",
                            locale), "N/A", null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        } else {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.operationAndLevel",
                            locale), productionRecord.getBelongsToField("orderOperationComponent").getStringField("nodeNumber")
                            + " "
                            + productionRecord.getBelongsToField("orderOperationComponent").getBelongsToField("operation")
                                    .getStringField("name"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
            addTableCellAsTable(panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.dateAndTime", locale),
                    (new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format((Date) productionRecord.getField("creationTime")))
                            .toString(), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
            if ((Boolean) productionRecord.getBelongsToField("order").getField("registerProductionTime")) {
                addTableCellAsTable(
                        panelTable,
                        getTranslationService().translate(
                                "productionCounting.productionCounting.report.panel.machineOperationTime", locale),
                        timeConverterService.convertTimeToString((Integer) productionRecord.getField("machineTime")), null,
                        PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
            } else {
                addTableCellAsTable(
                        panelTable,
                        getTranslationService().translate(
                                "productionCounting.productionCounting.report.panel.machineOperationTime", locale), "N/A", null,
                        PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
            }
        }
        addTableCellAsTable(panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.worker", locale),
                productionRecord.getStringField("worker"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        if ((Boolean) productionRecord.getBelongsToField("order").getField("registerProductionTime")) {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.laborOperationTime",
                            locale), timeConverterService.convertTimeToString((Integer) productionRecord.getField("laborTime")),
                    null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        } else {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.laborOperationTime",
                            locale), "N/A", null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        }
        panelTable.setSpacingBefore(10);
        document.add(panelTable);

        Entity technology = productionRecord.getBelongsToField("order").getBelongsToField("technology");

        if ((Boolean) productionRecord.getBelongsToField("order").getField("registerQuantityInProduct")) {
            addInputProducts(document, productionRecord, technology, locale);
        }

        if ((Boolean) productionRecord.getBelongsToField("order").getField("registerQuantityOutProduct")) {
            addOutputProducts(document, productionRecord, technology, locale);
        }
    }

    private void addInputProducts(Document document, final Entity productionRecord, final Entity technology, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionCounting.report.paragraph2",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> inputProductsTableHeader = new ArrayList<String>();
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionCounting.report.columnHeader.quantity", locale));
        inputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));

        PdfPTable inputProductsTable = PdfUtil.createTableWithHeader(5, inputProductsTableHeader, false);

        if (productionRecord.getHasManyField("recordOperationProductInComponents") != null) {
            List<Entity> productsInList = new ArrayList<Entity>(
                    productionRecord.getHasManyField("recordOperationProductInComponents"));
            Collections.sort(productsInList, new EntityProductInOutComparator());
            for (Entity productIn : productsInList) {
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField("product").getStringField("number"), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField("product").getStringField("name"), PdfUtil
                        .getArialRegular9Dark()));

                String type = technologyService.getProductType(productIn.getBelongsToField("product"), technology);
                inputProductsTable.addCell(new Phrase(getTranslationService().translate(
                        "basic.product.typeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                if (productIn.getField("usedQuantity") != null) {
                    inputProductsTable.addCell(new Phrase(getDecimalFormat().format(productIn.getField("usedQuantity")), PdfUtil
                            .getArialRegular9Dark()));
                } else {
                    inputProductsTable.addCell(new Phrase("N/A", PdfUtil.getArialRegular9Dark()));
                }
                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField("product").getStringField("unit"), PdfUtil
                        .getArialRegular9Dark()));
            }
        }

        document.add(inputProductsTable);
    }

    private void addOutputProducts(Document document, final Entity productionRecord, final Entity technology, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionCounting.report.paragraph3",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> outputProductsTableHeader = new ArrayList<String>();
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionCounting.report.columnHeader.quantity", locale));
        outputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));

        PdfPTable outputProductsTable = PdfUtil.createTableWithHeader(5, outputProductsTableHeader, false);

        if (productionRecord.getHasManyField("recordOperationProductOutComponents") != null) {
            List<Entity> productsOutList = new ArrayList<Entity>(
                    productionRecord.getHasManyField("recordOperationProductOutComponents"));
            Collections.sort(productsOutList, new EntityProductInOutComparator());
            for (Entity productOut : productsOutList) {
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField("product").getStringField("number"), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField("product").getStringField("name"), PdfUtil
                        .getArialRegular9Dark()));

                String type = technologyService.getProductType(productOut.getBelongsToField("product"), technology);
                outputProductsTable.addCell(new Phrase(getTranslationService().translate(
                        "basic.product.typeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                if (productOut.getField("usedQuantity") != null) {
                    outputProductsTable.addCell(new Phrase(getDecimalFormat().format(productOut.getField("usedQuantity")),
                            PdfUtil.getArialRegular9Dark()));
                } else {
                    outputProductsTable.addCell(new Phrase("N/A", PdfUtil.getArialRegular9Dark()));
                }
                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField("product").getStringField("unit"), PdfUtil
                        .getArialRegular9Dark()));
            }
        }

        document.add(outputProductsTable);
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
