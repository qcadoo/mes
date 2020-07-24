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
package com.qcadoo.mes.advancedGenealogy.print;

import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.model.api.*;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newLinkedHashMap;

@Component(value = "trackingRecordPdfView")
public class TrackingRecordPdfView extends ReportPdfView {

    private static final String L_WORKER = "worker";

    private static final String L_DATE_AND_TIME = "dateAndTime";

    private static final String L_BATCH = "batch";

    private static final String L_QUANTITY = "quantity";

    private static final String L_NAME = "name";

    private static final String L_UNIT = "unit";

    private static final String L_SUPPLIER = "supplier";

    private static final String L_PRODUCT = "product";

    private static final String L_NUMBER = "number";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected final String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved technology! (missing id)");

        DataDefinition trackingRecordDD = dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyConstants.MODEL_TRACKING_RECORD);

        Entity trackingRecord = trackingRecordDD.get(Long.valueOf(model.get("id").toString()));

        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);

        String producedBatchNumber = producedBatch.getStringField(L_NUMBER);
        String producedBatchProductNumber = producedBatch.getBelongsToField(L_PRODUCT).getStringField(L_NUMBER);
        String producedBatchProductName = producedBatch.getBelongsToField(L_PRODUCT).getStringField(L_NAME);
        String producedBatchProductNameNumber = " - " + producedBatchProductName + "(" + producedBatchProductNumber + ")";
        String producedBatchSupplierName = (producedBatch.getBelongsToField(L_SUPPLIER) == null) ? " " : " - "
                + producedBatch.getBelongsToField(L_SUPPLIER).getStringField(L_NAME);

        String documentTitle = translationService.translate("advancedGenealogy.trackingRecordSimpleDetails.report.title",
                locale) + " " + producedBatchNumber + producedBatchProductNameNumber + producedBatchSupplierName;
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        createInfoTable(document, locale, trackingRecord);

        createUsedBatchesTable(document, locale, trackingRecord);

        createLoggingsTable(document, locale, trackingRecord);

        return translationService.translate("advancedGenealogy.trackingRecordSimpleDetails.report.fileName", locale);
    }

    private void createInfoTable(final Document document, final Locale locale, final Entity trackingRecord)
            throws DocumentException {
        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);

        String producedBatchNumber = producedBatch.getStringField(L_NUMBER);
        String producedBatchProductNumber = producedBatch.getBelongsToField(L_PRODUCT).getStringField(L_NUMBER);
        String producedBatchProductName = producedBatch.getBelongsToField(L_PRODUCT).getStringField(L_NAME);
        String producedBatchProductNameNumber = producedBatchProductName + "(" + producedBatchProductNumber + ")";
        String producedBatchProductUnit = producedBatch.getBelongsToField(L_PRODUCT).getStringField(L_UNIT);
        String producedBatchSupplierName = (producedBatch.getBelongsToField(L_SUPPLIER) == null) ? " " : producedBatch
                .getBelongsToField(L_SUPPLIER).getStringField("L_NAME");
        String producedBatchQuantity = (trackingRecord.getField(L_QUANTITY) == null) ? " " : numberService.format(trackingRecord
                .getField(L_QUANTITY)) + " " + producedBatchProductUnit;

        Map<String, String> panelTableValues = newLinkedHashMap();

        panelTableValues.put(L_BATCH, producedBatchNumber);
        panelTableValues.put(L_PRODUCT, producedBatchProductNameNumber);
        panelTableValues.put(L_SUPPLIER, producedBatchSupplierName);
        panelTableValues.put(L_QUANTITY, producedBatchQuantity);

        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        for (Map.Entry<String, String> panelEntry : panelTableValues.entrySet()) {
            pdfHelper.addTableCellAsOneColumnTable(
                    panelTable,
                    translationService.translate(
                            "advancedGenealogy.trackingRecordSimpleDetails.report.panel." + panelEntry.getKey(), locale),
                    panelEntry.getValue());
        }

        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);

        document.add(panelTable);
    }

    private void createUsedBatchesTable(final Document document, final Locale locale, final Entity trackingRecord)
            throws DocumentException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);

        String usedBatchesHeader = translationService.translate(
                "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches", locale);

        document.add(new Paragraph(usedBatchesHeader, FontUtils.getDejavuBold11Dark()));

        EntityList usedBatches = trackingRecord.getHasManyField("usedBatchesSimple");

        if (usedBatches.isEmpty()) {
            String usedBatchesEmpty = translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatchesEmpty", locale);
            document.add(new Paragraph(usedBatchesEmpty, FontUtils.getDejavuRegular10Dark()));
            document.add(Chunk.NEWLINE);
        } else {
            List<String> usedBatchesTableHeader = new ArrayList<String>();

            usedBatchesTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.batchNumber", locale));
            usedBatchesTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.productNumber", locale));
            usedBatchesTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.productName", locale));
            usedBatchesTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.quantity", locale));
            usedBatchesTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.productUnit", locale));
            usedBatchesTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.enteredDate", locale));
            usedBatchesTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.enteredBy", locale));
            Map<String, HeaderAlignment> alignments = Maps.newHashMap();
            alignments.put(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.batchNumber", locale),
                    HeaderAlignment.LEFT);
            alignments.put(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.productNumber", locale),
                    HeaderAlignment.LEFT);
            alignments.put(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.productName", locale),
                    HeaderAlignment.LEFT);
            alignments.put(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.quantity", locale),
                    HeaderAlignment.RIGHT);
            alignments.put(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.productUnit", locale),
                    HeaderAlignment.LEFT);
            alignments.put(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.enteredDate", locale),
                    HeaderAlignment.LEFT);
            alignments.put(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.usedBatches.column.enteredBy", locale),
                    HeaderAlignment.LEFT);

            PdfPTable usedBatchesTable = pdfHelper.createTableWithHeader(7, usedBatchesTableHeader, false, alignments);

            for (Entity usedBatch : usedBatches) {
                Entity batch = usedBatch.getBelongsToField(L_BATCH);

                String usedBatchNumber = batch.getStringField(L_NUMBER);
                String usedBatchProductNumber = batch.getBelongsToField(L_PRODUCT).getStringField(L_NUMBER);
                String usedBatchProductName = batch.getBelongsToField(L_PRODUCT).getStringField(L_NAME);
                String usedBatchProductUnit = batch.getBelongsToField(L_PRODUCT).getStringField(L_UNIT);
                String usedBatchQuantity = (usedBatch.getField(L_QUANTITY) == null) ? " " : numberService.format(usedBatch
                        .getField(L_QUANTITY));

                Date usedEnteredDate = (Date) usedBatch.getField(L_DATE_AND_TIME);

                String usedBatchEnteredDate = dateFormat.format(usedEnteredDate);

                String usedBatchEnteredBy = usedBatch.getStringField(L_WORKER);

                usedBatchesTable.addCell(new Phrase(usedBatchNumber, FontUtils.getDejavuRegular7Dark()));
                usedBatchesTable.addCell(new Phrase(usedBatchProductNumber, FontUtils.getDejavuRegular7Dark()));
                usedBatchesTable.addCell(new Phrase(usedBatchProductName, FontUtils.getDejavuRegular7Dark()));
                usedBatchesTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                usedBatchesTable.addCell(new Phrase(usedBatchQuantity, FontUtils.getDejavuRegular7Dark()));
                usedBatchesTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                usedBatchesTable.addCell(new Phrase(usedBatchProductUnit, FontUtils.getDejavuRegular7Dark()));
                usedBatchesTable.addCell(new Phrase(usedBatchEnteredDate, FontUtils.getDejavuRegular7Dark()));
                usedBatchesTable.addCell(new Phrase(usedBatchEnteredBy, FontUtils.getDejavuRegular7Dark()));
            }

            usedBatchesTable.setSpacingAfter(20);
            usedBatchesTable.setSpacingBefore(20);

            document.add(usedBatchesTable);
        }
    }

    private void createLoggingsTable(final Document document, final Locale locale, final Entity trackingRecord)
            throws DocumentException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);

        String loggingsHeader = translationService.translate("advancedGenealogy.trackingRecordSimpleDetails.report.loggings",
                locale);

        document.add(new Paragraph(loggingsHeader, FontUtils.getDejavuBold11Dark()));

        EntityList loggings = trackingRecord.getHasManyField("loggings");

        if (loggings.isEmpty()) {
            String loggingsEmpty = translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.loggingsEmpty", locale);

            document.add(new Paragraph(loggingsEmpty, FontUtils.getDejavuRegular10Dark()));

            document.add(Chunk.NEWLINE);
        } else {
            List<String> loggingsTableHeader = new ArrayList<String>();

            loggingsTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.loggings.column.enteredDate", locale));
            loggingsTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.loggings.column.currentState", locale));
            loggingsTableHeader.add(translationService.translate(
                    "advancedGenealogy.trackingRecordSimpleDetails.report.loggings.column.enteredBy", locale));

            PdfPTable loggingsTable = pdfHelper.createTableWithHeader(3, loggingsTableHeader, false);

            for (Entity logging : loggings) {
                String loggingState = logging.getStringField("currentState");

                String loggingCurrentState = translationService.translate(
                        "advancedGenealogy.trackingRecordSimpleDetails.report.loggings.state." + loggingState, locale);

                Date loggingDate = (Date) logging.getField(L_DATE_AND_TIME);
                String loggingEnteredDate = dateFormat.format(loggingDate);

                String loggingEnteredBy = logging.getStringField(L_WORKER);

                loggingsTable.addCell(new Phrase(loggingEnteredDate, FontUtils.getDejavuRegular7Dark()));
                loggingsTable.addCell(new Phrase(loggingCurrentState, FontUtils.getDejavuRegular7Dark()));
                loggingsTable.addCell(new Phrase(loggingEnteredBy, FontUtils.getDejavuRegular7Dark()));
            }

            loggingsTable.setSpacingAfter(20);
            loggingsTable.setSpacingBefore(20);

            document.add(loggingsTable);
        }
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("advancedGenealogy.trackingRecordSimpleDetails.report.title", locale));
    }

}
