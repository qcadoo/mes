/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

    private static final String FIELD_USED_QUANTITY = "usedQuantity";

    private static final String NOT_AVAILABLE = "N/A";

    private static final String QCADOO_VIEW_FALSE = "qcadooView.false";

    private static final String QCADOO_VIEW_TRUE = "qcadooView.true";

    private static final String FIELD_PRODUCT = "product";

    private static final String FIELD_NAME = "name";

    private static final String FIELD_ORDER = "order";

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
        final String documentTitle = getTranslationService().translate("productionCounting.productionCounting.report.title",
                locale)
                + " " + productionCounting.getId().toString();
        final String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionCounting.getField("date"),
                securityService.getCurrentUserName());

        final PdfPTable leftPanel = createLeftPanel(productionCounting, locale);
        final PdfPTable rightPanel = createRightPanel(productionCounting, locale);

        final PdfPTable panelTable = PdfUtil.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
        final List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(FIELD_ORDER, productionCounting.getBelongsToField(FIELD_ORDER))).list()
                .getEntities();
        Collections.sort(productionRecordsList, new EntityProductionRecordComparator());
        for (Entity productionRecord : productionRecordsList) {
            addProductionRecord(document, productionRecord, locale);
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

    private PdfPTable createLeftPanel(final Entity productionCounting, final Locale locale) {
        final PdfPTable leftPanel = PdfUtil.createPanelTable(1);

        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionCounting.report.title", locale) + ":",
                productionCounting.getId().toString(), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.order", locale),
                productionCounting.getBelongsToField(FIELD_ORDER).getStringField(FIELD_NAME), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.product", locale),
                productionCounting.getBelongsToField(FIELD_PRODUCT).getStringField(FIELD_NAME), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                String.valueOf(productionCounting.getBelongsToField(FIELD_ORDER).getHasManyField("productionRecords").size()),
                null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionCounting.getStringField("description"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                null);

        return leftPanel;
    }

    private PdfPTable createRightPanel(final Entity productionCounting, final Locale locale) {
        final PdfPTable rightPanel = PdfUtil.createPanelTable(1);

        rightPanel.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityOutProduct", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField(FIELD_ORDER).getField(
                                        "registerQuantityInProduct") ? getTranslationService()
                                        .translate(QCADOO_VIEW_TRUE, locale) : getTranslationService().translate(
                                        QCADOO_VIEW_FALSE, locale)), PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityInProduct", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField(FIELD_ORDER).getField(
                                        "registerQuantityOutProduct") ? getTranslationService().translate(QCADOO_VIEW_TRUE,
                                        locale) : getTranslationService().translate(QCADOO_VIEW_FALSE, locale)), PdfUtil
                                .getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerProductionTime", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField(FIELD_ORDER).getField("registerProductionTime") ? getTranslationService()
                                        .translate(QCADOO_VIEW_TRUE, locale) : getTranslationService().translate(
                                        QCADOO_VIEW_FALSE, locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.justOne", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField(FIELD_ORDER).getField("justOne") ? getTranslationService()
                        .translate(QCADOO_VIEW_TRUE, locale) : getTranslationService().translate(QCADOO_VIEW_FALSE, locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.allowToClose", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField(FIELD_ORDER).getField("allowToClose") ? getTranslationService()
                        .translate(QCADOO_VIEW_TRUE, locale) : getTranslationService().translate(QCADOO_VIEW_FALSE, locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField(FIELD_ORDER).getField("autoCloseOrder") ? getTranslationService()
                                        .translate(QCADOO_VIEW_TRUE, locale) : getTranslationService().translate(
                                        QCADOO_VIEW_FALSE, locale)), PdfUtil.getArialBold9Dark()));

        return rightPanel;
    }

    private void addProductionRecord(final Document document, final Entity productionRecord, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionCounting.report.paragraph",
                locale)
                + " " + productionRecord.getStringField("number"), PdfUtil.getArialBold19Dark()));

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        addTableCellAsTable(
                panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.recordType", locale),
                (Boolean) productionRecord.getField("lastRecord") ? getTranslationService().translate(
                        "productionCounting.productionCounting.report.panel.recordType.final", locale) : getTranslationService()
                        .translate("productionCounting.productionCounting.report.panel.recordType.partial", locale), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        if (productionRecord.getBelongsToField(FIELD_ORDER).getStringField("typeOfProductionRecording").equals("02cumulated")) {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.operationAndLevel",
                            locale), NOT_AVAILABLE, null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        } else {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.operationAndLevel",
                            locale), productionRecord.getBelongsToField("orderOperationComponent").getStringField("nodeNumber")
                            + " "
                            + productionRecord.getBelongsToField("orderOperationComponent").getBelongsToField("operation")
                                    .getStringField(FIELD_NAME), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                    null);
            addTableCellAsTable(panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.dateAndTime", locale),
                    (new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format((Date) productionRecord.getField("creationTime"))),
                    null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
            if ((Boolean) productionRecord.getBelongsToField(FIELD_ORDER).getField("registerProductionTime")) {
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
                                "productionCounting.productionCounting.report.panel.machineOperationTime", locale),
                        NOT_AVAILABLE, null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
            }
        }
        addTableCellAsTable(panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.worker", locale),
                productionRecord.getStringField("worker"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        if ((Boolean) productionRecord.getBelongsToField(FIELD_ORDER).getField("registerProductionTime")) {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.laborOperationTime",
                            locale), timeConverterService.convertTimeToString((Integer) productionRecord.getField("laborTime")),
                    null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        } else {
            addTableCellAsTable(
                    panelTable,
                    getTranslationService().translate("productionCounting.productionCounting.report.panel.laborOperationTime",
                            locale), NOT_AVAILABLE, null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        }
        panelTable.setSpacingBefore(10);
        document.add(panelTable);

        Entity technology = productionRecord.getBelongsToField(FIELD_ORDER).getBelongsToField("technology");

        if ((Boolean) productionRecord.getBelongsToField(FIELD_ORDER).getField("registerQuantityInProduct")) {
            addInputProducts(document, productionRecord, technology, locale);
        }

        if ((Boolean) productionRecord.getBelongsToField(FIELD_ORDER).getField("registerQuantityOutProduct")) {
            addOutputProducts(document, productionRecord, technology, locale);
        }
    }

    private void addInputProducts(final Document document, final Entity productionRecord, final Entity technology,
            final Locale locale) throws DocumentException {
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
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField(FIELD_PRODUCT).getStringField("number"),
                        PdfUtil.getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField(FIELD_PRODUCT).getStringField(FIELD_NAME),
                        PdfUtil.getArialRegular9Dark()));

                String type = technologyService.getProductType(productIn.getBelongsToField(FIELD_PRODUCT), technology);
                inputProductsTable.addCell(new Phrase(getTranslationService().translate(
                        "basic.product.globalTypeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                if (productIn.getField(FIELD_USED_QUANTITY) == null) {
                    inputProductsTable.addCell(new Phrase(NOT_AVAILABLE, PdfUtil.getArialRegular9Dark()));
                } else {
                    inputProductsTable.addCell(new Phrase(getDecimalFormat().format(productIn.getField(FIELD_USED_QUANTITY)),
                            PdfUtil.getArialRegular9Dark()));
                }
                inputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField(FIELD_PRODUCT).getStringField("unit"), PdfUtil
                        .getArialRegular9Dark()));
            }
        }

        document.add(inputProductsTable);
    }

    private void addOutputProducts(final Document document, final Entity productionRecord, final Entity technology,
            final Locale locale) throws DocumentException {
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
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField(FIELD_PRODUCT).getStringField("number"),
                        PdfUtil.getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField(FIELD_PRODUCT).getStringField(FIELD_NAME),
                        PdfUtil.getArialRegular9Dark()));

                String type = technologyService.getProductType(productOut.getBelongsToField(FIELD_PRODUCT), technology);
                outputProductsTable.addCell(new Phrase(getTranslationService().translate(
                        "basic.product.globalTypeOfMaterial.value." + type, locale), PdfUtil.getArialRegular9Dark()));

                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                if (productOut.getField(FIELD_USED_QUANTITY) == null) {
                    outputProductsTable.addCell(new Phrase(NOT_AVAILABLE, PdfUtil.getArialRegular9Dark()));
                } else {
                    outputProductsTable.addCell(new Phrase(getDecimalFormat().format(productOut.getField(FIELD_USED_QUANTITY)),
                            PdfUtil.getArialRegular9Dark()));
                }
                outputProductsTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField(FIELD_PRODUCT).getStringField("unit"),
                        PdfUtil.getArialRegular9Dark()));
            }
        }

        document.add(outputProductsTable);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("productionCounting.productionBalance.report.title", locale);
    }

}
