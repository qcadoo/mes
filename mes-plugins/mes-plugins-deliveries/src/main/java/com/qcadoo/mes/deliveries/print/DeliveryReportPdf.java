package com.qcadoo.mes.deliveries.print;

import static com.google.common.base.Preconditions.checkState;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DAMAGED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ORDERED_QUANTITY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "deliveryReportPdf")
public class DeliveryReportPdf extends ReportPdfView {

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
        String documentTitle = translationService.translate("deliveries.deliveryReport.title", locale);
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

        return translationService.translate("deliveries.deliveryReport.report.fileName", locale);
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

    private void createPanelWithOrWithoutDescription(final Document document, final Locale locale, final Entity delivery,
            final boolean withDescription) throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(3);
        panelTable.setSpacingBefore(7);
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.number", locale),
                delivery.getStringField(DeliveryFields.NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.name", locale),
                delivery.getStringField(DeliveryFields.NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.description", locale),
                delivery.getStringField(DeliveryFields.DESCRIPTION));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.deliveryDate", locale),
                delivery.getField(DeliveryFields.DELIVERY_DATE));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "deliveries.deliveryReport.report.columnHeader.supplier", locale),
                delivery.getBelongsToField(DeliveryFields.SUPPLIER).getStringField("name"));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");
        document.add(panelTable);
    }

    private void createTableWith(final Document document, final Locale locale, final Entity delivery) throws DocumentException {

        List<String> resourcesTableHeader = new ArrayList<String>();

        resourcesTableHeader
                .add(translationService.translate("deliveries.deliveryReport.report.deliveredProduct.number", locale));
        resourcesTableHeader.add(translationService.translate("deliveries.deliveryReport.report.deliveredProduct.name", locale));
        resourcesTableHeader.add(translationService.translate(
                "deliveries.deliveryReport.report.deliveredProduct.ordererQuantity", locale));
        resourcesTableHeader.add(translationService.translate(
                "deliveries.deliveryReport.report.deliveredProduct.deliveredQuantity", locale));
        resourcesTableHeader.add(translationService.translate(
                "deliveries.deliveryReport.report.deliveredProduct.damagedQuantity", locale));
        resourcesTableHeader.add(translationService.translate("deliveries.deliveryReport.report.deliveredProduct.unit", locale));

        PdfPTable resourcesTable = pdfHelper.createTableWithHeader(6, resourcesTableHeader, false);

        for (Map.Entry<Long, ProductWithQuantities> orderedProduct : getOrderedAndDeliveredProductsList(delivery).entrySet()) {
            resourcesTable.addCell(new Phrase(orderedProduct.getValue().getProduct().getStringField(ProductFields.NUMBER),
                    FontUtils.getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(orderedProduct.getValue().getProduct().getStringField(ProductFields.NAME),
                    FontUtils.getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(numberService.format(orderedProduct.getValue().getOrderedQuantity()), FontUtils
                    .getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(numberService.format(orderedProduct.getValue().getDeliveredQuantity()), FontUtils
                    .getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(numberService.format(orderedProduct.getValue().getDamagedQuantity()), FontUtils
                    .getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(orderedProduct.getValue().getProduct().getStringField(ProductFields.UNIT),
                    FontUtils.getDejavuRegular9Dark()));
        }
        document.add(resourcesTable);
    }

    private Map<Long, ProductWithQuantities> getOrderedAndDeliveredProductsList(final Entity delivery) {
        Map<Long, ProductWithQuantities> productsList = new HashMap<Long, ProductWithQuantities>();
        for (Entity deliveredProduct : delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS)) {
            ProductWithQuantities delivered = new ProductWithQuantities();
            delivered.setDamagedQuantity(deliveredProduct.getDecimalField(DAMAGED_QUANTITY));
            delivered.setDeliveredQuantity(deliveredProduct.getDecimalField(DELIVERED_QUANTITY));
            delivered.setOrderedQuantity(deliveredProduct.getDecimalField(ORDERED_QUANTITY));
            delivered.setProduct(deliveredProduct.getBelongsToField(PRODUCT));
            productsList.put(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getId(), delivered);
        }
        for (Entity ordered : delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS)) {
            if (!productsList.containsKey(ordered.getBelongsToField(PRODUCT).getId())) {
                ProductWithQuantities productWithQuantities = new ProductWithQuantities();
                productWithQuantities.setOrderedQuantity(ordered.getDecimalField(ORDERED_QUANTITY));
                productWithQuantities.setProduct(ordered.getBelongsToField(PRODUCT));
                productsList.put(ordered.getBelongsToField(DeliveredProductFields.PRODUCT).getId(), productWithQuantities);
            } else {
                ProductWithQuantities productsWithQuantities = productsList.get(ordered.getBelongsToField(PRODUCT).getId());
                productsWithQuantities.setOrderedQuantity(ordered.getDecimalField(ORDERED_QUANTITY));
                productsList.put(ordered.getBelongsToField(PRODUCT).getId(), productsWithQuantities);
            }
        }
        return productsList;
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("deliveries.deliveryReport.title", locale));
    }
}
