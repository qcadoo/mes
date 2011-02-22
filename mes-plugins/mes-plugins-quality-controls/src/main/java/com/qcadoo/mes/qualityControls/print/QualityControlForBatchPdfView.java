package com.qcadoo.mes.qualityControls.print;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.mes.utils.pdf.ReportPdfView;

public class QualityControlForBatchPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        Date dateFrom = (Date) model.get("dateFrom");
        Date dateTo = (Date) model.get("dateTo");
        String documentTitle = getTranslationService().translate("qualityControls.qualityControlForBatch.report.title", locale);
        String documentAuthor = getTranslationService().translate("qualityControls.qualityControlForBatch.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), user);
        addQualityControlsOrders(document, dateFrom, dateTo, locale);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("qualityControls.qualityControlForBatch.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("qualityControls.qualityControlForBatch.report.title", locale));
    }

    private void addQualityControlsOrders(final Document document, final Date dateFrom, final Date dateTo, final Locale locale)
            throws DocumentException {
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_TIME_FORMAT);
        Paragraph firstParagraphTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.report.paragrah", locale), PdfUtil.getArialBold11Light()));
        firstParagraphTitle.add(new Phrase(df.format(dateFrom) + "-" + df.format(dateTo), PdfUtil.getArialBold11Light()));
        document.add(firstParagraphTitle);

        Paragraph secondParagraphTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.report.paragrah2", locale), PdfUtil.getArialBold11Light()));
        document.add(secondParagraphTitle);

        List<String> qualityHeader = new ArrayList<String>();
        qualityHeader.add(getTranslationService().translate("products.order.number.label", locale));
        qualityHeader.add(getTranslationService().translate(
                "qualityControl.qualityControlForBatch.window.qualityControlForBatch.controlledQuantity.label", locale));
        qualityHeader.add(getTranslationService().translate(
                "qualityControl.qualityControlForBatch.window.qualityControlForBatch.rejectedQuantity.label", locale));
        qualityHeader.add(getTranslationService().translate(
                "qualityControl.qualityControlForBatch.window.qualityControlForBatch.acceptedDefectsQuantity.label", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(4, qualityHeader, false);
        SearchResult result = dataDefinitionService.get("qualityControl", "qualityControl").find()
                .restrictedWith(Restrictions.eq("dateFrom", dateFrom)).restrictedWith(Restrictions.eq("dateTo", dateTo))
                .restrictedWith(Restrictions.eq("qualityControlType", "qualityControlsForBatch"))
                .restrictedWith(Restrictions.eq("closed", true)).list();
        for (Entity entity : result.getEntities()) {
            Entity product = entity.getBelongsToField("order").getBelongsToField("product");
            table.addCell(new Phrase(product != null ? product.getField("number").toString() : "", PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entity.getField("takenForControlQuantity").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entity.getField("rejectedQuantity").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entity.getField("acceptedDefectsQuantity").toString(), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }
}
