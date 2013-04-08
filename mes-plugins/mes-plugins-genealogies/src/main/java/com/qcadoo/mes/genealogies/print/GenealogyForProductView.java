/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.genealogies.print.util.BatchOrderNrComparator;
import com.qcadoo.mes.genealogies.print.util.EntityNumberComparator;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.Pair;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "genealogyForProductView")
public class GenealogyForProductView extends ReportPdfView {

    private static final String GENEALOGIES_FOR_COMPONENTS_PLUGIN = "genealogiesForComponents";

    private static final String VALUE = "value";

    private static final String GENEALOGIES_FIELD = "genealogies";

    private static final String PRODUCT_IN_COMPONENT_FIELD = "productInComponent";

    private static final String PRODUCT_IN_COMPONENTS_FIELD = "productInComponents";

    private static final String DATE_FROM_FIELD = "dateFrom";

    private static final String BATCH_FIELD = "batch";

    private static final String NAME_FIELD = "name";

    private static final String NUMBER_FIELD = "number";

    private static final String PRODUCT_FIELD = "product";

    private static final String ORDER_FIELD = "order";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        Entity entity = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY)
                .get(Long.valueOf(model.get(VALUE).toString()));
        String documentTitle = translationService.translate("genealogies.genealogyForProduct.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());
        addTables(document, entity, locale);
        return translationService.translate("genealogies.genealogyForProduct.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("genealogies.genealogyForProduct.report.title", locale));
    }

    private void addTables(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("orders.order.number.label", locale));
        orderHeader.add(translationService.translate("orders.order.name.label", locale));
        orderHeader.add(translationService.translate("orders.order.dateFrom.label", locale));
        Paragraph productTitle = new Paragraph(new Phrase(translationService.translate(
                "genealogies.genealogyForProduct.report.paragrah.product", locale), FontUtils.getDejavuBold11Light()));
        productTitle.setSpacingBefore(20);
        document.add(productTitle);
        PdfPTable headerData = pdfHelper.createPanelTable(3);
        headerData.setSpacingBefore(7);
        Entity product = entity.getBelongsToField(ORDER_FIELD).getBelongsToField(PRODUCT_FIELD);
        pdfHelper.addTableCellAsOneColumnTable(headerData, translationService.translate("basic.product.number.label", locale),
                product.getField(NUMBER_FIELD));
        pdfHelper.addTableCellAsOneColumnTable(headerData, translationService.translate("basic.product.name.label", locale),
                product.getField(NAME_FIELD));
        pdfHelper.addTableCellAsOneColumnTable(headerData,
                translationService.translate("genealogies.genealogy.batch.label", locale), entity.getField(BATCH_FIELD));
        document.add(headerData);
        Paragraph orderTitle = new Paragraph(new Phrase(translationService.translate(
                "genealogies.genealogyForProduct.report.paragrah.order", locale), FontUtils.getDejavuBold11Light()));
        orderTitle.setSpacingBefore(20);
        document.add(orderTitle);
        List<Entity> orders = getOrders(entity);
        Collections.sort(orders, new EntityNumberComparator());
        addOrderSeries(document, orders, orderHeader);
        if (pluginManager.isPluginEnabled(GENEALOGIES_FOR_COMPONENTS_PLUGIN)) {
            addComponentSeries(document, orders, locale);
        }
    }

    private void addComponentSeries(final Document document, final List<Entity> orders, final Locale locale)
            throws DocumentException {
        for (Entity order : orders) {
            document.add(Chunk.NEWLINE);
            Paragraph title = new Paragraph(new Phrase(translationService.translate(
                    "genealogies.genealogyForProduct.report.paragrah", locale), FontUtils.getDejavuBold11Light()));
            title.add(new Phrase(" " + order.getField(NUMBER_FIELD).toString(), FontUtils.getDejavuBold19Dark()));
            document.add(title);
            List<String> componentHeader = new ArrayList<String>();
            componentHeader.add(translationService.translate("basic.product.number.label", locale));
            componentHeader.add(translationService.translate("basic.product.name.label", locale));
            componentHeader.add(translationService.translate("genealogiesForComponents.productInBatch.batch.label", locale));
            PdfPTable table = pdfHelper.createTableWithHeader(3, componentHeader, false);

            List<Pair<String, Entity>> batchList = getBatchList(order);

            Collections.sort(batchList, new BatchOrderNrComparator());

            for (Pair<String, Entity> pair : batchList) {
                String batch = pair.getKey();
                Entity product = pair.getValue();
                table.addCell(new Phrase(product.getField(NUMBER_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
                table.addCell(new Phrase(product.getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
                table.addCell(new Phrase(batch, FontUtils.getDejavuRegular9Dark()));
            }
            document.add(table);

        }
    }

    private void addOrderSeries(final Document document, final List<Entity> orders, final List<String> orderHeader)
            throws DocumentException {
        PdfPTable table = pdfHelper.createTableWithHeader(3, orderHeader, false);
        for (Entity order : orders) {
            table.addCell(new Phrase(order.getField(NUMBER_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(order.getField(NAME_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(order.getField(DATE_FROM_FIELD).toString(), FontUtils.getDejavuRegular9Dark()));
        }
        document.add(table);
    }

    private List<Pair<String, Entity>> getBatchList(final Entity entity) {
        List<Pair<String, Entity>> batchList = new ArrayList<Pair<String, Entity>>();
        for (Entity genealogy : entity.getHasManyField(GENEALOGIES_FIELD)) {
            for (Entity productInComponent : genealogy.getHasManyField(PRODUCT_IN_COMPONENTS_FIELD)) {
                Entity product = (Entity) ((Entity) productInComponent.getField(PRODUCT_IN_COMPONENT_FIELD))
                        .getField(PRODUCT_FIELD);
                for (Entity batch : productInComponent.getHasManyField(BATCH_FIELD)) {
                    Pair<String, Entity> pair = Pair.of(batch.getField(BATCH_FIELD).toString(), product);
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
                .add(SearchRestrictions.eq(BATCH_FIELD, entity.getField(BATCH_FIELD))).list().getEntities();

        for (Entity genealogy : genealogyList) {
            Entity order = (Entity) genealogy.getField(ORDER_FIELD);
            if (!orders.contains(order)) {
                orders.add(order);
            }
        }
        return orders;
    }
}
