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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(DeliveredProductLabelsReportPdf.class);

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

        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        String number = product.getStringField(ProductFields.NUMBER);
        PdfPCell numberCell = new PdfPCell(new Phrase(number, FontUtils.getDejavuBold11Light()));
        numberCell.setFixedHeight(15f);
        numberCell.setPaddingTop(10F);
        numberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numberCell.setBorder(Rectangle.NO_BORDER);
        barcodeTable.addCell(numberCell);
        PdfPCell nameCell = new PdfPCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuBold11Light()));
        nameCell.setFixedHeight(30f);
        nameCell.setPaddingTop(10F);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        nameCell.setBorder(Rectangle.NO_BORDER);
        barcodeTable.addCell(nameCell);

        String code = product.getStringField(ProductFields.EAN);
        if (code == null) {
            code = number;
        }
        createBarcode(writer, barcodeTable, code);

        PdfPCell codeCell = new PdfPCell(new Phrase(code, FontUtils.getDejavuBold11Light()));
        codeCell.setFixedHeight(15f);
        codeCell.setPaddingTop(10F);
        codeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        codeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        codeCell.setBorder(Rectangle.NO_BORDER);
        barcodeTable.addCell(codeCell);

        Date expirationDate = deliveredProduct.getDateField(DeliveredProductFields.EXPIRATION_DATE);
        if (expirationDate != null) {
            barcodeTable.addCell(new Phrase(translationService.translate("deliveries.deliveredProductLabelsReport.report.expirationDate.label", locale) + " " + simpleDateFormat.format(expirationDate), FontUtils.getDejavuBold11Light()));
        }
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);

        PdfPCell deliveryCell = new PdfPCell(new Phrase(translationService.translate("deliveries.deliveredProductLabelsReport.report.delivery.label", locale) + delivery.getStringField(DeliveryFields.NUMBER), FontUtils.getDejavuBold11Light()));
        deliveryCell.setFixedHeight(15f);
        deliveryCell.setPaddingTop(10F);
        deliveryCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        deliveryCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        deliveryCell.setBorder(Rectangle.NO_BORDER);
        barcodeTable.addCell(deliveryCell);

        Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);

        if (supplier != null) {
            PdfPCell supplierCell = new PdfPCell(new Phrase(translationService.translate("deliveries.deliveredProductLabelsReport.report.supplier.label", locale) + " " + supplier.getStringField(CompanyFields.NUMBER), FontUtils.getDejavuBold11Light()));
            supplierCell.setFixedHeight(30f);
            supplierCell.setPaddingTop(10F);
            supplierCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            supplierCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            supplierCell.setBorder(Rectangle.NO_BORDER);
            barcodeTable.addCell(supplierCell);
        }

        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);
        if (batch != null) {
            PdfPCell batchCell = new PdfPCell(new Phrase(translationService.translate("deliveries.deliveredProductLabelsReport.report.batch.label", locale) + " " + batch.getStringField(BatchFields.NUMBER), FontUtils.getDejavuBold11Light()));
            batchCell.setFixedHeight(15f);
            batchCell.setPaddingTop(10F);
            batchCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            batchCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            batchCell.setBorder(Rectangle.NO_BORDER);
            barcodeTable.addCell(batchCell);

            createBarcode(writer, barcodeTable, batch.getStringField(BatchFields.NUMBER));
        }

        return barcodeTable;
    }

    private void createBarcode(final PdfWriter writer, final PdfPTable barcodeTable, final String code) {
        Barcode128 code128 = new Barcode128();
        code128.setBarHeight(32F);
        code128.setX(1.8f);
        code128.setFont(null);

        code128.setCode(code);

        PdfContentByte cb = writer.getDirectContent();

        try {
            Image barcodeImage = code128.createImageWithBarcode(cb, null, null);

            barcodeTable.addCell(barcodeImage);
        } catch (ExceptionConverter e) {
            LOG.warn("Problem with generating barcode for batch: " + code);
        }
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
