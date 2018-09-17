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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchStateChangeFields;
import com.qcadoo.mes.advancedGenealogy.tree.AdvancedGenealogyTreeService;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public class AdvancedGenealogyPdfService extends PdfDocumentService {

    private static final String L_DASH = " - ";

    private static final String L_SPACER = " : ";

    private static final String L_SUPPLIER = "supplier";

    private static final String L_PRODUCT = "product";

    private static final String L_PRODUCED_FROM = "02producedFrom";

    private static final String L_USED_TO_PRODUCE = "01usedToProduce";

    private static final String L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE = "advancedGenealogy.batch.report.batchNumber";

    private static final String L_NAME = "name";

    private static final String L_TYPE = "type";

    private static final String L_NUMBER = "number";

    @Autowired
    private AdvancedGenealogyTreeService treeService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        Preconditions.checkArgument(entity != null, "Batch is required");
        final String documentTitle = getReportTitle(locale);
        final String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, (String) entity.getField(L_NAME), documentTitle, documentAuthor,
                (Date) entity.getField("date"));
        document.add(Chunk.NEWLINE);

        final PdfPTable panelTable = createDocumentPanelTable(entity, locale);
        document.add(panelTable);

        Entity rootBatch = entity.getBelongsToField("batch");

        List<Entity> batches = getAllRelatedBatches(rootBatch, entity.getStringField(L_TYPE),
                entity.getBooleanField("includeDraft"));

        final List<String> batchHeader = new ArrayList<String>();
        batchHeader.add(translationService.translate("advancedGenealogy.batch.report.date", locale));
        batchHeader.add(translationService.translate(L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale));
        batchHeader.add(translationService.translate("advancedGenealogy.batch.report.productNumber", locale));
        batchHeader.add(translationService.translate("advancedGenealogy.batch.report.productName", locale));
        batchHeader.add(translationService.translate("advancedGenealogy.batch.report.company", locale));

        String type = entity.getStringField(L_TYPE);
        boolean directOnly = entity.getBooleanField("directRelatedOnly");

        if (batches.size() > 1) {
            generateBatchTable(document, batches, rootBatch, type, batchHeader, locale, !directOnly);
        } else {
            createPanelForEmptyBatch(document, rootBatch, locale, type);
        }
    }

    private void generateBatchTable(final Document document, final List<Entity> batches, final Entity parent, final String type,
            final List<String> batchHeader, final Locale locale, final boolean youHaveToGoDeeper) throws DocumentException {

        List<Entity> children = new ArrayList<Entity>();

        for (Entity batch : batches) {
            Entity par = batch.getBelongsToField("parent");
            if (par == null) {
                continue;
            }

            if (par.equals(parent)) {
                children.add(batch);
            }
        }

        if (!children.isEmpty()) {
            createPanelForBatch(document, parent, locale, type);
            addBatchSeries(document, children, batchHeader, locale);

            if (youHaveToGoDeeper) {
                for (Entity child : children) {
                    generateBatchTable(document, batches, child, type, batchHeader, locale, true);
                }
            }
        }
    }

    private List<Entity> getAllRelatedBatches(final Entity batch, final String type, final boolean includeDraft) {
        List<Entity> batches;
        if (L_USED_TO_PRODUCE.equals(type)) {
            batches = treeService.getUsedToProduceTree(batch, includeDraft, false);
        } else if (L_PRODUCED_FROM.equals(type)) {
            batches = treeService.getProducedFromTree(batch, includeDraft, false);
        } else {
            throw new IllegalStateException("");
        }
        return batches;
    }

    private void createPanelForBatch(final Document document, final Entity batch, final Locale locale, final String type)
            throws DocumentException {
        final PdfPTable panel = pdfHelper.createPanelTable(2);
        final StringBuilder header = new StringBuilder();
        final Entity product = batch.getBelongsToField(L_PRODUCT);
        final Entity supplier = batch.getBelongsToField(L_SUPPLIER);
        final StringBuilder footer = new StringBuilder();

        header.append(translationService.translate(L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale)).append(" ");
        header.append(batch.getField(L_NUMBER));
        header.append(L_DASH).append(product.getField(L_NAME)).append(" ( ").append(product.getField(L_NUMBER)).append(" )");
        if (supplier != null) {
            header.append(L_DASH).append(supplier.getField(L_NAME));
        }

        if (L_USED_TO_PRODUCE.equals(type)) {
            footer.append(translationService.translate("advancedGenealogy.genealogyReport.type.value.01usedToProduce", locale));
        } else if (L_PRODUCED_FROM.equals(type)) {
            footer.append(translationService.translate("advancedGenealogy.genealogyReport.type.value.02producedFrom", locale));
        } else {
            throw new IllegalStateException("Type of genealogy table should be specified");
        }

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(header.toString(), FontUtils.getDejavuBold11Dark()));
        document.add(Chunk.NEWLINE);

        final StringBuilder productNumber = new StringBuilder();
        final StringBuilder company = new StringBuilder();
        final StringBuilder productName = new StringBuilder();

        productNumber.append(translationService.translate("advancedGenealogy.batch.report.productNumber", locale));
        productNumber.append(L_SPACER).append(product.getField(L_NUMBER));

        company.append(translationService.translate("advancedGenealogy.batch.report.company", locale));
        if (supplier != null) {
            company.append(L_SPACER).append(supplier.getField(L_NAME));
        }

        productName.append(translationService.translate("advancedGenealogy.batch.report.productName", locale));
        productName.append(L_SPACER).append(product.getField(L_NAME));

        panel.getDefaultCell().setBorder(PdfCell.NO_BORDER);
        panel.addCell(new Phrase(productNumber.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(company.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(productName.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase());

        Paragraph typeOfReport = new Paragraph(footer.toString(), FontUtils.getDejavuBold9Dark());
        typeOfReport.setAlignment(Chunk.ALIGN_CENTER);

        document.add(typeOfReport);
    }

    private void createPanelForEmptyBatch(final Document document, final Entity batch, final Locale locale, final String type)
            throws DocumentException {
        final StringBuilder header = new StringBuilder();
        final Entity product = batch.getBelongsToField(L_PRODUCT);
        final Entity supplier = batch.getBelongsToField(L_SUPPLIER);
        final StringBuilder footer = new StringBuilder();

        header.append(translationService.translate(L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale)).append(" ");
        header.append(batch.getField(L_NUMBER));
        header.append(L_DASH).append(product.getField(L_NAME)).append(" ( ").append(product.getField(L_NUMBER)).append(" )");
        if (supplier != null) {
            header.append(L_DASH).append(supplier.getField(L_NAME));
        }

        if (L_USED_TO_PRODUCE.equals(type)) {
            footer.append(translationService.translate("advancedGenealogy.genealogyReport.type.value.01usedToProduce", locale));
        } else if (L_PRODUCED_FROM.equals(type)) {
            footer.append(translationService.translate("advancedGenealogy.genealogyReport.type.value.02producedFrom", locale));
        } else {
            throw new IllegalStateException("Type of genealogy table should be specified");
        }

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(header.toString(), FontUtils.getDejavuBold11Dark()));
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("advancedGenealogy.batch.report.noBatchTrackingRecord", locale),
                FontUtils.getDejavuBold11Dark()));

    }

    private PdfPTable createDocumentPanelTable(final Entity entity, final Locale locale) {
        final PdfPTable panel = pdfHelper.createPanelTable(2);
        final Entity batch = entity.getBelongsToField("batch");
        final TranslationService translation = translationService;
        final StringBuilder batchNumber = new StringBuilder(translation.translate(
                L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale));
        batchNumber.append(L_SPACER).append(batch.getField(L_NUMBER));
        final StringBuilder type = new StringBuilder(translation.translate("advancedGenealogy.batch.report.type", locale))
                .append(L_SPACER);
        if (L_USED_TO_PRODUCE.equals(entity.getField(L_TYPE))) {
            type.append(translation.translate("advancedGenealogy.genealogyReport.type.value.01usedToProduce", locale));
        } else if (L_PRODUCED_FROM.equals(entity.getField(L_TYPE))) {
            type.append(translation.translate("advancedGenealogy.genealogyReport.type.value.02producedFrom", locale));
        } else {
            throw new IllegalStateException("Type of genealogy table should be specified");
        }
        final StringBuilder includeDraft = new StringBuilder(translation.translate("advancedGenealogy.batch.report.includeDraft",
                locale)).append(L_SPACER);
        if (entity.getBooleanField("includeDraft")) {
            includeDraft.append(translation.translate("qcadooView.true", locale));
        } else {
            includeDraft.append(translation.translate("qcadooView.false", locale));
        }
        final StringBuilder directRelatedOnly = new StringBuilder(translation.translate(
                "advancedGenealogy.batch.report.directRelatedOnly", locale)).append(L_SPACER);
        if (entity.getBooleanField("directRelatedOnly")) {
            directRelatedOnly.append(translation.translate("qcadooView.true", locale));
        } else {
            directRelatedOnly.append(translation.translate("qcadooView.false", locale));
        }

        panel.addCell(new Phrase(batchNumber.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(type.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(includeDraft.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(directRelatedOnly.toString(), FontUtils.getDejavuBold9Dark()));
        return panel;
    }

    private void addBatchSeries(final Document document, final List<Entity> batches, final List<String> batchHeader,
            final Locale locale) throws DocumentException {
        PdfPTable table = pdfHelper.createTableWithHeader(5, batchHeader, false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        for (Entity batch : batches) {
            final List<Entity> stateChanges = batch.getHasManyField(BatchFields.STATE_CHANGES);
            final Entity firstStateChange = getFirstSuccessfullStateChange(stateChanges);
            Phrase datePhrase = new Phrase();
            if (firstStateChange != null) {
                final Date date = (Date) firstStateChange.getField(BatchStateChangeFields.DATE_AND_TIME);
                if (date != null) {
                    datePhrase = new Phrase(dateFormat.format(date), FontUtils.getDejavuRegular7Dark());
                }
            }
            table.addCell(datePhrase);
            table.addCell(new Phrase((String) batch.getField(L_NUMBER), FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase((String) batch.getBelongsToField(L_PRODUCT).getField(L_NUMBER), FontUtils
                    .getDejavuRegular7Dark()));
            table.addCell(new Phrase((String) batch.getBelongsToField(L_PRODUCT).getField(L_NAME), FontUtils
                    .getDejavuRegular7Dark()));
            table.addCell(new Phrase((batch.getBelongsToField(L_SUPPLIER) == null) ? " " : (String) batch.getBelongsToField(
                    L_SUPPLIER).getField(L_NAME), FontUtils.getDejavuRegular7Dark()));
        }

        document.add(table);
    }

    private Entity getFirstSuccessfullStateChange(final List<Entity> stateChanges) {
        for (Entity stateChange : stateChanges) {
            if (StateChangeStatus.SUCCESSFUL.getStringValue().equals(stateChange.getStringField(BatchStateChangeFields.STATUS))) {
                return stateChange;
            }
        }
        return null;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("advancedGenealogy.batch.report.title", locale);
    }

}
