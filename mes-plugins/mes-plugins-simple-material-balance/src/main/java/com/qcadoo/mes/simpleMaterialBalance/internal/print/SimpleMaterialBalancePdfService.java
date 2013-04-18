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
import com.qcadoo.mes.simpleMaterialBalance.util.EntityLocationNumberComparator;
import com.qcadoo.mes.simpleMaterialBalance.util.EntityOrderNumberComparator;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public final class SimpleMaterialBalancePdfService extends PdfDocumentService {

    private static final String L_NUMBER = "number";

    private static final String L_NAME = "name";

    private static final String L_DATE = "date";

    private static final String L_WORKER = "worker";

    private static final String L_ORDER = "order";

    private static final String L_SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS = "simpleMaterialBalanceOrdersComponents";

    private static final String L_SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS = "simpleMaterialBalanceLocationsComponents";

    private static final String L_LOCATION = "location";

    private static final String L_UNIT = "unit";

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) simpleMaterialBalance.getField(L_DATE));
        addPanel(document, simpleMaterialBalance, locale);
        addBalance(document, simpleMaterialBalance, locale);
        addOrders(document, simpleMaterialBalance, locale);
        addLocations(document, simpleMaterialBalance, locale);
    }

    private void addPanel(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        pdfHelper.addTableCellAsOneColumnTable(
                panelTable,
                translationService.translate(
                        "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.date", locale),
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.DEFAULT, locale).format(
                        (Date) simpleMaterialBalance.getField(L_DATE)));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.name", locale),
                simpleMaterialBalance.getStringField(L_NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.worker", locale),
                simpleMaterialBalance.getStringField(L_WORKER));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.panel.simpleMaterialBalance.mrpAlgorithm", locale),
                translationService.translate("simpleMaterialBalance.simpleMaterialBalance.mrpAlgorithm.value."
                        + simpleMaterialBalance.getStringField("mrpAlgorithm"), locale));
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
    }

    private void addBalance(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.paragrah",
                locale), FontUtils.getDejavuBold11Dark()));

        List<String> simpleMaterialBalanceTableHeader = new ArrayList<String>();
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate("basic.product.unit.label", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.needed", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.inLocation", locale));
        simpleMaterialBalanceTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.balance", locale));

        PdfPTable table = pdfHelper.createTableWithHeader(6, simpleMaterialBalanceTableHeader, false);
        List<Entity> simpleMaterialBalanceOrdersComponents = simpleMaterialBalance
                .getHasManyField(L_SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS);
        MrpAlgorithm mrpAlgorithm = MrpAlgorithm.parseString(simpleMaterialBalance.getStringField("mrpAlgorithm"));

        Map<Entity, BigDecimal> products = productQuantitiesService.getNeededProductQuantitiesForComponents(
                simpleMaterialBalanceOrdersComponents, mrpAlgorithm);

        List<Entity> simpleMaterialBalanceLocationComponents = simpleMaterialBalance
                .getHasManyField(L_SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS);
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField(L_NUMBER).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField(L_NAME).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField(L_UNIT).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(numberService.format(entry.getValue()), FontUtils.getDejavuRegular9Dark()));
            BigDecimal available = BigDecimal.ZERO;
            for (Entity simpleMaterialBalanceLocationComponent : simpleMaterialBalanceLocationComponents) {
                available = available.add(materialFlowService.calculateShouldBeInLocation(simpleMaterialBalanceLocationComponent
                        .getBelongsToField(L_LOCATION).getId(), entry.getKey().getId(), (Date) simpleMaterialBalance
                        .getField(L_DATE)));
            }
            table.addCell(new Phrase(numberService.format(available), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(numberService.format(available.subtract(entry.getValue(), numberService.getMathContext())),
                    FontUtils.getDejavuBold9Dark()));
        }
        document.add(table);
    }

    private void addOrders(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.paragrah2",
                locale), FontUtils.getDejavuBold11Dark()));

        List<String> simpleMaterialBalanceOrdersTableHeader = new ArrayList<String>();
        simpleMaterialBalanceOrdersTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceOrdersTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));

        PdfPTable table = pdfHelper.createTableWithHeader(2, simpleMaterialBalanceOrdersTableHeader, false);
        List<Entity> orders = new ArrayList<Entity>(
                simpleMaterialBalance.getHasManyField(L_SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS));
        Collections.sort(orders, new EntityOrderNumberComparator());
        for (Entity e : orders) {
            table.addCell(new Phrase(e.getBelongsToField(L_ORDER).getStringField(L_NUMBER), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField(L_ORDER).getStringField(L_NAME), FontUtils.getDejavuRegular9Dark()));
        }
        document.add(table);
    }

    private void addLocations(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.paragrah3",
                locale), FontUtils.getDejavuBold11Dark()));

        List<String> simpleMaterialBalanceLocationTableHeader = new ArrayList<String>();
        simpleMaterialBalanceLocationTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceLocationTableHeader.add(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));

        PdfPTable table = pdfHelper.createTableWithHeader(2, simpleMaterialBalanceLocationTableHeader, false);
        List<Entity> simpleMaterialBalanceLocationComponents = new ArrayList<Entity>(
                simpleMaterialBalance.getHasManyField(L_SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS));
        Collections.sort(simpleMaterialBalanceLocationComponents, new EntityLocationNumberComparator());
        for (Entity simpleMaterialBalanceLocationComponent : simpleMaterialBalanceLocationComponents) {
            table.addCell(new Phrase(simpleMaterialBalanceLocationComponent.getBelongsToField(L_LOCATION)
                    .getStringField(L_NUMBER), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(simpleMaterialBalanceLocationComponent.getBelongsToField(L_LOCATION).getStringField(L_NAME),
                    FontUtils.getDejavuRegular9Dark()));
        }
        document.add(table);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
