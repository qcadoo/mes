package com.qcadoo.mes.qualityControls.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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

public class QualityControlForOrderPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private QualityControlsReportService qualityControlsReportService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        String documentTitle = getTranslationService().translate("qualityControls.qualityControlForOrder.report.title", locale);
        String documentAuthor = getTranslationService().translate("qualityControls.qualityControl.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), user);
        qualityControlsReportService.addQualityControlReportHeader(document, model.get("dateFrom").toString(), model
                .get("dateTo").toString(), locale);
        Map<Entity, List<Entity>> productOrders = new HashMap<Entity, List<Entity>>();
        Map<Entity, List<BigDecimal>> quantities = new HashMap<Entity, List<BigDecimal>>();
        qualityControlsReportService.aggregateOrdersDataForOrder(productOrders, quantities, qualityControlsReportService
                .getOrderSeries(model.get("dateFrom").toString(), model.get("dateTo").toString(), "qualityControlsForBatch"),
                true);
        addOrderSeries(document, quantities, locale);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("qualityControls.qualityControlForOrder.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("qualityControls.qualityControlForOrder.report.title", locale));
    }

    private void addOrderSeries(final Document document, final Map<Entity, List<BigDecimal>> quantities, final Locale locale)
            throws DocumentException {
        List<String> qualityHeader = new ArrayList<String>();
        qualityHeader.add(getTranslationService().translate("qualityControls.qualityControl.report.product.number", locale));
        qualityHeader.add(getTranslationService().translate("qualityControls.qualityControl.report.controlled.quantity", locale));
        qualityHeader.add(getTranslationService().translate("qualityControls.qualityControl.report.done.quantity", locale));
        qualityHeader.add(getTranslationService().translate("qualityControls.qualityControl.report.correct.quantity", locale));
        qualityHeader.add(getTranslationService().translate("qualityControls.qualityControl.report.incorrect.quantity", locale));
        qualityHeader.add(getTranslationService().translate("qualityControls.qualityControl.report.objective.quantity", locale));
        PdfPTable table = PdfUtil.createTableWithHeader(6, qualityHeader, false);

        for (Entry<Entity, List<BigDecimal>> entry : quantities.entrySet()) {
            table.addCell(new Phrase(entry.getKey() != null ? entry.getKey().getField("number").toString() : "", PdfUtil
                    .getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue().get(0)), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue().get(4)), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue().get(1)), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue().get(2)), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(getDecimalFormat().format(entry.getValue().get(3)), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }
}
