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
package com.qcadoo.mes.qualityControls.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.qualityControls.print.utils.EntityBatchNumberComparator;
import com.qcadoo.mes.qualityControls.print.utils.EntityNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "qualityControlForBatchPdfView")
public class QualityControlForBatchPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private QualityControlsReportService qualityControlsReportService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private NumberService numberService;

    @Override
    protected final String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        String documentTitle = translationService.translate("qualityControls.qualityControlForBatch.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        qualityControlsReportService.addQualityControlReportHeader(document, model, locale);
        List<Entity> orders = qualityControlsReportService.getOrderSeries(model, "qualityControlsForBatch");
        Map<Entity, List<Entity>> productOrders = qualityControlsReportService.getQualityOrdersForProduct(orders);
        Map<Entity, List<BigDecimal>> quantities = qualityControlsReportService.getQualityOrdersQuantitiesForProduct(orders);

        quantities = SortUtil.sortMapUsingComparator(quantities, new EntityNumberComparator());
        addOrderSeries(document, quantities, locale);
        productOrders = SortUtil.sortMapUsingComparator(productOrders, new EntityNumberComparator());

        for (Entry<Entity, List<Entity>> entry : productOrders.entrySet()) {
            document.add(Chunk.NEWLINE);
            addProductSeries(document, entry, locale);
        }
        String text = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);
        pdfHelper.addEndOfDocument(document, writer, text);
        return translationService.translate("qualityControls.qualityControlForBatch.report.fileName", locale);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("qualityControls.qualityControlForBatch.report.title", locale));
    }

    private void addOrderSeries(final Document document, final Map<Entity, List<BigDecimal>> quantities, final Locale locale)
            throws DocumentException {
        List<String> qualityHeader = new ArrayList<String>();
        qualityHeader.add(translationService.translate("qualityControls.qualityControl.report.product.number", locale));
        qualityHeader
                .add(translationService
                        .translate(
                                "qualityControlsForBatch.qualityControlForBatchDetails.window.mainTab.qualityControlForBatch.controlledQuantity.label",
                                locale));
        qualityHeader
                .add(translationService
                        .translate(
                                "qualityControlsForBatch.qualityControlForBatchDetails.window.mainTab.qualityControlForBatch.rejectedQuantity.label",
                                locale));
        qualityHeader
                .add(translationService
                        .translate(
                                "qualityControlsForBatch.qualityControlForBatchDetails.window.mainTab.qualityControlForBatch.acceptedDefectsQuantity.label",
                                locale));
        PdfPTable table = pdfHelper.createTableWithHeader(4, qualityHeader, false);
        for (Entry<Entity, List<BigDecimal>> entry : quantities.entrySet()) {
            table.addCell(new Phrase(entry.getKey() == null ? "" : entry.getKey().getField("number").toString(), FontUtils
                    .getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(numberService.format(entry.getValue().get(0)), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(numberService.format(entry.getValue().get(1)), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(numberService.format(entry.getValue().get(2)), FontUtils.getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    private void addProductSeries(final Document document, final Entry<Entity, List<Entity>> entry, final Locale locale)
            throws DocumentException {

        document.add(qualityControlsReportService.prepareTitle(entry.getKey(), locale, "batch"));

        List<String> productHeader = new ArrayList<String>();
        productHeader.add(translationService.translate("qualityControls.qualityControl.report.batch.number", locale));
        productHeader.add(translationService.translate("qualityControls.qualityControl.report.control.number", locale));
        productHeader.add(translationService.translate("qualityControls.qualityControl.report.controlled.quantity", locale));
        productHeader.add(translationService.translate("qualityControls.qualityControl.report.rejected.quantity", locale));
        productHeader
                .add(translationService.translate("qualityControls.qualityControl.report.accepted.defects.quantity", locale));
        PdfPTable table = pdfHelper.createTableWithHeader(5, productHeader, false);

        List<Entity> sortedOrders = entry.getValue();

        Collections.sort(sortedOrders, new EntityBatchNumberComparator());

        for (Entity entity : sortedOrders) {
            table.addCell(new Phrase(entity.getField("batchNr") == null ? "" : entity.getField("batchNr").toString(), FontUtils
                    .getDejavuRegular9Dark()));
            table.addCell(new Phrase(entity.getField("number").toString(), FontUtils.getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(numberService.format(entity.getField("controlledQuantity")), FontUtils
                    .getDejavuRegular9Dark()));
            table.addCell(new Phrase(numberService.format(entity.getField("rejectedQuantity")), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(numberService.format(entity.getField("acceptedDefectsQuantity")), FontUtils
                    .getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }

        document.add(table);
    }
}
