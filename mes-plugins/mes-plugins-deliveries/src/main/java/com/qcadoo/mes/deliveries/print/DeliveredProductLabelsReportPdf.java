package com.qcadoo.mes.deliveries.print;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "deliveredProductLabelsReportPdf")
public class DeliveredProductLabelsReportPdf extends ReportPdfView {

    private static final String L_ID = "id";

    private static final String L_IDS = "ids";

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_FORMAT,
            LocaleContextHolder.getLocale());

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
                                final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get(L_IDS) != null, "Unable to generate report - missing ids");

        List<Long> ids = (List<Long>) model.get("ids");

        List<Entity> deliveredProducts = getDeliveredProductsFromIds(ids);

        PdfPTable table = pdfHelper.createPanelTable(2);

        table.setTableEvent(null);

        int index = 0;

        for (Entity deliveredProduct : deliveredProducts) {
            PdfPCell cell = new PdfPCell();

            cell.setFixedHeight(330F);

            cell.addElement(createBarcodeTable(writer, deliveredProduct, locale));

            table.addCell(cell);

            index++;

            if (index % 4 == 0) {
                document.add(table);

                if (index < deliveredProducts.size()) {
                    document.add(Chunk.NEXTPAGE);

                    table = pdfHelper.createPanelTable(2);

                    table.setTableEvent(null);
                }
            } else {
                if (index == deliveredProducts.size()) {
                    table.completeRow();

                    document.add(table);
                }
            }
        }

        return translationService.translate("deliveries.deliveredProductLabelsReport.report.fileName", locale, DateUtils.toDateTimeString(new Date()));
    }

    private PdfPTable createBarcodeTable(final PdfWriter writer, final Entity deliveredProduct, Locale locale) {
        PdfPTable barcodeTable = new PdfPTable(1);

        barcodeTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        barcodeTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        barcodeTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        barcodeTable.getDefaultCell().setPaddingTop(10F);
        barcodeTable.getDefaultCell().setPaddingBottom(10F);

        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        String number = product.getStringField(ProductFields.NUMBER);
        String name = product.getStringField(ProductFields.NAME);
        barcodeTable.addCell(number.substring(0, Math.min(29, number.length())));
        barcodeTable.getDefaultCell().setPaddingTop(0F);
        barcodeTable.addCell(name.substring(0, Math.min(58, name.length())));

        barcodeTable.getDefaultCell().setPaddingTop(10F);
        barcodeTable.getDefaultCell().setPaddingLeft(25F);
        barcodeTable.getDefaultCell().setPaddingRight(25F);
        barcodeTable.getDefaultCell().setPaddingBottom(0F);

        String code = product.getStringField(ProductFields.EAN);
        if (code == null) {
            code = product.getStringField(ProductFields.NUMBER);
        }
        createBarcode(writer, barcodeTable, code, true);

        Date expirationDate = deliveredProduct.getDateField(DeliveredProductFields.EXPIRATION_DATE);
        if (expirationDate != null) {
            barcodeTable.addCell(new Phrase(translationService.translate("deliveries.deliveredProductLabelsReport.report.expirationDate.label", locale) + " " + simpleDateFormat.format(expirationDate), FontUtils.getDejavuBold11Light()));
        }
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);

        barcodeTable.addCell(translationService.translate("deliveries.deliveredProductLabelsReport.report.delivery.label", locale) + " " + delivery.getStringField(DeliveryFields.NUMBER));

        Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);

        if (supplier != null) {
            barcodeTable.addCell(translationService.translate("deliveries.deliveredProductLabelsReport.report.supplier.label", locale) + " " + supplier.getStringField(CompanyFields.NUMBER));
        }

        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);
        if (batch != null) {
            barcodeTable.addCell(translationService.translate("deliveries.deliveredProductLabelsReport.report.batch.label", locale) + " " + batch.getStringField(BatchFields.NUMBER));
            createBarcode(writer, barcodeTable, batch.getStringField(BatchFields.NUMBER), false);
        }

        return barcodeTable;
    }

    private void createBarcode(final PdfWriter writer, final PdfPTable barcodeTable, final String code, boolean font) {
        Barcode128 code128 = new Barcode128();
        code128.setBarHeight(50F);
        if (!font) {
            code128.setFont(null);
        }

        code128.setCode(code);

        PdfContentByte cb = writer.getDirectContent();

        Image barcodeImage = code128.createImageWithBarcode(cb, null, null);

        barcodeTable.addCell(barcodeImage);
    }

    private List<Entity> getDeliveredProductsFromIds(final List<Long> ids) {
        return getDeliveredProductDD().find().add(SearchRestrictions.in(L_ID, ids)).list().getEntities();
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("deliveries.deliveredProductLabelsReport.report.title", locale));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

    private DataDefinition getDeliveredProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
    }

}
