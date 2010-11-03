package com.qcadoo.mes.products.print.view.pdf;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.types.internal.DateType;

public final class OrderPdfView extends ProductsPdfView {

    @Override
    protected String addContent(final Document document, final DefaultEntity entity, final Locale locale)
            throws DocumentException, IOException {
        UsersUser user = securityService.getCurrentUser();
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_TIME_FORMAT);
        LineSeparator line = new LineSeparator(3, 100f, lineDarkColor, Element.ALIGN_LEFT, 0);
        document.add(Chunk.NEWLINE);
        Paragraph title = new Paragraph(new Phrase(translationService.translate("products.order.report.order", locale),
                arialBold19Light));
        title.add(new Phrase(" " + entity.getField("name"), arialBold19Dark));
        title.setSpacingAfter(7f);
        document.add(title);
        document.add(line);
        PdfPTable userAndDate = new PdfPTable(2);
        userAndDate.setWidthPercentage(100f);
        userAndDate.setHorizontalAlignment(Element.ALIGN_LEFT);
        userAndDate.getDefaultCell().setBorderWidth(0);
        Paragraph userParagraph = new Paragraph(new Phrase(translationService.translate("products.order.report.author", locale),
                arialRegular9Light));
        userParagraph.add(new Phrase(" " + user.getUserName(), arialRegular9Dark));
        Paragraph dateParagraph = new Paragraph(df.format(new Date()), arialRegular9Light);
        userAndDate.addCell(userParagraph);
        userAndDate.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        userAndDate.addCell(dateParagraph);
        document.add(userAndDate);
        document.add(Chunk.NEWLINE);

        PdfPTable mainData = new PdfPTable(2);
        mainData.setWidthPercentage(100f);
        mainData.getDefaultCell().setBackgroundColor(backgroundColor);
        mainData.getDefaultCell().setBorderWidth(0);
        Paragraph numberParagraph = new Paragraph(new Phrase(translationService.translate("products.order.number.label", locale),
                arialBold9Dark));
        numberParagraph.add(Chunk.NEWLINE);
        numberParagraph.add(new Phrase(entity.getField("number").toString(), arialRegular9Light));
        mainData.addCell(numberParagraph);
        Paragraph startDateParagraph = new Paragraph(new Phrase(translationService.translate("products.order.dateFrom.label",
                locale), arialBold9Dark));
        startDateParagraph.add(Chunk.NEWLINE);
        startDateParagraph.add(new Phrase(entity.getField("dateFrom").toString(), arialRegular9Light));
        mainData.addCell(startDateParagraph);
        Paragraph nameParagraph = new Paragraph(new Phrase(translationService.translate("products.order.name.label", locale),
                arialBold9Dark));
        nameParagraph.add(Chunk.NEWLINE);
        nameParagraph.add(new Phrase(entity.getField("name").toString(), arialRegular9Light));
        mainData.addCell(nameParagraph);
        Paragraph endDateParagraph = new Paragraph(new Phrase(
                translationService.translate("products.order.dateTo.label", locale), arialBold9Dark));
        endDateParagraph.add(Chunk.NEWLINE);
        endDateParagraph.add(new Phrase(entity.getField("dateTo").toString(), arialRegular9Light));
        mainData.addCell(endDateParagraph);
        Entity product = (Entity) entity.getField("product");
        Paragraph productParagraph = new Paragraph(new Phrase(
                translationService.translate("products.order.product.label", locale), arialBold9Dark));
        productParagraph.add(Chunk.NEWLINE);
        if (product == null) {
            productParagraph.add(new Phrase("", arialRegular9Light));
        } else {
            productParagraph.add(new Phrase(product.getField("name").toString(), arialRegular9Light));
        }
        mainData.addCell(productParagraph);
        Paragraph stateParagraph = new Paragraph(new Phrase(translationService.translate("products.order.state.label", locale),
                arialBold9Dark));
        stateParagraph.add(Chunk.NEWLINE);
        stateParagraph.add(new Phrase(entity.getField("state").toString(), arialRegular9Light));
        mainData.addCell(stateParagraph);
        document.add(mainData);

        return "Order" + entity.getField("number");
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.order.report.title", locale));
    }
}
