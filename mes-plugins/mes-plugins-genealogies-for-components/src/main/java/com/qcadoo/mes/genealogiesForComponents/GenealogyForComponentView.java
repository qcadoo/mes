/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.genealogiesForComponents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.genealogiesForComponents.constants.GenealogiesForComponentsConstants;
import com.qcadoo.mes.genealogiesForComponents.util.EntityOrderNumberComparator;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "genealogyForComponentView")
public class GenealogyForComponentView extends ReportPdfView {

    private static final String BATCH_FIELD = "batch";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        Entity entity = dataDefinitionService.get(GenealogiesForComponentsConstants.PLUGIN_IDENTIFIER,
                GenealogiesForComponentsConstants.MODEL_PRODUCT_IN_BATCH).get(Long.valueOf(model.get("value").toString()));
        String documentTitle = translationService
                .translate("genealogiesForComponents.genealogyForComponent.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());
        addTables(document, entity, locale);
        String text = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);
        pdfHelper.addEndOfDocument(document, writer, text);
        return translationService.translate("genealogiesForComponents.genealogyForComponent.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("genealogiesForComponents.genealogyForComponent.report.title", locale));
    }

    private void addTables(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("orders.order.number.label", locale));
        orderHeader.add(translationService.translate("orders.order.name.label", locale));
        orderHeader.add(translationService.translate("orders.order.product.label", locale));
        orderHeader.add(translationService
                .translate("genealogiesForComponents.genealogyForComponent.report.productBatch", locale));
        Paragraph productTitle = new Paragraph(new Phrase(translationService.translate(
                "genealogiesForComponents.genealogyForComponent.report.paragrah.product", locale),
                FontUtils.getDejavuBold11Light()));
        productTitle.setSpacingBefore(20);
        document.add(productTitle);
        PdfPTable headerData = pdfHelper.createPanelTable(3);
        headerData.setSpacingBefore(7);
        Entity product = entity.getBelongsToField("productInComponent").getBelongsToField("productInComponent")
                .getBelongsToField("product");
        pdfHelper.addTableCellAsOneColumnTable(headerData, translationService.translate("basic.product.number.label", locale),
                product.getField("number"));
        pdfHelper.addTableCellAsOneColumnTable(headerData, translationService.translate("basic.product.name.label", locale),
                product.getField("name"));
        pdfHelper.addTableCellAsOneColumnTable(headerData,
                translationService.translate("genealogiesForComponents.productInBatch.batch.label", locale),
                entity.getField(BATCH_FIELD));
        document.add(headerData);
        Paragraph orderTitle = new Paragraph(new Phrase(translationService.translate(
                "genealogiesForComponents.genealogyForComponent.report.paragrah.order", locale), FontUtils.getDejavuBold11Light()));
        orderTitle.setSpacingBefore(20);
        document.add(orderTitle);
        addOrderSeries(document, entity, orderHeader);
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        PdfPTable table = pdfHelper.createTableWithHeader(4, orderHeader, false);
        List<Entity> genealogies = getGenealogies(entity);
        Collections.sort(genealogies, new EntityOrderNumberComparator());
        for (Entity genealogy : genealogies) {
            Entity order = (Entity) genealogy.getField("order");
            table.addCell(new Phrase(order.getField("number").toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(order.getField("name").toString(), FontUtils.getDejavuRegular9Dark()));
            Entity product = (Entity) order.getField("product");
            if (product == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
            } else {
                table.addCell(new Phrase(product.getField("name").toString(), FontUtils.getDejavuRegular9Dark()));
            }
            table.addCell(new Phrase(genealogy.getField(BATCH_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
        }
        document.add(table);
    }

    private List<Entity> getGenealogies(final Entity entity) {
        List<Entity> genealogies = new ArrayList<Entity>();

        List<Entity> batchList = dataDefinitionService
                .get(GenealogiesForComponentsConstants.PLUGIN_IDENTIFIER,
                        GenealogiesForComponentsConstants.MODEL_PRODUCT_IN_BATCH).find()
                .add(SearchRestrictions.eq(BATCH_FIELD, entity.getField(BATCH_FIELD))).list().getEntities();

        for (Entity batch : batchList) {
            Entity genealogy = ((Entity) ((Entity) batch.getField("productInComponent")).getField("genealogy"));
            if (!genealogies.contains(genealogy)) {
                genealogies.add(genealogy);
            }
        }
        return genealogies;
    }
}
