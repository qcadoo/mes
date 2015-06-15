package com.qcadoo.mes.warehouseMinimalState.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.print.helper.DocumentPdfHelper;
import com.qcadoo.mes.warehouseMinimalState.constants.WarehouseMinimalStateConstants;
import com.qcadoo.mes.warehouseMinimalState.constants.WarehouseMinimumStateFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "warehouseMinimalStateReportPdf")
public class DocumentPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private DocumentPdfHelper documentPdfHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    private Set<Entity> warehouses;

    private Multimap<Long, Entity> warehouseToMinimumStateMap;

    private Locale locale;

    @Override
    protected String addContent(Document document, Map<String, Object> model, Locale locale, PdfWriter writer)
            throws DocumentException, IOException {
        this.locale = locale;
        String documentTitle = translationService.translate("warehouseMinimalState.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        DataDefinition warehouseMinimumStateDD = dataDefinitionService.get(WarehouseMinimalStateConstants.PLUGIN_IDENTIFIER,
                WarehouseMinimalStateConstants.MODEL_WAREHOUSE_MINIMUM_STATE);
        SearchResult searchResult = warehouseMinimumStateDD.find().add(SearchRestrictions.gt("minimumState", BigDecimal.ZERO))
                .list();
        List<Entity> warehouseMinimumStates = searchResult.getEntities();
        fillWarehousesLists(warehouseMinimumStates);
        addWarehouseTables(document);

        return "raport";
    }

    @Override
    protected void addTitle(Document document, Locale locale) {
        document.addTitle(translationService.translate("warehouseMinimalState.report.title", locale));
    }

    private void fillWarehousesLists(List<Entity> warehouseMinimumStates) {
        warehouses = new HashSet<Entity>();
        warehouseToMinimumStateMap = ArrayListMultimap.create();
        for (Entity warehouseMinimumState : warehouseMinimumStates) {
            Entity warehouse = warehouseMinimumState.getBelongsToField(WarehouseMinimumStateFields.LOCATION);
            warehouses.add(warehouse);
            warehouseToMinimumStateMap.put(warehouse.getId(), warehouseMinimumState);
        }
    }

    private void addWarehouseTables(Document document) throws DocumentException {
        List<Entity> warehousesList = warehouses
                .stream()
                .sorted((w1, w2) -> w1.getStringField(LocationFields.NAME).compareToIgnoreCase(
                        w2.getStringField(LocationFields.NAME))).collect(Collectors.toList());
        for (Entity warehouse : warehousesList) {
            addWarehouseTable(document, warehouse);
        }
    }

    private void addWarehouseTable(Document document, Entity warehouse) throws DocumentException {
        String subtitle = translationService.translate("warehouseMinimalState.report.subtitle", locale,
                warehouse.getStringField(LocationFields.NUMBER), warehouse.getStringField(LocationFields.NAME));

        PdfPTable warehouseTable = createTable();

        List<Entity> stocks = getWarehouseStockWithTooSmallMinState(warehouse);
        Map<Long, Entity> stocksByProduct = stocks.stream().collect(
                Collectors.toMap(res -> res.getBelongsToField("product").getId(), (res) -> res));
        Collection<Entity> minimumStates = warehouseToMinimumStateMap.get(warehouse.getId());
        minimumStates = minimumStates
                .stream()
                .sorted(
                (ms1, ms2) -> ms1.getBelongsToField("product").getStringField(ProductFields.NUMBER)
                        .compareToIgnoreCase(ms2.getBelongsToField("product").getStringField(ProductFields.NUMBER)))
                .collect(Collectors.toList());
        boolean rowsWereAdded = false;
        for (Entity minimumState : minimumStates) {
            rowsWereAdded |= addRow(minimumState, stocksByProduct, warehouseTable);
        }
        if (rowsWereAdded) {
            document.add(new Paragraph(subtitle));
            document.add(warehouseTable);
        }
    }

    private PdfPTable createTable() {
        int[] headerWidths = { 40, 70, 40, 40, 40, 40, 60 };
        Map<String, HeaderAlignment> headerValues = getHeaderValues(locale);
        PdfPTable warehouseTable = pdfHelper.createTableWithHeader(7, Lists.newArrayList(headerValues.keySet()), false,
                headerWidths, headerValues);
        warehouseTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        warehouseTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        warehouseTable.setHeaderRows(1);
        warehouseTable.setSpacingAfter(12.0f);
        return warehouseTable;
    }

    private boolean addRow(Entity minimalState, Map<Long, Entity> stocksByProduct, PdfPTable table) {
        boolean rowAdded = false;
        Entity product = minimalState.getBelongsToField("product");
        Entity warehouse = minimalState.getBelongsToField("product");
        Entity stock = stocksByProduct.get(product.getId());
        if (stock == null) {
            BigDecimal ordered = getOrderedQuantityForProductAndLocation(warehouse.getId(), product.getId());
            if (ordered.compareTo(minimalState.getDecimalField("minimumState")) == -1
                    || ordered.compareTo(minimalState.getDecimalField("minimumState")) == 0) {
                addCells(table, minimalState, stock, ordered);
                rowAdded = true;
            }
        } else {
            BigDecimal statePlusOrder = BigDecimalUtils.convertNullToZero(stock.getDecimalField("orderedQuantity")).add(
                    BigDecimalUtils.convertNullToZero(stock.getDecimalField("quantity")), numberService.getMathContext());
            if (statePlusOrder.compareTo(stock.getDecimalField("minimumState")) == -1
                    || statePlusOrder.compareTo(stock.getDecimalField("minimumState")) == 0) {
                addCells(table, minimalState, stock, null);
                rowAdded = true;
            }
        }
        return rowAdded;
    }

    private void addCells(PdfPTable table, Entity warehouseMinimumState, Entity stock, BigDecimal ordered) {
        Entity product = warehouseMinimumState.getBelongsToField(WarehouseMinimumStateFields.PRODUCT);
        addSmallCell(table, product.getStringField(ProductFields.NUMBER));
        addSmallCell(table, product.getStringField(ProductFields.NAME));
        addSmallCell(table, warehouseMinimumState.getDecimalField(WarehouseMinimumStateFields.MINIMUM_STATE));
        if (stock != null) {
            addSmallCell(table, stock.getDecimalField("quantity"));
            addSmallCell(table, stock.getDecimalField("orderedQuantity"));
        } else {
            addSmallCell(table, BigDecimal.ZERO);
            addSmallCell(table, ordered);
        }
        addSmallCell(table, warehouseMinimumState.getDecimalField(WarehouseMinimumStateFields.OPTIMAL_ORDER_QUANTITY));
        Entity supplier = getDefaultSupplier(product.getId());
        if (supplier != null) {
            Entity company = supplier.getBelongsToField(CompanyProductFields.COMPANY);
            addSmallCell(table, company.getStringField(CompanyFields.NAME));
        } else {
            table.completeRow();
        }
    }

    private void addSmallCell(PdfPTable table, String content) {
        table.addCell(new Phrase(content, FontUtils.getDejavuRegular7Dark()));
    }

    private void addSmallCell(PdfPTable table, BigDecimal content) {
        PdfPCell cell = new PdfPCell(table.getDefaultCell());
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        String value = numberService.formatWithMinimumFractionDigits(content, 0);
        cell.setPhrase(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
        table.addCell(cell);
    }

    private Map<String, HeaderAlignment> getHeaderValues(Locale locale) {
        Map<String, HeaderAlignment> headerLabels = Maps.newLinkedHashMap();

        headerLabels.put(translationService.translate("warehouseMinimalState.report.columnHeader.productNumber", locale),
                HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate("warehouseMinimalState.report.columnHeader.productName", locale),
                HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate("warehouseMinimalState.report.columnHeader.minimumState", locale),
                HeaderAlignment.RIGHT);
        headerLabels.put(translationService.translate("warehouseMinimalState.report.columnHeader.warehouseState", locale),
                HeaderAlignment.RIGHT);
        headerLabels.put(translationService.translate("warehouseMinimalState.report.columnHeader.orderQuantity", locale),
                HeaderAlignment.RIGHT);
        headerLabels.put(translationService.translate("warehouseMinimalState.report.columnHeader.optimalOrderQuantity", locale),
                HeaderAlignment.RIGHT);
        headerLabels.put(translationService.translate("warehouseMinimalState.report.columnHeader.contractor", locale),
                HeaderAlignment.LEFT);
        return headerLabels;
    }

    public BigDecimal getOrderedQuantityForProductAndLocation(final Long warehouse, final Long product) {
        String query = "select COALESCE(sum(op.orderedQuantity),0) as  orderedQuantity from #deliveries_orderedProduct op, "
                + "#deliveries_delivery del where op.delivery.id=del.id and op.product.id=:product and del.location.id = :warehouseId";
        return getWarehouseStockDD().find(query).setParameter("warehouseId", warehouse).setParameter("product", product)
                .setMaxResults(1).uniqueResult().getDecimalField("orderedQuantity");
    }

    public List<Entity> getWarehouseStockWithTooSmallMinState(final Entity warehouse) {
        String query = "select stock from #materialFlowResources_warehouseStock as stock where stock.minimumState > 0"
                + " and stock.location.id = :warehouseId";
        return getWarehouseStockDD().find(query).setParameter("warehouseId", warehouse.getId()).list().getEntities();
    }

    private DataDefinition getWarehouseStockDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "warehouseStock");
    }

    private Entity getDefaultSupplier(Long productId) {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);

        if (product != null) {
            if (ProductFamilyElementType.PARTICULAR_PRODUCT.equals(product.getStringField(ProductFields.ENTITY_TYPE))) {
                return getDefaultSupplierForParticularProduct(productId);
            } else {
                return getDefaultSupplierForProductsFamily(productId);
            }
        }
        return null;
    }

    private Entity getDefaultSupplierForProductsFamily(Long productId) {
        String query = "select company from #deliveries_companyProductsFamily company, #basic_product product where product.parent.id = company.product.id and product.id = :id"
                + " and company.isDefault = true";
        return deliveriesService.getCompanyProductDD().find(query).setParameter("id", productId).setMaxResults(1).uniqueResult();
    }

    private Entity getDefaultSupplierForParticularProduct(Long productId) {
        String query = "select company from #deliveries_companyProduct company where company.product.id = :id"
                + " and company.isDefault = true";
        return deliveriesService.getCompanyProductDD().find(query).setParameter("id", productId).setMaxResults(1).uniqueResult();
    }
}
