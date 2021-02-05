/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.materialFlowResources.print.helper;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public class DocumentPdfHelper {

    private static final String L_DOCUMENT_TYPE_KEY = "materialFlowResources.report.type.value.";

    private static final String L_FILENAME = "materialFlowResources.report.filename";

    private static final String L_HEADER = "materialFlowResources.report.header";

    private static final String L_NAME = "materialFlowResources.document.name.label";

    private static final String L_NUMBER = "materialFlowResources.document.number.label";

    private static final String L_TIME = "materialFlowResources.document.time.label";

    private static final String L_LOCATION_FROM = "materialFlowResources.document.locationFrom.label";

    private static final String L_LOCATION_TO = "materialFlowResources.document.locationTo.label";

    private static final String L_STATE = "materialFlowResources.document.state.label";

    private static final String L_DESCRIPTION = "materialFlowResources.document.description.label";

    private static final String L_COMPANY = "materialFlowResources.document.companyAndAddress.label";

    private static final String L_STATE_VALUE = "materialFlowResources.document.state.value.";

    private static final String L_POSITION_HEADER__INDEX = "materialFlowResources.report.positionsHeader.index";

    private static final String L_POSITION_HEADER__NUMBER = "materialFlowResources.report.positionsHeader.number";

    private static final String L_POSITION_HEADER__QUANTITY = "materialFlowResources.report.positionsHeader.quantity";

    private static final String L_POSITION_HEADER__UNIT = "materialFlowResources.report.positionsHeader.unit";

    private static final String L_POSITION_HEADER__QUANTITY_ADD = "materialFlowResources.report.positionsHeader.quantityAdd";

    private static final String L_POSITION_HEADER__UNIT_ADD = "materialFlowResources.report.positionsHeader.unitAdd";

    private static final String L_POSITION_HEADER__TOTAL_REST = "materialFlowResources.report.positionsHeader.totalRest";

    private static final String L_POSITION_HEADER__PRICE = "materialFlowResources.report.positionsHeader.price";

    private static final String L_POSITION_HEADER__BATCH = "materialFlowResources.report.positionsHeader.batch";

    private static final String L_POSITION_HEADER__PRODUCTION_DATE = "materialFlowResources.report.positionsHeader.productionDate";

    private static final String L_POSITION_HEADER__VALUE = "materialFlowResources.report.positionsHeader.value";

    private static final String L_TABLE_HEADER = "materialFlowResources.report.tableHeader";

    private static final String L_TOTAL_VALUE = "materialFlowResources.report.tableHeader.totalValue";

    private static final String L_TOTAL = "materialFlowResources.report.tableHeader.total";

    private static final String L_DELIVERY = "materialFlowResources.document.delivery.label";

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    public Entity getDocumentEntity(final Long id) {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT).get(id);
    }

    /**
     * Returns translated short document type
     *
     * @param documentEntity
     * @param locale
     * @return translated short document type
     */
    public String getDocumentType(final Entity documentEntity, final Locale locale) {
        return translationService.translate(L_DOCUMENT_TYPE_KEY + DocumentType.of(documentEntity).getStringValue(), locale);
    }

    /**
     * Returns translated products table header
     *
     * @param locale
     * @return translated short document type
     */
    public String getTableHeader(final Locale locale) {
        return translationService.translate(L_TABLE_HEADER, locale);
    }

    /**
     * Returns translated document report file name (with document type and number)
     *
     * @param documentEntity
     * @param locale
     * @return translated document report file name
     */
    public String getFileName(final Entity documentEntity, final Locale locale) {
        return translationService.translate(L_FILENAME, locale, getDocumentType(documentEntity, locale),
                documentEntity.getStringField(DocumentFields.NUMBER));
    }

    /**
     * Returns translated document report header (with document type)
     *
     * @param documentEntity
     * @param locale
     * @return translated document report header
     */
    public String getDocumentHeader(final Entity documentEntity, final Locale locale) {
        return translationService.translate(L_HEADER, locale, getDocumentType(documentEntity, locale));
    }

    /**
     * Returns total value label
     *
     * @param locale
     * @return
     */
    public String getTotalValueLabel(final Locale locale) {
        return translationService.translate(L_TOTAL_VALUE, locale);
    }

    /**
     * Returns total row label
     *
     * @param locale
     * @return
     */
    public String getTotaLabel(final Locale locale) {
        return translationService.translate(L_TOTAL, locale);
    }

    /**
     * Returns content of document header table in proper order as pair (label, value)
     *
     * @param documentEntity
     * @param locale
     * @return document header table
     */
    public List<HeaderPair> getDocumentHeaderTableContent(final Entity documentEntity, final Locale locale) {
        List<HeaderPair> headerValues = Lists.newLinkedList();
        headerValues.add(new HeaderPair(translationService.translate(L_NUMBER, locale) + "\n"
                + translationService.translate(L_NAME, locale), DocumentDataProvider.number(documentEntity) + "\n"
                + DocumentDataProvider.name(documentEntity), false));
        headerValues.add(new HeaderPair(translationService.translate(L_LOCATION_FROM, locale), DocumentDataProvider
                .locationFrom(documentEntity), false));
        headerValues.add(new HeaderPair(translationService.translate(L_COMPANY, locale), DocumentDataProvider
                .company(documentEntity), false));
        headerValues.add(new HeaderPair(translationService.translate(L_TIME, locale), DocumentDataProvider.time(documentEntity),
                false));
        headerValues.add(new HeaderPair(translationService.translate(L_LOCATION_TO, locale), DocumentDataProvider
                .locationTo(documentEntity), false));
        headerValues.add(new HeaderPair(translationService.translate(L_STATE, locale), translationService.translate(L_STATE_VALUE
                + DocumentDataProvider.state(documentEntity), locale), false));
        return headerValues;
    }

    /**
     * Returns document description with label
     *
     * @param documentEntity
     * @param locale
     * @return
     */
    public HeaderPair getDescription(final Entity documentEntity, final Locale locale) {
        return new HeaderPair(translationService.translate(L_DESCRIPTION, locale),
                DocumentDataProvider.description(documentEntity), false);
    }

    public HeaderPair getDelivery(final Entity documentEntity, final Locale locale) {
        return new HeaderPair(translationService.translate(L_DELIVERY, locale), DocumentDataProvider.delivery(documentEntity),
                false);
    }

    /**
     * Returns map of translated positions' table headers and alignments
     *
     * @param locale
     * @return
     */
    public Map<String, HeaderAlignmentWithWidth> getPositionsTableHeaderLabels(final Locale locale) {
        Map<String, HeaderAlignmentWithWidth> headerLabels = Maps.newLinkedHashMap();

        Entity documentPositionParameters = parameterService.getParameter().getBelongsToField(
                ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);

        boolean notShowPrices = documentPositionParameters.getBooleanField("notShowPrices");
        boolean presentTotalAmountAndRest = documentPositionParameters.getBooleanField("presentTotalAmountAndRest");

        headerLabels.put(translationService.translate(L_POSITION_HEADER__INDEX, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.LEFT, 20));
        headerLabels.put(translationService.translate(L_POSITION_HEADER__NUMBER, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.LEFT, 90));
        headerLabels.put(translationService.translate(L_POSITION_HEADER__QUANTITY, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.RIGHT, 40));
        headerLabels.put(translationService.translate(L_POSITION_HEADER__UNIT, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.LEFT, 30));
        headerLabels.put(translationService.translate(L_POSITION_HEADER__QUANTITY_ADD, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.RIGHT, 40));
        headerLabels.put(translationService.translate(L_POSITION_HEADER__UNIT_ADD, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.LEFT, 30));
        if (presentTotalAmountAndRest) {
            headerLabels.put(translationService.translate(L_POSITION_HEADER__TOTAL_REST, locale), new HeaderAlignmentWithWidth(
                    HeaderAlignment.RIGHT, 60));
        }

        if (!notShowPrices) {
            headerLabels.put(translationService.translate(L_POSITION_HEADER__PRICE, locale), new HeaderAlignmentWithWidth(
                    HeaderAlignment.RIGHT, 40));
        }
        headerLabels.put(translationService.translate(L_POSITION_HEADER__BATCH, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.LEFT, 50));
        headerLabels.put(translationService.translate(L_POSITION_HEADER__PRODUCTION_DATE, locale), new HeaderAlignmentWithWidth(
                HeaderAlignment.LEFT, 50));
        if (!notShowPrices) {
            headerLabels.put(translationService.translate(L_POSITION_HEADER__VALUE, locale), new HeaderAlignmentWithWidth(
                    HeaderAlignment.RIGHT, 50));
        }
        return headerLabels;
    }

    public Map<String, HeaderAlignment> headerValues(Map<String, HeaderAlignmentWithWidth> header) {
        Map<String, HeaderAlignment> headerValues = Maps.newHashMap();
        header.forEach((a, b) -> {
            headerValues.put(a, b.getAlignment());
        });
        return headerValues;
    }

    public void addHeaderTable(Document document, Entity documentEntity, Locale locale) throws DocumentException {

        Entity documentPositionParameters = parameterService.getParameter().getBelongsToField(
                ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);

        boolean notShowPrices = documentPositionParameters.getBooleanField("notShowPrices");

        PdfPTable table = pdfHelper.createPanelTable(3);

        List<HeaderPair> headerValues = getDocumentHeaderTableContent(documentEntity, locale);
        for (HeaderPair pair : headerValues) {
            if (pair.getValue() != null && !pair.getValue().isEmpty()) {
                pdfHelper.addTableCellAsOneColumnTable(table, pair.getLabel(), pair.getValue());
            } else {
                pdfHelper.addTableCellAsOneColumnTable(table, StringUtils.EMPTY, StringUtils.EMPTY);
            }
        }
        HeaderPair description = getDescription(documentEntity, locale);

        Phrase label = new Phrase(description.getValue().isEmpty() ? StringUtils.EMPTY : description.getLabel(),
                FontUtils.getDejavuBold8Dark());
        PdfPCell descriptionLabelCell = new PdfPCell(label);
        descriptionLabelCell.setColspan(2);
        descriptionLabelCell.setBorder(PdfPCell.NO_BORDER);
        descriptionLabelCell.setPaddingLeft(7);
        table.addCell(descriptionLabelCell);

        if (!notShowPrices) {
            Phrase totalValueLabel = new Phrase(getTotalValueLabel(locale), FontUtils.getDejavuBold8Dark());
            PdfPCell totalValueLabelCell = new PdfPCell(totalValueLabel);
            totalValueLabelCell.setBorder(PdfPCell.NO_BORDER);
            totalValueLabelCell.setPaddingLeft(7);
            table.addCell(totalValueLabelCell);
            table.completeRow();

        } else {
            Phrase totalValueLabel = new Phrase("", FontUtils.getDejavuBold8Dark());
            PdfPCell totalValueLabelCell = new PdfPCell(totalValueLabel);
            totalValueLabelCell.setBorder(PdfPCell.NO_BORDER);
            totalValueLabelCell.setPaddingLeft(7);
            table.addCell(totalValueLabelCell);
            table.completeRow();
        }
        Phrase value = new Phrase(description.getValue(), FontUtils.getDejavuRegular9Dark());
        PdfPCell descriptionValueCell = new PdfPCell(value);
        descriptionValueCell.setColspan(2);
        descriptionValueCell.setBorder(PdfPCell.NO_BORDER);
        descriptionValueCell.setPaddingLeft(7);
        descriptionValueCell.setPaddingBottom(15);
        table.addCell(descriptionValueCell);

        if (!notShowPrices) {
            Phrase totalValue = new Phrase(PositionDataProvider.totalValue(documentEntity), FontUtils.getDejavuRegular9Dark());
            PdfPCell totalValueCell = new PdfPCell(totalValue);
            totalValueCell.setBorder(PdfPCell.NO_BORDER);
            totalValueCell.setPaddingLeft(7);
            totalValueCell.setPaddingBottom(15);
            table.addCell(totalValueCell);
        } else {
            Phrase totalValue = new Phrase("", FontUtils.getDejavuRegular9Dark());
            PdfPCell totalValueCell = new PdfPCell(totalValue);
            totalValueCell.setBorder(PdfPCell.NO_BORDER);
            totalValueCell.setPaddingLeft(7);
            totalValueCell.setPaddingBottom(15);
            table.addCell(totalValueCell);
        }
        table.completeRow();

        HeaderPair delivery = getDelivery(documentEntity, locale);
        if (!delivery.getValue().isEmpty()) {
            pdfHelper.addTableCellAsOneColumnTable(table, delivery.getLabel(), delivery.getValue());
            table.completeRow();
        }

        document.add(table);

    }

    public int[] headerWidths(Map<String, HeaderAlignmentWithWidth> header) {
        return header.values().stream().mapToInt(w -> w.getWidth()).toArray();
    }

    public static class HeaderPair {

        private String label;

        private String value;

        private boolean boldAndBigger;

        public HeaderPair(String label, String value, boolean boldAndBigger) {
            this.setLabel(label);
            this.setValue(value);
            this.setBoldAndBigger(boldAndBigger);
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isBoldAndBigger() {
            return boldAndBigger;
        }

        public void setBoldAndBigger(boolean boldAndBigger) {
            this.boldAndBigger = boldAndBigger;
        }
    }
}
