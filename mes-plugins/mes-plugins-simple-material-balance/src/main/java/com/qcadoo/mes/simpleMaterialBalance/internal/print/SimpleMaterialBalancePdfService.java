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
import com.qcadoo.mes.inventory.InventoryService;
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementReportDataServiceImpl;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.simpleMaterialBalance.util.EntityOrderNumberComparator;
import com.qcadoo.mes.simpleMaterialBalance.util.EntityWarehouseNumberComparator;
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
    private InventoryService inventoryService;

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
        addWarehouses(document, simpleMaterialBalance, locale);
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
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.needed", locale));
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.inStoch", locale));
        simpleMaterialBalanceTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.balance", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(5, simpleMaterialBalanceTableHeader, false);
        List<Entity> orders = simpleMaterialBalance.getHasManyField("orders");
        Boolean onlyComponents = (Boolean) simpleMaterialBalance.getField("onlyComponents");
        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForMaterialRequirementProducts(
                orders, onlyComponents);
        List<Entity> warehouses = simpleMaterialBalance.getHasManyField("warehouses");
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue()), PdfUtil.getArialRegular9Dark()));
            // TODO sth wrong, wtf
            BigDecimal available = BigDecimal.ZERO;
            for (Entity warehouse : warehouses) {
                available.add(inventoryService.calculateShouldBe(warehouse.getBelongsToField("warehouse")
                        .getStringField("number"), entry.getKey().getStringField("number"), simpleMaterialBalance
                        .getField("date").toString()));
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

    private void addWarehouses(final Document document, final Entity simpleMaterialBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.paragrah3", locale), PdfUtil.getArialBold11Dark()));

        List<String> simpleMaterialBalanceWarehousesTableHeader = new ArrayList<String>();
        simpleMaterialBalanceWarehousesTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        simpleMaterialBalanceWarehousesTableHeader.add(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(2, simpleMaterialBalanceWarehousesTableHeader, false);
        List<Entity> warehouses = new ArrayList<Entity>(simpleMaterialBalance.getHasManyField("warehouses"));
        Collections.sort(warehouses, new EntityWarehouseNumberComparator());
        for (Entity e : warehouses) {
            table.addCell(new Phrase(e.getBelongsToField("warehouse").getStringField("number"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("warehouse").getStringField("name"), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    @Override
    protected String getSuffix() {
        return "";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
