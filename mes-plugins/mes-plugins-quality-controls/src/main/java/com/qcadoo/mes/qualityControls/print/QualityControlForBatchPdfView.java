package com.qcadoo.mes.qualityControls.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.mes.utils.pdf.ReportPdfView;

public class QualityControlForBatchPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private QualityControlsReportService qualityControlsReportService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        String documentTitle = getTranslationService().translate("qualityControls.qualityControlForBatch.report.title", locale);
        String documentAuthor = getTranslationService().translate("qualityControls.qualityControl.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), user);
        qualityControlsReportService.addQualityControlReportHeader(document, model.get("dateFrom").toString(), model
                .get("dateTo").toString(), locale);
        addOrderSeries(document, qualityControlsReportService.getOrderSeries(model.get("dateFrom").toString(), model
                .get("dateTo").toString(), "qualityControlsForBatch"), locale);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("qualityControls.qualityControlForBatch.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("qualityControls.qualityControlForBatch.report.title", locale));
    }

    private void addOrderSeries(final Document document, final List<Entity> entities, final Locale locale)
            throws DocumentException {
        List<String> qualityHeader = new ArrayList<String>();
        qualityHeader.add(getTranslationService().translate("qualityControls.qualityControl.report.product.number", locale));
        qualityHeader.add(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.window.qualityControlForBatch.controlledQuantity.label", locale));
        qualityHeader.add(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.window.qualityControlForBatch.rejectedQuantity.label", locale));
        qualityHeader.add(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.window.qualityControlForBatch.acceptedDefectsQuantity.label", locale));
        PdfPTable table = PdfUtil.createTableWithHeader(4, qualityHeader, false);

        for (Entity entity : entities) {
            Entity product = entity.getBelongsToField("order").getBelongsToField("product");
            table.addCell(new Phrase(product != null ? product.getField("number").toString() : "", PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(getDecimalFormat().format((BigDecimal) entity.getField("controlledQuantity")), PdfUtil
                    .getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format((BigDecimal) entity.getField("rejectedQuantity")), PdfUtil
                    .getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format((BigDecimal) entity.getField("acceptedDefectsQuantity")), PdfUtil
                    .getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }

}
