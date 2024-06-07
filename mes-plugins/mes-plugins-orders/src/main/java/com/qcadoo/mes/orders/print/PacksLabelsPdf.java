package com.qcadoo.mes.orders.print;

import com.google.common.base.Strings;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AssortmentFields;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.ModelFields;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SizeFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.google.common.base.Preconditions.checkState;

@Component(value = "packsLabelsPdf")
public class PacksLabelsPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("ids") != null, "Unable to generate report - missing ids");

        List<Long> ids = (List<Long>) model.get("ids");

        PdfPTable table = pdfHelper.createPanelTable(2);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.setTableEvent(null);

        List<Entity> packs = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK)
                .find().add(SearchRestrictions.in("id", ids)).list().getEntities();

        boolean showRightBorder = true;
        boolean showBottomBorder = true;
        int totalPrinted = 1;

        for (Entity pack : packs) {

            int labelNo = 1;

            addLabel(writer, pack, table, labelNo, showRightBorder, showBottomBorder, locale);
            totalPrinted++;
            showBottomBorder = totalPrinted % 8 != 0 && totalPrinted % 8 != 7;
            showRightBorder = !showRightBorder;
            labelNo++;

        }

        if (totalPrinted > 1) {
            table.completeRow();
            document.add(table);
        } else {
            document.add(Chunk.NEXTPAGE);
        }
        return translationService.translate("orders.packsLabels.report.fileName", locale, DateUtils.toDateTimeString(new Date()));
    }

    private void addLabel(PdfWriter writer, Entity pack, final PdfPTable table, final int orderNo, final boolean showRightBorder,
            final boolean showBottomBorder, final Locale locale) {
        PdfPCell cell = new PdfPCell();
        int border;
        if (showRightBorder) {
            border = Rectangle.RIGHT | Rectangle.BOTTOM;
        } else {
            border = Rectangle.BOTTOM;
        }
        cell.setBorder(border);

        PdfPTable mainTable = pdfHelper.createPanelTable(1);
        mainTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        mainTable.setTableEvent(null);

        addLabelFields(writer, pack, mainTable, orderNo, locale);
        cell.addElement(mainTable);
        table.addCell(cell);
    }

    private void addLabelFields(PdfWriter writer, Entity pack, final PdfPTable mainTable, final int orderNo, final Locale locale) {
        PdfPTable orderAndQrTable;
        PdfContentByte cb = writer.getDirectContent();

        orderAndQrTable = pdfHelper.createPanelTable(1);

        orderAndQrTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        orderAndQrTable.setTableEvent(null);

        PdfPTable innerTable = pdfHelper.createPanelTable(1);
        innerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        innerTable.setTableEvent(null);

        PdfPTable brTable = pdfHelper.createPanelTable(8);
        brTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        brTable.setTableEvent(null);

        Barcode128 code128 = new Barcode128();
        code128.setCode(pack.getStringField(OrderPackFields.NUMBER));
        code128.setBarHeight(32f);
        //barcode width
        code128.setX(1.8f);

        //font size
        code128.setSize(10f);
        code128.setN(0.25f);
        code128.setBaseline(11f);

        Image barcodeImage = code128.createImageWithBarcode(cb, null, null);

        PdfPCell barcodeCell = new PdfPCell(barcodeImage);
        barcodeCell.setColspan(4);
        barcodeCell.setBorder(Rectangle.NO_BORDER);
        barcodeCell.setFixedHeight(46f);

        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setColspan(2);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setFixedHeight(46f);

        brTable.addCell(emptyCell);
        brTable.addCell(barcodeCell);
        brTable.addCell(emptyCell);

        innerTable.addCell(brTable);
        int[] columnWidths = new int[] { 30, 70 };

        pdfHelper.addTableCellAsTwoColumnsTable(innerTable,
                translationService.translate("orders.packsLabels.report.product", locale), extractProduct(pack), columnWidths);

        appendSize(innerTable, pack, locale);

        appendAttribute(innerTable, pack);

        pdfHelper.addTableCellAsTwoColumnsTable(innerTable,
                translationService.translate("orders.packsLabels.report.quantity", locale), extractQuantity(pack), columnWidths);

        pdfHelper.addTableCellAsTwoColumnsTable(innerTable,
                translationService.translate("orders.packsLabels.report.order", locale), extractOrder(pack), columnWidths);

        pdfHelper
                .addTableCellAsTwoColumnsTable(innerTable,
                        translationService.translate("orders.packsLabels.report.startDate", locale), extractStartDate(pack),
                        columnWidths);

        pdfHelper.addTableCellAsTwoColumnsTable(innerTable,
                translationService.translate("orders.packsLabels.report.finishDate", locale), extractFinishDate(pack),
                columnWidths);

        orderAndQrTable.addCell(innerTable);

        mainTable.addCell(orderAndQrTable);
    }

    private void appendSize(PdfPTable innerTable, Entity pack, Locale locale) {
        int[] columnWidths = new int[] { 30, 70 };
        Entity product = pack.getBelongsToField(OrderPackFields.ORDER).getBelongsToField(OrderFields.PRODUCT);
        Entity size = product.getBelongsToField(ProductFields.SIZE);
        if (Objects.nonNull(size)) {
            pdfHelper.addTableCellAsTwoColumnsTable(innerTable,
                    translationService.translate("orders.packsLabels.report.size", locale),
                    size.getStringField(SizeFields.NUMBER), columnWidths);
        } else {
            pdfHelper.addTableCellAsTwoColumnsTable(innerTable, "", "", columnWidths);
        }

    }

    private void appendAttribute(PdfPTable innerTable, Entity pack) {
        int[] columnWidths = new int[] { 30, 70 };

        Entity parameter = parameterService.getParameter();
        Entity attribute = parameter.getBelongsToField("attributeOnTheLabel");
        if (Objects.isNull(attribute)) {
            pdfHelper.addTableCellAsTwoColumnsTable(innerTable, "", "", columnWidths);
        } else {
            Entity product = pack.getBelongsToField(OrderPackFields.ORDER).getBelongsToField(OrderFields.PRODUCT);
            List<Entity> attrs = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);
            String value = attrs.stream()
                    .filter(v -> v.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId()))
                    .map(a -> a.getStringField(ProductAttributeValueFields.VALUE)).collect(Collectors.joining(", "));
            if (!Strings.isNullOrEmpty(value)) {
                pdfHelper.addTableCellAsTwoColumnsTable(innerTable, attribute.getStringField(AttributeFields.NAME), value,
                        columnWidths);
            } else {
                pdfHelper.addTableCellAsTwoColumnsTable(innerTable, attribute.getStringField(AttributeFields.NAME), "",
                        columnWidths);
            }
        }

    }

    private String extractOrder(Entity pack) {
        return pack.getBelongsToField(OrderPackFields.ORDER).getStringField(OrderFields.NUMBER);
    }

    private String extractQuantity(Entity pack) {
        Entity productEntity = pack.getBelongsToField(OrderPackFields.ORDER).getBelongsToField(OrderFields.PRODUCT);

        return BigDecimalUtils.toString(pack.getDecimalField(OrderPackFields.QUANTITY), 2) + " "
                + productEntity.getStringField(ProductFields.UNIT);
    }

    private String extractStartDate(Entity pack) {
        return DateUtils.toDateTimeString(pack.getBelongsToField(OrderPackFields.ORDER).getDateField(OrderFields.START_DATE));
    }

    private String extractFinishDate(Entity pack) {
        return DateUtils.toDateTimeString(pack.getBelongsToField(OrderPackFields.ORDER).getDateField(OrderFields.FINISH_DATE));
    }

    private String extractProduct(Entity pack) {
        Entity productEntity = pack.getBelongsToField(OrderPackFields.ORDER).getBelongsToField(OrderFields.PRODUCT);
        StringBuilder product = new StringBuilder();
        product.append(productEntity.getStringField(ProductFields.NUMBER));
        product.append("\n");
        product.append(productEntity.getStringField(ProductFields.NAME));
        product.append("\n");
        Entity assortment = productEntity.getBelongsToField(ProductFields.ASSORTMENT);
        if (Objects.nonNull(assortment)) {
            product.append(assortment.getStringField(AssortmentFields.NAME)).append(", ");
        }
        Entity model = productEntity.getBelongsToField(ProductFields.MODEL);
        if (Objects.nonNull(model)) {
            product.append(model.getStringField(ModelFields.NAME));
        }

        return product.toString();
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("orders.packsLabels.report.title", locale));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

}
