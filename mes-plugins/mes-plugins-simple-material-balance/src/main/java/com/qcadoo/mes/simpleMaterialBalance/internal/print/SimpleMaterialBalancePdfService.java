/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.simpleMaterialBalance.internal.print;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.simpleMaterialBalance.util.EntityOrderNumberComparator;
import com.qcadoo.mes.simpleMaterialBalance.util.EntityStockAreasNumberComparator;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class SimpleMaterialBalancePdfService extends PdfDocumentService {

    private static final String WORKER_FIELD = "worker";

    private static final String DATE_FIELD = "date";

    private static final String ONLY_COMPONENTS_FIELD = "onlyComponents";

    private static final String UNIT_FIELD = "unit";

    private static final String ORDERS_FIELD = "orders";

    private static final String NUMBER_FIELD = "number";

    private static final String ORDER_FIELD = "order";

    private static final String STOCK_AREAS_FIELD = "stockAreas";

    private static final String NAME_FIELD = "name";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Override
    protected void buildPdfContent(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) simpleMaterialBalance.getField(DATE_FIELD),
                securityService.getCurrentUserName());
        addPanel(document, simpleMaterialBalance, locale);
        addBalance(document, simpleMaterialBalance, locale);
        addOrders(document, simpleMaterialBalance, locale);
        addstockAreass(document, simpleMaterialBalance, locale);
    }

    private void addPanel(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        PdfUtil.addTableCellAsTable(
                panelTable,
                translationService.translate(
                        "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.date", locale),
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.DEFAULT, locale).format(
                        (Date) simpleMaterialBalance.getField(DATE_FIELD)), null, PdfUtil.getArialBold10Dark(), PdfUtil
                        .getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable, translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.name", locale),
                simpleMaterialBalance.getStringField(NAME_FIELD), null, PdfUtil.getArialBold10Dark(), PdfUtil
                        .getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable, translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.worker", locale),
                simpleMaterialBalance.getStringField(WORKER_FIELD), null, PdfUtil.getArialBold10Dark(), PdfUtil
                        .getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                translationService.translate(
                        "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.onlyComponents", locale),
                (Boolean) simpleMaterialBalance.getField(ONLY_COMPONENTS_FIELD) ? translationService.translate("qcadooView.true",
                        locale) : translationService.translate("qcadooView.false", locale), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
    }

    private void addBalance(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.paragrah",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> simpleMaterialBalanceTableHeader = new ArrayList<String>();
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate("basic.product.unit.label", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.needed", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.inStoch", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.balance", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(6, simpleMaterialBalanceTableHeader, false);
        List<Entity> orders = simpleMaterialBalance.getHasManyField(ORDERS_FIELD);
        Boolean onlyComponents = (Boolean) simpleMaterialBalance.getField(ONLY_COMPONENTS_FIELD);

        Map<Entity, BigDecimal> products = productQuantitiesService.getNeededProductQuantitiesForComponents(orders,
                onlyComponents);

        List<Entity> stockAreass = simpleMaterialBalance.getHasManyField(STOCK_AREAS_FIELD);
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField(NUMBER_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField(NAME_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField(UNIT_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(numberService.format(entry.getValue()), PdfUtil.getArialRegular9Dark()));
            BigDecimal available = BigDecimal.ZERO;
            for (Entity stockAreas : stockAreass) {
                available = available.add(materialFlowService.calculateShouldBeInStockArea(
                        stockAreas.getBelongsToField(STOCK_AREAS_FIELD).getId(), entry.getKey().getId().toString(),
                        (Date) simpleMaterialBalance.getField(DATE_FIELD)));
            }
            table.addCell(new Phrase(numberService.format(available), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(numberService.format(available.subtract(entry.getValue(), numberService.getMathContext())),
                    PdfUtil.getArialBold9Dark()));
        }
        document.add(table);
    }

    private void addOrders(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.paragrah2",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> simpleMaterialBalanceOrdersTableHeader = new ArrayList<String>();
        simpleMaterialBalanceOrdersTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceOrdersTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(2, simpleMaterialBalanceOrdersTableHeader, false);
        List<Entity> orders = new ArrayList<Entity>(simpleMaterialBalance.getHasManyField(ORDERS_FIELD));
        Collections.sort(orders, new EntityOrderNumberComparator());
        for (Entity e : orders) {
            table.addCell(new Phrase(e.getBelongsToField(ORDER_FIELD).getStringField(NUMBER_FIELD), PdfUtil
                    .getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField(ORDER_FIELD).getStringField(NAME_FIELD), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    private void addstockAreass(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.paragrah3",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> simpleMaterialBalancestockAreassTableHeader = new ArrayList<String>();
        simpleMaterialBalancestockAreassTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalancestockAreassTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(2, simpleMaterialBalancestockAreassTableHeader, false);
        List<Entity> stockAreas = new ArrayList<Entity>(simpleMaterialBalance.getHasManyField(STOCK_AREAS_FIELD));
        Collections.sort(stockAreas, new EntityStockAreasNumberComparator());
        for (Entity e : stockAreas) {
            table.addCell(new Phrase(e.getBelongsToField(STOCK_AREAS_FIELD).getStringField(NUMBER_FIELD), PdfUtil
                    .getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField(STOCK_AREAS_FIELD).getStringField(NAME_FIELD), PdfUtil
                    .getArialRegular9Dark()));
        }
        document.add(table);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
