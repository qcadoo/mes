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
package com.qcadoo.mes.simpleMaterialBalance.internal.print;

import java.math.BigDecimal;
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
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementReportDataServiceImpl;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.simpleMaterialBalance.util.EntityOrderNumberComparator;
import com.qcadoo.mes.simpleMaterialBalance.util.EntityStockAreasNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class SimpleMaterialBalancePdfService extends PdfDocumentService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialRequirementReportDataServiceImpl materialRequirementReportDataService;

    @Autowired
    private MaterialFlowService materialFlowService;

    @Override
    protected void buildPdfContent(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = getTranslationService().translate("simpleMaterialBalance.simpleMaterialBalance.report.title",
                locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) simpleMaterialBalance.getField("date"),
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
                getTranslationService().translate(
                        "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.date", locale),
                ((Date) simpleMaterialBalance.getField("date")).toString(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate(
                        "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.name", locale),
                simpleMaterialBalance.getStringField("name"), null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate(
                        "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.worker", locale),
                simpleMaterialBalance.getStringField("worker"), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate(
                        "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.onlyComponents", locale),
                (Boolean) simpleMaterialBalance.getField("onlyComponents") ? getTranslationService().translate("qcadooView.true",
                        locale) : getTranslationService().translate("qcadooView.false", locale), null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
    }

    private void addBalance(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.paragrah", locale), PdfUtil.getArialBold11Dark()));

        List<String> simpleMaterialBalanceTableHeader = new ArrayList<String>();
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.needed", locale));
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.inStoch", locale));
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.balance", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(6, simpleMaterialBalanceTableHeader, false);
        List<Entity> orders = simpleMaterialBalance.getHasManyField("orders");
        Boolean onlyComponents = (Boolean) simpleMaterialBalance.getField("onlyComponents");
        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForMaterialRequirementProducts(
                orders, onlyComponents);
        List<Entity> stockAreass = simpleMaterialBalance.getHasManyField("stockAreas");
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField("unit").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue()), PdfUtil.getArialRegular9Dark()));
            BigDecimal available = BigDecimal.ZERO;
            for (Entity stockAreas : stockAreass) {
                available = available.add(materialFlowService.calculateShouldBeInStockArea(
                        stockAreas.getBelongsToField("stockAreas").getId(), entry.getKey().getId().toString(),
                        simpleMaterialBalance.getField("date").toString()));
            }
            table.addCell(new Phrase(getDecimalFormat().format(available), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format(available.subtract(entry.getValue())), PdfUtil.getArialBold9Dark()));
        }
        document.add(table);
    }

    private void addOrders(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.paragrah2", locale), PdfUtil.getArialBold11Dark()));

        List<String> simpleMaterialBalanceOrdersTableHeader = new ArrayList<String>();
        simpleMaterialBalanceOrdersTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceOrdersTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(2, simpleMaterialBalanceOrdersTableHeader, false);
        List<Entity> orders = new ArrayList<Entity>(simpleMaterialBalance.getHasManyField("orders"));
        Collections.sort(orders, new EntityOrderNumberComparator());
        for (Entity e : orders) {
            table.addCell(new Phrase(e.getBelongsToField("order").getStringField("number"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("order").getStringField("name"), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    private void addstockAreass(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.paragrah3", locale), PdfUtil.getArialBold11Dark()));

        List<String> simpleMaterialBalancestockAreassTableHeader = new ArrayList<String>();
        simpleMaterialBalancestockAreassTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalancestockAreassTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(2, simpleMaterialBalancestockAreassTableHeader, false);
        List<Entity> stockAreas = new ArrayList<Entity>(simpleMaterialBalance.getHasManyField("stockAreas"));
        Collections.sort(stockAreas, new EntityStockAreasNumberComparator());
        for (Entity e : stockAreas) {
            table.addCell(new Phrase(e.getBelongsToField("stockAreas").getStringField("number"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("stockAreas").getStringField("name"), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
