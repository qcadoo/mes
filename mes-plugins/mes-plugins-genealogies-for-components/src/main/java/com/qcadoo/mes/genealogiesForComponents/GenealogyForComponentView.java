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
package com.qcadoo.mes.genealogiesForComponents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.genealogiesForComponents.constants.GenealogiesForComponentsConstants;
import com.qcadoo.mes.genealogiesForComponents.util.EntityOrderNumberComparator;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

public class GenealogyForComponentView extends ReportPdfView {

    private static final String BATCH_FIELD = "batch";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        Entity entity = dataDefinitionService.get(GenealogiesForComponentsConstants.PLUGIN_IDENTIFIER,
                GenealogiesForComponentsConstants.MODEL_PRODUCT_IN_BATCH).get(Long.valueOf(model.get("value").toString()));
        String documentTitle = getTranslationService().translate("genealogiesForComponents.genealogyForComponent.report.title",
                locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());
        addTables(document, entity, locale);
        String text = getTranslationService().translate("qcadooReport.commons.endOfPrint.label", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("genealogiesForComponents.genealogyForComponent.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService()
                .translate("genealogiesForComponents.genealogyForComponent.report.title", locale));
    }

    private void addTables(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("orders.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.product.label", locale));
        orderHeader.add(getTranslationService().translate("genealogiesForComponents.genealogyForComponent.report.productBatch",
                locale));
        Paragraph productTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "genealogiesForComponents.genealogyForComponent.report.paragrah.product", locale), PdfUtil.getArialBold11Light()));
        productTitle.setSpacingBefore(20);
        document.add(productTitle);
        PdfPTable headerData = PdfUtil.createPanelTable(3);
        headerData.setSpacingBefore(7);
        Entity product = entity.getBelongsToField("productInComponent").getBelongsToField("productInComponent")
                .getBelongsToField("product");
        PdfUtil.addTableCellAsTable(headerData, getTranslationService().translate("basic.product.number.label", locale),
                product.getField("number"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(headerData, getTranslationService().translate("basic.product.name.label", locale),
                product.getField("name"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(headerData,
                getTranslationService().translate("genealogiesForComponents.productInBatch.batch.label", locale),
                entity.getField(BATCH_FIELD), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        document.add(headerData);
        Paragraph orderTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "genealogiesForComponents.genealogyForComponent.report.paragrah.order", locale), PdfUtil.getArialBold11Light()));
        orderTitle.setSpacingBefore(20);
        document.add(orderTitle);
        addOrderSeries(document, entity, orderHeader);
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        PdfPTable table = PdfUtil.createTableWithHeader(4, orderHeader, false);
        List<Entity> genealogies = getGenealogies(entity);
        Collections.sort(genealogies, new EntityOrderNumberComparator());
        for (Entity genealogy : genealogies) {
            Entity order = (Entity) genealogy.getField("order");
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            Entity product = (Entity) order.getField("product");
            if (product == null) {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase(product.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            }
            table.addCell(new Phrase(genealogy.getField(BATCH_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
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
