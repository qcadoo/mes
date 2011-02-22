package com.qcadoo.mes.qualityControls.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.model.DataDefinition;
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
        String documentTitle = getTranslationService().translate("qualityControls.qualityControlForBatch.report.title", locale);
        String documentAuthor = getTranslationService().translate("qualityControls.qualityControlForBatch.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), user);
        addTables(document, model.get("dateFrom").toString(), model.get("dateTo").toString(), locale);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("qualityControls.qualityControlForBatch.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("qualityControls.qualityControlForBatch.report.title", locale));
    }

    private void addTables(final Document document, final String dateFrom, final String dateTo, final Locale locale)
            throws DocumentException {
        Paragraph firstParagraphTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.report.paragrah", locale), PdfUtil.getArialBold11Light()));
        firstParagraphTitle.add(new Phrase(" " + dateFrom + " - " + dateTo, PdfUtil.getArialBold11Light()));
        firstParagraphTitle.setSpacingBefore(20);
        document.add(firstParagraphTitle);

        Paragraph secondParagraphTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.report.paragrah2", locale), PdfUtil.getArialBold11Light()));
        secondParagraphTitle.setSpacingBefore(20);
        document.add(secondParagraphTitle);

        addOrderSeries(document, getOrderSeries(dateFrom, dateTo), locale);
    }

    private void addOrderSeries(final Document document, final List<Entity> entities, final Locale locale)
            throws DocumentException {
        List<String> qualityHeader = new ArrayList<String>();
        qualityHeader.add(getTranslationService().translate("products.order.number.label", locale));
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

    private List<Entity> getOrderSeries(final String dateFrom, final String dateTo) {
        DataDefinition dataDef = dataDefinitionService.get("qualityControls", "qualityControl");
        try {
            SearchResult result = dataDef.find()
                    .restrictedWith(Restrictions.ge(dataDef.getField("date"), DateType.parseDate(dateFrom, false)))
                    .restrictedWith(Restrictions.le(dataDef.getField("date"), DateType.parseDate(dateTo, true)))
                    .restrictedWith(Restrictions.eq("qualityControlType", "qualityControlsForBatch"))
                    .restrictedWith(Restrictions.eq("closed", true)).list();
            return result.getEntities();
        } catch (ParseException e) {
            return Collections.emptyList();
        }
    }
}
