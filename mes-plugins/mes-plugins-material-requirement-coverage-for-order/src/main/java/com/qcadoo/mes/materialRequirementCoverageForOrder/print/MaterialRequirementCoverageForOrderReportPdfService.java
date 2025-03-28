/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.materialRequirementCoverageForOrder.print;

import com.google.common.collect.Lists;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.MaterialRequirementCoverageForOrderService;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.ColumnForCoveragesForOrderFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageForOrderFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageLocationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MaterialRequirementCoverageForOrderReportPdfService extends PdfDocumentService {

    private static final Integer REPORT_WIDTH = 762;

    private static final String L_NUMBER = "number";

    private static final String L_NAME = "name";

    @Autowired
    private MaterialRequirementCoverageForOrderService materialRequirementCoverageForOrderService;

    @Autowired
    private MaterialRequirementCoverageForOrderColumnFetcher materialRequirementCoverageColumnFetcher;

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ParameterService parameterService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

    @Override
    protected void buildPdfContent(final Document document, final Entity materialRequirementCoverage, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("orderSupplies.materialRequirementCoverage.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        createHeaderTable(document, materialRequirementCoverage, locale);
        createOptionsTable(document, materialRequirementCoverage, locale);
        createProductsTable(document, materialRequirementCoverage, locale);
    }

    public void createHeaderTable(final Document document, final Entity materialRequirementCoverage, final Locale locale)
            throws DocumentException {
        PdfPTable headerTable = pdfHelper.createPanelTable(5);

        headerTable.setSpacingBefore(7);

        pdfHelper.addTableCellAsOneColumnTable(headerTable, translationService.translate(
                "orderSupplies.materialRequirementCoverage.report.columnHeader.coverageToDate", locale),
                getStringFromDate(materialRequirementCoverage.getDateField(CoverageForOrderFields.COVERAGE_TO_DATE)));
        pdfHelper.addTableCellAsOneColumnTable(headerTable, translationService.translate(
                "orderSupplies.materialRequirementCoverage.report.columnHeader.coverageLocations", locale),
                getCoverageLocationNames(materialRequirementCoverage.getHasManyField(CoverageForOrderFields.COVERAGE_LOCATIONS)));
        pdfHelper.addTableCellAsOneColumnTable(headerTable, translationService.translate(
                "materialRequirementCoverageForOrder.coverageForOrder.report.columnHeader.orderNumber", locale),
                materialRequirementCoverage.getBelongsToField(CoverageForOrderFields.ORDER).getStringField(OrderFields.NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(headerTable, translationService.translate(
                "orderSupplies.materialRequirementCoverage.report.columnHeader.generatedDate", locale),
                getStringFromDate(materialRequirementCoverage.getDateField(CoverageForOrderFields.GENERATED_DATE)));
        pdfHelper
                .addTableCellAsOneColumnTable(headerTable, translationService.translate(
                        "orderSupplies.materialRequirementCoverage.report.columnHeader.generatedBy", locale),
                        materialRequirementCoverage.getStringField(CoverageForOrderFields.GENERATED_BY));

        document.add(headerTable);
    }

    public void createOptionsTable(final Document document, final Entity materialRequirementCoverage, final Locale locale)
            throws DocumentException {
        PdfPTable optionsTable = new PdfPTable(2);

        optionsTable.setSpacingBefore(7);
        optionsTable.setWidthPercentage(100f);

        optionsTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        optionsTable.getDefaultCell().setPadding(2f);

        PdfPTable leftPanel = createOptionsLeftTable(materialRequirementCoverage, locale);

        PdfPTable rightPanel = createOptionsRightTable(materialRequirementCoverage, locale);

        optionsTable.addCell(leftPanel);
        optionsTable.addCell(rightPanel);

        document.add(optionsTable);

        document.add(Chunk.NEWLINE);
    }

    private PdfPTable createOptionsLeftTable(final Entity materialRequirementCoverage, final Locale locale) {
        PdfPTable leftPanel = pdfHelper.createPanelTable(1);

        leftPanel.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);

        leftPanel.addCell(new Paragraph(translationService.translate(
                "orderSupplies.materialRequirementCoverage.report.includeInCoverage.title", locale), FontUtils
                .getDejavuBold10Dark()));

        PdfPTable leftPanelOptions = new PdfPTable(1);

        leftPanelOptions.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        leftPanelOptions.getDefaultCell().setPadding(0);
        leftPanelOptions.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);

        pdfHelper.addTableCellAsOneColumnTable(
                leftPanelOptions,
                translationService.translate("materialRequirementCoverageForOrder.coverageForOrder.report.includeDraftDeliveries", locale),
                materialRequirementCoverage.getBooleanField(CoverageForOrderFields.INCLUDE_DRAFT_DELIVERIES) ? translationService
                        .translate("qcadooView.true", locale) : translationService.translate("qcadooView.false", locale));

        leftPanel.addCell(leftPanelOptions);

        return leftPanel;
    }

    private PdfPTable createOptionsRightTable(final Entity materialRequirementCoverage, final Locale locale) {
        PdfPTable rightPanel = pdfHelper.createPanelTable(1);

        rightPanel.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);

        rightPanel.addCell(new Paragraph(translationService.translate(
                "orderSupplies.materialRequirementCoverage.report.onlyProductsInCoverage.title", locale), FontUtils
                .getDejavuBold10Dark()));

        PdfPTable rightPanelOptions = new PdfPTable(1);

        rightPanelOptions.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        rightPanelOptions.getDefaultCell().setPadding(0);
        rightPanelOptions.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);

        pdfHelper.addTableCellAsOneColumnTable(
                rightPanelOptions,
                translationService.translate("orderSupplies.materialRequirementCoverage.report.coverageType", locale),
                translationService.translate("orderSupplies.materialRequirementCoverage.coverageType.value."
                        + materialRequirementCoverage.getStringField(CoverageForOrderFields.COVERAGE_TYPE), locale));

        rightPanel.addCell(rightPanelOptions);

        return rightPanel;
    }

    public void createProductsTable(final Document document, final Entity materialRequirementCoverage, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForCoverages = materialRequirementCoverageForOrderService.getColumnsForCoveragesForOrder();

        if (!columnsForCoverages.isEmpty()) {
            List<Entity> coverageProducts = materialRequirementCoverage.getHasManyField(CoverageForOrderFields.COVERAGE_PRODUCTS);

            Map<Entity, Map<String, String>> coverageProductsColumnValues = materialRequirementCoverageColumnFetcher
                    .getCoverageProductsColumnValues(coverageProducts);

            List<Entity> filteredColumnsForCoverages = columnExtensionService.filterEmptyColumns(columnsForCoverages,
                    coverageProducts, coverageProductsColumnValues);

            if (!filteredColumnsForCoverages.isEmpty()) {
                List<String> columnsName = Lists.newArrayList();

                for (Entity entity : filteredColumnsForCoverages) {
                    columnsName.add(entity.getStringField(ColumnForCoveragesForOrderFields.IDENTIFIER));
                }

                PdfPTable productsTable = pdfHelper.createTableWithHeader(filteredColumnsForCoverages.size(),
                        prepareProductsTableHeader(document, filteredColumnsForCoverages, locale), false,
                        pdfHelper.getReportColumnWidths(REPORT_WIDTH, parameterService.getReportColumnWidths(), columnsName),
                        HeaderAlignment.CENTER);

                for (Entity coverageProduct : coverageProducts) {
                    for (Entity columnForCoverages : filteredColumnsForCoverages) {
                        String identifier = columnForCoverages.getStringField(ColumnForCoveragesForOrderFields.IDENTIFIER);
                        String alignment = columnForCoverages.getStringField(ColumnForCoveragesForOrderFields.ALIGNMENT);

                        String value = coverageProductsColumnValues.get(coverageProduct).get(identifier);

                        prepareProductColumnAlignment(productsTable.getDefaultCell(), ColumnAlignment.parseString(alignment));

                        productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
                    }
                }

                document.add(productsTable);
                document.add(Chunk.NEWLINE);
            }
        }
    }

    private String getCoverageLocationNames(final List<Entity> coverageLocations) {
        StringBuilder coverageLocationNames = new StringBuilder();

        for (Entity coverageLocation : coverageLocations) {
            Entity location = coverageLocation.getBelongsToField(CoverageLocationFields.LOCATION);

            String name = location.getStringField(LocationFields.NAME);

            coverageLocationNames.append(name);

            if (coverageLocations.indexOf(coverageLocation) != (coverageLocations.size() - 1)) {
                coverageLocationNames.append(", ");
            }
        }

        return coverageLocationNames.toString();
    }

    private List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForCoverages,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService.translate(
                "orderSupplies.materialRequirementCoverage.report.coverageProducts.title", locale), FontUtils
                .getDejavuBold11Dark()));

        List<String> productsHeader = Lists.newArrayList();

        for (Entity columnForCoverages : columnsForCoverages) {
            String name = columnForCoverages.getStringField(ColumnForCoveragesForOrderFields.NAME);

            productsHeader.add(translationService.translate(name, locale));
        }

        return productsHeader;
    }

    private PdfPTable createIncludedTable(final List<Entity> includedEntities, final String includedType, final Locale locale)
            throws DocumentException {
        PdfPTable includedTable = new PdfPTable(1);

        includedTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        includedTable.getDefaultCell().setPadding(2f);

        includedTable.addCell(new Paragraph(translationService.translate("orderSupplies.materialRequirementCoverage.report."
                + includedType + ".title", locale), FontUtils.getDejavuBold10Dark()));

        int[] columnWidths = { 40, 60 };

        PdfPTable includedTableBody = pdfHelper.createTableWithHeader(2, prepareIncludedTableHeader(includedType, locale), false,
                columnWidths);

        includedTableBody.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);

        for (Entity included : includedEntities) {
            String number = included.getStringField(L_NUMBER);
            String name = included.getStringField(L_NAME);

            includedTableBody.addCell(new Phrase(number, FontUtils.getDejavuRegular7Dark()));
            includedTableBody.addCell(new Phrase(name, FontUtils.getDejavuRegular7Dark()));
        }

        includedTable.addCell(includedTableBody);

        return includedTable;
    }

    private List<String> prepareIncludedTableHeader(final String includedType, final Locale locale) throws DocumentException {
        List<String> productsHeader = Lists.newArrayList(translationService.translate(
                "orderSupplies.materialRequirementCoverage.report." + includedType + ".column.number", locale),
                translationService.translate("orderSupplies.materialRequirementCoverage.report." + includedType + ".column.name",
                        locale));

        return productsHeader;
    }

    private void prepareProductColumnAlignment(final PdfPCell cell, final ColumnAlignment columnAlignment) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    private String getStringFromDate(final Date date) {
        return simpleDateFormat.format(date);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("orderSupplies.materialRequirementCoverage.report.title", locale);
    }

}
