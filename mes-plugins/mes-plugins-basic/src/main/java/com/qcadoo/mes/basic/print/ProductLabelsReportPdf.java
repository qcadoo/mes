package com.qcadoo.mes.basic.print;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "productLabelsReportPdf")
public class ProductLabelsReportPdf extends ReportPdfView {

    private static final String L_ID = "id";

    private static final String L_IDS = "ids";

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

        List<Entity> products = getProductsFromIds(ids);

        PdfPTable table = pdfHelper.createPanelTable(2);

        table.setTableEvent(null);

        int index = 0;

        for (Entity product : products) {
            PdfPCell cell = new PdfPCell();

            cell.setFixedHeight(165F);

            cell.addElement(createBarcodeTable(writer, product));

            table.addCell(cell);

            index++;

            if (index % 8 == 0) {
                document.add(table);

                if (index < products.size()) {
                    document.add(Chunk.NEXTPAGE);

                    table = pdfHelper.createPanelTable(2);

                    table.setTableEvent(null);
                }
            } else {
                if (index == products.size()) {
                    table.completeRow();

                    document.add(table);
                }
            }
        }

        return translationService.translate("basic.productLabelsReport.report.fileName", locale, DateUtils.toDateTimeString(new Date()));
    }

    private PdfPTable createBarcodeTable(final PdfWriter writer, final Entity product) {
        PdfPTable barcodeTable = new PdfPTable(1);

        barcodeTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        barcodeTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        barcodeTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        barcodeTable.getDefaultCell().setPaddingTop(10F);

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

        return barcodeTable;
    }

    private void createBarcode(final PdfWriter writer, final PdfPTable barcodeTable, final String code) {
        Barcode128 code128 = new Barcode128();
        code128.setBarHeight(50F);
        code128.setX(1.8f);
        code128.setFont(null);

        code128.setCode(code);

        PdfContentByte cb = writer.getDirectContent();

        Image barcodeImage = code128.createImageWithBarcode(cb, null, null);

        barcodeTable.addCell(barcodeImage);
    }

    private List<Entity> getProductsFromIds(final List<Long> ids) {
        return getProductDD().find().add(SearchRestrictions.in(L_ID, ids)).list().getEntities();
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("basic.productLabelsReport.report.title", locale));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
