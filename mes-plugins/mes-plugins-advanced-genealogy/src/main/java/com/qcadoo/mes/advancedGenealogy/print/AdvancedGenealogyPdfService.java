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

import com.qcadoo.mes.advancedGenealogy.constants.GenealogyReportFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchStateChangeFields;
import com.qcadoo.mes.advancedGenealogy.tree.AdvancedGenealogyTreeService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public class AdvancedGenealogyPdfService extends PdfDocumentService {

    private static final String L_DASH = " - ";

    private static final String L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE = "advancedGenealogy.batch.report.batchNumber";

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
        pdfHelper.addDocumentHeader(document, (String) entity.getField(GenealogyReportFields.NAME), documentTitle, documentAuthor,
                (Date) entity.getField(GenealogyReportFields.DATE));
        document.add(Chunk.NEWLINE);

        final PdfPTable panelTable = createDocumentPanelTable(entity, locale);
        document.add(panelTable);

        Entity rootBatch = entity.getBelongsToField(GenealogyReportFields.BATCH);

        String type = entity.getStringField(GenealogyReportFields.TYPE);
        List<Entity> batches = getAllRelatedBatches(rootBatch, type, entity.getBooleanField(GenealogyReportFields.INCLUDE_DRAFT));

        final List<String> batchHeader = new ArrayList<String>();
        batchHeader.add(translationService.translate("advancedGenealogy.batch.report.date", locale));
        batchHeader.add(translationService.translate(L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale));
        batchHeader.add(translationService.translate("advancedGenealogy.batch.report.productNumber", locale));
        batchHeader.add(translationService.translate("advancedGenealogy.batch.report.productName", locale));
        if (AdvancedGenealogyConstants.L_PRODUCED_FROM.equals(type)) {
            batchHeader.add(translationService.translate("advancedGenealogy.batch.report.company", locale));
        } else {
            batchHeader.add(translationService.translate("advancedGenealogy.batch.report.order", locale));
        }

        boolean directOnly = entity.getBooleanField(GenealogyReportFields.DIRECT_RELATED_ONLY);

        if (batches.size() > 1) {
            generateBatchTable(document, batches, rootBatch, type, batchHeader, locale, !directOnly);
        } else {
            createPanelForEmptyBatch(document, rootBatch, locale);
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
            addBatchSeries(document, children, batchHeader, locale, type);

            if (youHaveToGoDeeper) {
                for (Entity child : children) {
                    generateBatchTable(document, batches, child, type, batchHeader, locale, true);
                }
            }
        }
    }

    private List<Entity> getAllRelatedBatches(final Entity batch, final String type, final boolean includeDraft) {
        List<Entity> batches;
        if (AdvancedGenealogyConstants.L_USED_TO_PRODUCE.equals(type)) {
            batches = treeService.getUsedToProduceTree(batch, includeDraft, false);
        } else if (AdvancedGenealogyConstants.L_PRODUCED_FROM.equals(type)) {
            batches = treeService.getProducedFromTree(batch, includeDraft, false);
        } else {
            throw new IllegalStateException("");
        }
        return batches;
    }

    private void createPanelForBatch(final Document document, final Entity batch, final Locale locale, final String type)
            throws DocumentException {
        final StringBuilder header = new StringBuilder();
        final Entity product = batch.getBelongsToField(BatchFields.PRODUCT);
        final Entity supplier = batch.getBelongsToField(BatchFields.SUPPLIER);
        final StringBuilder footer = new StringBuilder();

        header.append(translationService.translate(L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale)).append(" ");
        header.append(batch.getField(BatchFields.NUMBER));
        header.append(L_DASH).append(product.getField(ProductFields.NAME)).append(" ( ")
                .append(product.getField(ProductFields.NUMBER)).append(" )");
        if (supplier != null) {
            header.append(L_DASH).append(supplier.getField(CompanyFields.NAME));
        }
        if (AdvancedGenealogyConstants.L_PRODUCED_FROM.equals(type)) {
            String orders = treeService.getOrdersForBatch(batch);
            if (!orders.isEmpty()) {
                header.append(" ").append(translationService.translate("advancedGenealogy.batch.report.order", locale))
                        .append(AdvancedGenealogyConstants.L_SPACER).append(orders);
            }
        }

        if (AdvancedGenealogyConstants.L_USED_TO_PRODUCE.equals(type)) {
            footer.append(translationService.translate("advancedGenealogy.genealogyReport.type.value.01usedToProduce", locale));
        } else if (AdvancedGenealogyConstants.L_PRODUCED_FROM.equals(type)) {
            footer.append(translationService.translate("advancedGenealogy.genealogyReport.type.value.02producedFrom", locale));
        } else {
            throw new IllegalStateException("Type of genealogy table should be specified");
        }

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(header.toString(), FontUtils.getDejavuBold11Dark()));
        document.add(Chunk.NEWLINE);

        Paragraph typeOfReport = new Paragraph(footer.toString(), FontUtils.getDejavuBold9Dark());
        typeOfReport.setAlignment(Chunk.ALIGN_CENTER);

        document.add(typeOfReport);
    }

    private void createPanelForEmptyBatch(final Document document, final Entity batch, final Locale locale)
            throws DocumentException {
        final StringBuilder header = new StringBuilder();
        final Entity product = batch.getBelongsToField(BatchFields.PRODUCT);
        final Entity supplier = batch.getBelongsToField(BatchFields.SUPPLIER);

        header.append(translationService.translate(L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale)).append(" ");
        header.append(batch.getField(BatchFields.NUMBER));
        header.append(L_DASH).append(product.getField(ProductFields.NAME)).append(" ( ")
                .append(product.getField(ProductFields.NUMBER)).append(" )");
        if (supplier != null) {
            header.append(L_DASH).append(supplier.getField(CompanyFields.NAME));
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
        final Entity batch = entity.getBelongsToField(GenealogyReportFields.BATCH);
        final TranslationService translation = translationService;
        String batchNumber = translation.translate(L_ADVANCED_GENEALOGY_BATCH_REPORT_BATCH_NUMBER_MESSAGE, locale)
                + AdvancedGenealogyConstants.L_SPACER + batch.getField(BatchFields.NUMBER);
        final StringBuilder type = new StringBuilder(translation.translate("advancedGenealogy.batch.report.type", locale))
                .append(AdvancedGenealogyConstants.L_SPACER);
        if (AdvancedGenealogyConstants.L_USED_TO_PRODUCE.equals(entity.getField(GenealogyReportFields.TYPE))) {
            type.append(translation.translate("advancedGenealogy.genealogyReport.type.value.01usedToProduce", locale));
        } else if (AdvancedGenealogyConstants.L_PRODUCED_FROM.equals(entity.getField(GenealogyReportFields.TYPE))) {
            type.append(translation.translate("advancedGenealogy.genealogyReport.type.value.02producedFrom", locale));
        } else {
            throw new IllegalStateException("Type of genealogy table should be specified");
        }
        final StringBuilder includeDraft = new StringBuilder(
                translation.translate("advancedGenealogy.batch.report.includeDraft", locale))
                        .append(AdvancedGenealogyConstants.L_SPACER);
        if (entity.getBooleanField(GenealogyReportFields.INCLUDE_DRAFT)) {
            includeDraft.append(translation.translate("qcadooView.true", locale));
        } else {
            includeDraft.append(translation.translate("qcadooView.false", locale));
        }
        final StringBuilder directRelatedOnly = new StringBuilder(
                translation.translate("advancedGenealogy.batch.report.directRelatedOnly", locale))
                        .append(AdvancedGenealogyConstants.L_SPACER);
        if (entity.getBooleanField(GenealogyReportFields.DIRECT_RELATED_ONLY)) {
            directRelatedOnly.append(translation.translate("qcadooView.true", locale));
        } else {
            directRelatedOnly.append(translation.translate("qcadooView.false", locale));
        }

        panel.addCell(new Phrase(batchNumber, FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(type.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(includeDraft.toString(), FontUtils.getDejavuBold9Dark()));
        panel.addCell(new Phrase(directRelatedOnly.toString(), FontUtils.getDejavuBold9Dark()));
        return panel;
    }

    private void addBatchSeries(final Document document, final List<Entity> batches, final List<String> batchHeader,
            final Locale locale, String type) throws DocumentException {
        PdfPTable table = pdfHelper.createTableWithHeader(5, batchHeader, false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        for (Entity batch : batches) {
            final List<Entity> stateChanges = batch.getHasManyField(BatchFields.STATE_CHANGES);
            final Entity firstStateChange = getFirstSuccessfulStateChange(stateChanges);
            Phrase datePhrase = new Phrase();
            if (firstStateChange != null) {
                final Date date = (Date) firstStateChange.getField(BatchStateChangeFields.DATE_AND_TIME);
                if (date != null) {
                    datePhrase = new Phrase(dateFormat.format(date), FontUtils.getDejavuRegular7Dark());
                }
            }
            table.addCell(datePhrase);
            table.addCell(new Phrase((String) batch.getField(BatchFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase((String) batch.getBelongsToField(BatchFields.PRODUCT).getField(ProductFields.NUMBER),
                    FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase((String) batch.getBelongsToField(BatchFields.PRODUCT).getField(ProductFields.NAME),
                    FontUtils.getDejavuRegular7Dark()));
            if (AdvancedGenealogyConstants.L_PRODUCED_FROM.equals(type)) {
                table.addCell(new Phrase(
                        (batch.getBelongsToField(BatchFields.SUPPLIER) == null) ? " "
                                : (String) batch.getBelongsToField(BatchFields.SUPPLIER).getField(CompanyFields.NAME),
                        FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(treeService.getOrdersForBatch(batch), FontUtils.getDejavuRegular7Dark()));
            }
        }

        document.add(table);
    }

    private Entity getFirstSuccessfulStateChange(final List<Entity> stateChanges) {
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
