package com.qcadoo.mes.ordersForSubproductsGeneration.print;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SizeFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrderFieldsOFSPG;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ColorUtils;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

@Component(value = "listOfProductionOrdersReportPdf")
public class ListOfProductionOrdersReportPdf extends ReportPdfView {

    private static final String L_TRANSLATION_PATH = "ordersForSubproductsGeneration.listOfProductionOrders.report.%s.label";

    private static final int ITEMS_IN_ORDERS_TABLE_LIMIT = 7;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private NumberService numberService;

    @Autowired
    private BarcodeOperationComponentService barcodeOperationComponentService;

    @Override
    protected Document newDocument() {
        Document doc = super.newDocument();
        doc.setPageSize(PageSize.A4.rotate());

        return doc;
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
                                final PdfWriter writer) throws DocumentException {

        pdfHelper.addDocumentHeaderThin(document, "", translationService.translate(
                "ordersForSubproductsGeneration.listOfProductionOrders.report.header", locale), "", new Date());

        List<Long> ids = Arrays.stream(model.get("id").toString().split(",")).map(Long::valueOf).collect(Collectors.toList());

        List<Entity> orders = getOrders(ids);
        List<Integer> columnWidths = Lists.newArrayList(12, 12, 12, 7, 7, 14, 12);

        List<String> header = Lists.newArrayList();

        Map<String, HeaderAlignment> headerValues = Maps.newLinkedHashMap();

        headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, "number"), locale),
                HeaderAlignment.LEFT);
        headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, "productionLine"), locale),
                HeaderAlignment.LEFT);
        headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, "company"), locale),
                HeaderAlignment.LEFT);
        headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, "size"), locale), HeaderAlignment.RIGHT);
        headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, "quantity"), locale),
                HeaderAlignment.RIGHT);
        headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, "finalProduct"), locale),
                HeaderAlignment.LEFT);
        headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, "code"), locale), HeaderAlignment.LEFT);

        header.addAll(headerValues.keySet());

        for (int i = 0; i < ITEMS_IN_ORDERS_TABLE_LIMIT; i++) {
            String translatedHeader = translationService.translate(String.format(L_TRANSLATION_PATH, "item"), locale);

            headerValues.put(translatedHeader + i, HeaderAlignment.LEFT);
            header.add(translatedHeader);

            columnWidths.add(9);
        }

        BigDecimal quantitySum = BigDecimal.ZERO;

        PdfPTable table = pdfHelper.createTableWithHeader(headerValues.size(), header, false, headerValues);

        table.setHeaderRows(1);
        table.setWidths(ArrayUtils.toPrimitive(columnWidths.toArray(new Integer[0])));
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.getDefaultCell().setBorderColor(ColorUtils.getLineDarkColor());
        table.getDefaultCell().setBorder(1);

        for (Entity order : sortOrders(orders)) {
            String productNumber = getRootOrderField(order, OrderFields.PRODUCT, ProductFields.NUMBER);
            String masterOrderNumber = getRootOrderField(order, OrderFieldsMO.MASTER_ORDER, MasterOrderFields.NUMBER);

            Entity company = order.getBelongsToField(OrderFields.COMPANY);
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);

            PdfPCell cell = createCell(masterOrderNumber, Element.ALIGN_LEFT, false);
            cell.setPaddingRight(3.5F);
            table.addCell(cell);

            cell = createCell("", Element.ALIGN_LEFT, false);
            cell.addElement(getProductionLineTable(order));
            table.addCell(cell);

            cell = createCell(company != null ? company.getStringField(CompanyFields.NUMBER) : "", Element.ALIGN_LEFT, false);
            cell.setPaddingRight(3.5F);
            table.addCell(cell);

            cell = createCell(Strings.nullToEmpty(getSizeNumber(product)), Element.ALIGN_RIGHT, true);
            cell.setPaddingRight(3.5F);
            table.addCell(cell);

            BigDecimal quantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
            quantitySum = quantitySum.add(quantity, numberService.getMathContext());

            cell = createCell(getQuantity(order), Element.ALIGN_RIGHT, true);
            cell.setPaddingRight(1.0F);
            cell.setPaddingLeft(0F);
            cell.setPaddingRight(3.5F);

            table.addCell(cell);

            cell = createCell(productNumber, Element.ALIGN_LEFT, false);
            cell.disableBorderSide(PdfPCell.RIGHT);
            table.addCell(cell);

            printOperationBarcode(table, writer, order);
            printItemsInOrdersTable(table);
        }

        document.add(table);

        addSummary(document, quantitySum, locale);

        return translationService.translate("ordersForSubproductsGeneration.listOfProductionOrders.report.fileName", locale);
    }

    private List<Entity> sortOrders(List<Entity> orders) {
        return orders.stream().sorted(Comparator.comparing((Entity order) -> getRootOrderField(order, OrderFieldsMO.MASTER_ORDER, MasterOrderFields.NUMBER), nullsLast(naturalOrder()))
                .thenComparing((Entity order) -> getRootOrderField(order, OrderFields.PRODUCT, ProductFields.NUMBER))).collect(Collectors.toList());
    }

    private PdfPTable getProductionLineTable(Entity order) {
        PdfPTable productionLineTable = new PdfPTable(1);

        Entity productionLineEntity = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        String productionLine = "";
        if (Objects.nonNull(productionLineEntity)) {
            productionLine = productionLineEntity.getStringField(ProductionLineFields.NUMBER);
        }
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPhrase(new Phrase(productionLine, FontUtils.getDejavuRegular7Dark()));
        productionLineTable.addCell(cell);
        Entity technologyGroupEntity = order.getBelongsToField(OrderFields.TECHNOLOGY).getBelongsToField(
                TechnologiesConstants.MODEL_TECHNOLOGY_GROUP);
        String technologyGroup = "";
        if (Objects.nonNull(technologyGroupEntity)) {
            technologyGroup = technologyGroupEntity.getStringField(TechnologyGroupFields.NUMBER);
        }
        cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPhrase(new Phrase(technologyGroup, FontUtils.getDejavuRegular7Dark()));
        productionLineTable.addCell(cell);
        return productionLineTable;
    }

    private String getSizeNumber(final Entity product) {
        String sizeNumber = null;

        if (Objects.nonNull(product)) {
            Entity size = product.getBelongsToField(ProductFields.SIZE);

            if (Objects.nonNull(size)) {
                sizeNumber = size.getStringField(SizeFields.NUMBER);
            }
        }

        return sizeNumber;
    }

    private String getQuantity(final Entity order) {
        BigDecimal quantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

        String quantityString;

        if (isIntegerValue(quantity)) {
            quantityString = numberService.format(quantity.longValue());
            quantityString = quantityString.substring(0, quantityString.length() - 3);
        } else {
            quantityString = numberService.format(quantity);
        }

        return quantityString;
    }

    private boolean isIntegerValue(final BigDecimal bd) {
        return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
    }

    private PdfPCell createCell(final String content, final int alignment, final boolean bold) {
        PdfPCell cell = new PdfPCell();

        if (bold) {
            cell.setPhrase(new Phrase(content, FontUtils.getDejavuBold7Dark()));
        } else {
            cell.setPhrase(new Phrase(content, FontUtils.getDejavuRegular7Dark()));
        }

        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(0.2f);
        cell.disableBorderSide(PdfPCell.TOP);
        cell.setPaddingBottom(5F);
        cell.setPaddingTop(5F);
        cell.setPaddingLeft(2.5F);
        cell.setPaddingRight(0.5F);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(ColorUtils.getLineDarkColor());

        return cell;
    }

    private void printOperationBarcode(final PdfPTable table, final PdfWriter writer, final Entity order) {
        PdfContentByte cb = writer.getDirectContent();
        Barcode128 code128 = new Barcode128();

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            DataDefinition operationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

            Entity operationComponent = operationComponentDD.find()
                    .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                    .setMaxResults(1).uniqueResult();

            if (Objects.nonNull(operationComponent)) {
                String code = getCodeFromBarcode(order, operationComponent);

                if (Objects.nonNull(code)) {
                    code128.setCode(code);

                    Image barcodeImage = code128.createImageWithBarcode(cb, null, null);
                    PdfPCell cell = new PdfPCell(barcodeImage, true);

                    cell.setBorderWidth(0.2f);
                    cell.disableBorderSide(PdfPCell.RIGHT);
                    cell.disableBorderSide(PdfPCell.LEFT);
                    cell.disableBorderSide(PdfPCell.TOP);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorderColor(ColorUtils.getLineDarkColor());
                    cell.setPaddingBottom(5F);
                    cell.setPaddingTop(5F);
                    cell.setPaddingLeft(0.5F);
                    cell.setPaddingRight(2.5F);

                    table.addCell(cell);

                    return;
                }
            }
        }

        table.addCell(createCell("", Element.ALIGN_LEFT, false));
    }

    private String getCodeFromBarcode(final Entity order, final Entity operationComponent) {
        Optional<String> code = barcodeOperationComponentService.findBarcode(order, operationComponent);

        if (!code.isPresent()) {
            barcodeOperationComponentService.createBarcodeOperationComponent(order, operationComponent);

            return barcodeOperationComponentService.getCodeFromBarcode(order, operationComponent);
        }

        return code.get();
    }

    private void printItemsInOrdersTable(final PdfPTable table) {
        for (int i = 0; i < ITEMS_IN_ORDERS_TABLE_LIMIT; i++) {
            PdfPTable itemsInOrdersTable = new PdfPTable(1);

            itemsInOrdersTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            itemsInOrdersTable.setSpacingAfter(0);
            itemsInOrdersTable.setSpacingBefore(0);

            PdfPCell cell = new PdfPCell();

            cell.setPhrase(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorderWidth(0.2f);
            cell.disableBorderSide(PdfPCell.RIGHT);
            cell.disableBorderSide(PdfPCell.LEFT);
            cell.disableBorderSide(PdfPCell.TOP);
            cell.disableBorderSide(PdfPCell.BOTTOM);

            cell.setPaddingBottom(5);
            cell.setPaddingTop(5);
            cell.setPaddingLeft(0);
            cell.setPaddingRight(0);
            cell.setUseBorderPadding(false);

            itemsInOrdersTable.addCell(cell);

            cell = new PdfPCell();

            cell.setPhrase(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorderWidth(0.2f);
            cell.disableBorderSide(PdfPCell.RIGHT);
            cell.disableBorderSide(PdfPCell.LEFT);
            cell.disableBorderSide(PdfPCell.BOTTOM);

            cell.setPaddingBottom(5);
            cell.setPaddingTop(5);
            cell.setPaddingLeft(0);
            cell.setPaddingRight(0);
            cell.setUseBorderPadding(false);

            itemsInOrdersTable.addCell(cell);

            table.getDefaultCell().enableBorderSide(PdfPCell.RIGHT);
            table.getDefaultCell().enableBorderSide(PdfPCell.LEFT);
            table.getDefaultCell().enableBorderSide(PdfPCell.BOTTOM);
            table.getDefaultCell().setBorderWidth(0.2f);
            table.getDefaultCell().setBorderColor(ColorUtils.getLineDarkColor());
            table.addCell(itemsInOrdersTable);
        }
    }

    private void addSummary(final Document document, final BigDecimal quantitySum, final Locale locale)
            throws DocumentException {
        String sumString = numberService.formatWithMinimumFractionDigits(quantitySum, 0);

        Paragraph sumLabel = new Paragraph(new Phrase(translationService.translate(String.format(L_TRANSLATION_PATH, "sum"),
                locale, sumString), FontUtils.getDejavuBold11Dark()));

        PdfPTable sumTable = pdfHelper.createPanelTable(1);

        sumTable.setTableEvent(null);
        sumTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        sumTable.getDefaultCell().disableBorderSide(Rectangle.BOX);
        sumTable.addCell(sumLabel);

        document.add(sumLabel);
    }

    private String getRootOrderField(final Entity order, final String belongsToField, final String field) {
        Entity rootOrder = order.getBelongsToField(OrderFieldsOFSPG.ROOT);

        Entity belongsTo;
        if (Objects.nonNull(rootOrder)) {
            belongsTo = rootOrder.getBelongsToField(belongsToField);
        } else {
            belongsTo = order.getBelongsToField(belongsToField);
        }

        if (belongsTo != null) {
            return belongsTo.getStringField(field);
        }
        return null;
    }

    private List<Entity> getOrders(final List<Long> ids) {
        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);

        return orderDD.find().add(SearchRestrictions.in("id", ids)).list().getEntities();
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("ordersForSubproductsGeneration.listOfProductionOrders.report.header", locale));
    }

}
