package com.qcadoo.mes.basic.print;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
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
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "staffLabelsReportPdf")
public class StaffLabelsReportPdf extends ReportPdfView {

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

        List<Long> staffIds = (List<Long>) model.get("ids");

        List<Entity> staffs = getStaffFromIds(staffIds);

        PdfPTable table = pdfHelper.createPanelTable(2);

        table.setTableEvent(null);

        int index = 0;

        for (Entity staff : staffs) {
            PdfPCell cell = new PdfPCell();

            cell.setFixedHeight(165F);

            cell.addElement(createBarcodeTable(writer, staff));

            table.addCell(cell);

            index++;

            if (index % 8 == 0) {
                document.add(table);

                if (index < staffs.size()) {
                    document.add(Chunk.NEXTPAGE);

                    table = pdfHelper.createPanelTable(2);

                    table.setTableEvent(null);
                }
            } else {
                if (index == staffs.size()) {
                    table.completeRow();

                    document.add(table);
                }
            }
        }

        return translationService.translate("basic.staffLabelsReport.report.fileName", locale, DateUtils.toDateTimeString(new Date()));
    }

    private PdfPTable createBarcodeTable(final PdfWriter writer, final Entity staff) {
        PdfPTable barcodeTable = new PdfPTable(1);

        barcodeTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        barcodeTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        barcodeTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        barcodeTable.getDefaultCell().setPaddingTop(10F);
        barcodeTable.getDefaultCell().setPaddingBottom(10F);

        createNameAndSurname(writer, barcodeTable, staff);

        barcodeTable.getDefaultCell().setPaddingTop(0F);
        barcodeTable.getDefaultCell().setPaddingLeft(30F);
        barcodeTable.getDefaultCell().setPaddingRight(30F);
        barcodeTable.getDefaultCell().setPaddingBottom(0F);

        createBarcode(writer, barcodeTable, staff);

        return barcodeTable;
    }

    private void createNameAndSurname(final PdfWriter writer, final PdfPTable barcodeTable, final Entity staff) {
        String name = staff.getStringField(StaffFields.NAME);
        String surname = staff.getStringField(StaffFields.SURNAME);

        Phrase nameAndSurname = new Phrase(new StringBuilder(name).append(" ").append(surname).toString(), FontUtils.getDejavuBold11Dark());

        barcodeTable.addCell(nameAndSurname);
    }

    private void createBarcode(final PdfWriter writer, final PdfPTable barcodeTable, final Entity staff) {
        String number = staff.getStringField(StaffFields.NUMBER);

        Barcode128 code128 = new Barcode128();

        code128.setCode(number);

        PdfContentByte cb = writer.getDirectContent();

        Image barcodeImage = code128.createImageWithBarcode(cb, null, null);

        barcodeTable.addCell(barcodeImage);
    }

    private List<Entity> getStaffFromIds(final List<Long> staffIds) {
        return getStaffDD().find().add(SearchRestrictions.in(L_ID, staffIds)).list().getEntities();
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("basic.staffLabelsReport.report.title", locale));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

    private DataDefinition getStaffDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF);
    }

}
