/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.genealogies.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.genealogies.print.util.BatchOrderNrComparator;
import com.qcadoo.mes.genealogies.print.util.EntityNumberComparator;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.report.api.Pair;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

public class GenealogyForProductView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginAccessor pluginAccessor;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        Entity entity = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY)
                .get(Long.valueOf(model.get("value").toString()));
        String documentTitle = getTranslationService().translate("genealogies.genealogyForProduct.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());
        addTables(document, entity, locale);
        String text = getTranslationService().translate("qcadooReport.commons.endOfPrint.label", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("genealogies.genealogyForProduct.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("genealogies.genealogyForProduct.report.title", locale));
    }

    private void addTables(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("orders.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.dateFrom.label", locale));
        Paragraph productTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "genealogies.genealogyForProduct.report.paragrah.product", locale), PdfUtil.getArialBold11Light()));
        productTitle.setSpacingBefore(20);
        document.add(productTitle);
        PdfPTable headerData = PdfUtil.createPanelTable(3);
        headerData.setSpacingBefore(7);
        Entity product = entity.getBelongsToField("order").getBelongsToField("product");
        PdfUtil.addTableCellAsTable(headerData, getTranslationService().translate("basic.product.number.label", locale),
                product.getField("number"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(headerData, getTranslationService().translate("basic.product.name.label", locale),
                product.getField("name"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(headerData, getTranslationService().translate("genealogies.genealogy.batch.label", locale),
                entity.getField("batch"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        document.add(headerData);
        Paragraph orderTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "genealogies.genealogyForProduct.report.paragrah.order", locale), PdfUtil.getArialBold11Light()));
        orderTitle.setSpacingBefore(20);
        document.add(orderTitle);
        List<Entity> orders = getOrders(entity);
        Collections.sort(orders, new EntityNumberComparator());
        addOrderSeries(document, orders, orderHeader);
        if (pluginAccessor.getEnabledPlugin("genealogiesForComponents") != null) {
            addComponentSeries(document, orders, locale);
        }
    }

    private void addComponentSeries(final Document document, final List<Entity> orders, final Locale locale)
            throws DocumentException {
        for (Entity order : orders) {
            document.add(Chunk.NEWLINE);
            Paragraph title = new Paragraph(new Phrase(getTranslationService().translate(
                    "genealogies.genealogyForProduct.report.paragrah", locale), PdfUtil.getArialBold11Light()));
            title.add(new Phrase(" " + order.getField("number").toString(), PdfUtil.getArialBold19Dark()));
            document.add(title);
            List<String> componentHeader = new ArrayList<String>();
            componentHeader.add(getTranslationService().translate("basic.product.number.label", locale));
            componentHeader.add(getTranslationService().translate("basic.product.name.label", locale));
            componentHeader.add(getTranslationService().translate("genealogiesForComponents.productInBatch.batch.label", locale));
            PdfPTable table = PdfUtil.createTableWithHeader(3, componentHeader, false);

            List<Pair<String, Entity>> batchList = getBatchList(order);

            Collections.sort(batchList, new BatchOrderNrComparator());

            for (Pair<String, Entity> pair : batchList) {
                String batch = pair.getKey();
                Entity product = pair.getValue();
                table.addCell(new Phrase(product.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(product.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(batch, PdfUtil.getArialRegular9Dark()));
            }
            document.add(table);

        }
    }

    private void addOrderSeries(final Document document, final List<Entity> orders, final List<String> orderHeader)
            throws DocumentException {
        PdfPTable table = PdfUtil.createTableWithHeader(3, orderHeader, false);
        for (Entity order : orders) {
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("dateFrom").toString(), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    private List<Pair<String, Entity>> getBatchList(final Entity entity) {
        List<Pair<String, Entity>> batchList = new ArrayList<Pair<String, Entity>>();
        for (Entity genealogy : entity.getHasManyField("genealogies")) {
            for (Entity productInComponent : genealogy.getHasManyField("productInComponents")) {
                Entity product = (Entity) ((Entity) productInComponent.getField("productInComponent")).getField("product");
                for (Entity batch : productInComponent.getHasManyField("batch")) {
                    Pair<String, Entity> pair = Pair.of(batch.getField("batch").toString(), product);
                    if (!batchList.contains(pair)) {
                        batchList.add(pair);
                    }
                }
            }
        }
        return batchList;
    }

    private List<Entity> getOrders(final Entity entity) {
        List<Entity> orders = new ArrayList<Entity>();

        List<Entity> genealogyList = dataDefinitionService
                .get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY).find()
                .isEq("batch", entity.getField("batch")).list().getEntities();

        for (Entity genealogy : genealogyList) {
            Entity order = (Entity) genealogy.getField("order");
            if (!orders.contains(order)) {
                orders.add(order);
            }
        }
        return orders;
    }
}
