package com.qcadoo.mes.deliveries.print;

import static com.google.common.base.Preconditions.checkState;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "orderReportPdf")
public class OrderReportPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private SecurityService securityService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        String documentTitle = translationService.translate("deliveries.deliveryReport.report.orderedProduct.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        checkState(model.get("id") != null, "Unable to generate report for unsaved delivery! (missing id)");

        Long deliveryId = Long.valueOf(model.get("id").toString());

        Entity delivery = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY)
                .get(deliveryId);
        createHeaderTable(document, locale, delivery);
        createTableWith(document, locale, delivery);
        String endOfPrint = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);

        pdfHelper.addEndOfDocument(document, writer, endOfPrint);

        return translationService.translate("deliveries.deliveryReport.report.deliveredProduct.fileName", locale,
                delivery.getStringField(DeliveryFields.NUMBER),
                DateUtils.REPORT_D_T_F.format((Date) delivery.getField("updateDate")));
    }

    private void createHeaderTable(final Document document, final Locale locale, final Entity delivery) throws DocumentException {
        String description = delivery.getStringField(DeliveryFields.DESCRIPTION);
        boolean hasDescription = !StringUtils.isEmpty(description);
        if (hasDescription) {
            createPanelWithOrWithoutDescription(document, locale, delivery, true);
        } else {
            createPanelWithOrWithoutDescription(document, locale, delivery, false);
        }
    }

    private void createPanelWithOrWithoutDescription(final Document document, final Locale locale, final Entity order,
            final boolean withDescription) throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(3);
        panelTable.setSpacingBefore(7);
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.number", locale),
                order.getStringField(DeliveryFields.NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.name", locale),
                order.getStringField(DeliveryFields.NAME) == null ? "" : order.getStringField(DeliveryFields.NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.description", locale),
                order.getStringField(DeliveryFields.DESCRIPTION) == null ? "" : order.getStringField(DeliveryFields.DESCRIPTION));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.deliveryDate", locale),
                order.getField(DeliveryFields.DELIVERY_DATE) == null ? "" : order.getField(DeliveryFields.DELIVERY_DATE));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "deliveries.deliveryReport.report.columnHeader.supplier", locale), order
                .getBelongsToField(DeliveryFields.SUPPLIER) == null ? "" : order.getBelongsToField(DeliveryFields.SUPPLIER)
                .getStringField("name"));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");
        document.add(panelTable);
    }

    private void createTableWith(final Document document, final Locale locale, final Entity delivery) throws DocumentException {
        Paragraph productTableTitle = new Paragraph(new Phrase(translationService.translate(
                "deliveries.deliveryReport.report.orderedProduct.products", locale), FontUtils.getDejavuBold11Dark()));
        productTableTitle.setSpacingBefore(7f);
        productTableTitle.setSpacingAfter(7f);
        document.add(productTableTitle);
        List<String> resourcesTableHeader = new ArrayList<String>();

        resourcesTableHeader.add(translationService.translate("deliveries.deliveryReport.report.ordererProduct.number", locale));
        resourcesTableHeader.add(translationService.translate("deliveries.deliveryReport.report.ordererProduct.name", locale));
        resourcesTableHeader.add(translationService.translate("deliveries.deliveryReport.report.ordererProduct.ordererQuantity",
                locale));
        resourcesTableHeader.add(translationService.translate("deliveries.deliveryReport.report.ordererProduct.unit", locale));

        PdfPTable resourcesTable = pdfHelper.createTableWithHeader(4, resourcesTableHeader, false);

        List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);
        for (Entity orderedProduct : orderedProducts) {
            resourcesTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            resourcesTable.addCell(new Phrase(orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT).getStringField(
                    NUMBER), FontUtils.getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT).getStringField(
                    ProductFields.NAME), FontUtils.getDejavuRegular9Dark()));
            resourcesTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            resourcesTable.addCell(new Phrase(numberService.format(orderedProduct
                    .getDecimalField(OrderedProductFields.ORDERED_QUANTITY)), FontUtils.getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT).getStringField(
                    ProductFields.UNIT), FontUtils.getDejavuRegular9Dark()));
        }
        document.add(resourcesTable);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("deliveries.deliveryReport.report.orderedProduct.title", locale));
    }

}
