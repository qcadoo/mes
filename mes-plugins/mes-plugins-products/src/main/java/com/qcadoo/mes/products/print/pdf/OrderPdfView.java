package com.qcadoo.mes.products.print.pdf;

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
import com.lowagie.text.Rectangle;
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

        PdfPTable mainData = new PdfPTable(2);
        mainData.setWidthPercentage(100f);
        mainData.setSpacingBefore(20);
        mainData.getDefaultCell().setBackgroundColor(backgroundColor);
        mainData.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        mainData.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        mainData.getDefaultCell().setPadding(8.0f);
        mainData.setTableEvent(new TableBorderEvent());

        PdfPTable numberPdfPTable = new PdfPTable(1);
        numberPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        numberPdfPTable.addCell(new Phrase(translationService.translate("products.order.number.label", locale), arialBold10Dark));
        numberPdfPTable.addCell(new Phrase(entity.getField("number").toString(), arialRegular10Dark));
        mainData.addCell(numberPdfPTable);
        PdfPTable startDatePdfPTable = new PdfPTable(1);
        startDatePdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        startDatePdfPTable.addCell(new Phrase(translationService.translate("products.order.dateFrom.label", locale),
                arialBold10Dark));
        startDatePdfPTable.addCell(new Phrase(entity.getField("dateFrom").toString(), arialRegular10Dark));
        mainData.addCell(startDatePdfPTable);
        PdfPTable namePdfPTable = new PdfPTable(1);
        namePdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        namePdfPTable.addCell(new Phrase(translationService.translate("products.order.name.label", locale), arialBold10Dark));
        namePdfPTable.addCell(new Phrase(entity.getField("name").toString(), arialRegular10Dark));
        mainData.addCell(namePdfPTable);
        PdfPTable endDatePdfPTable = new PdfPTable(1);
        endDatePdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        endDatePdfPTable
                .addCell(new Phrase(translationService.translate("products.order.dateTo.label", locale), arialBold10Dark));
        endDatePdfPTable.addCell(new Phrase(entity.getField("dateTo").toString(), arialRegular10Dark));
        mainData.addCell(endDatePdfPTable);
        Entity product = (Entity) entity.getField("product");
        PdfPTable productPdfPTable = new PdfPTable(1);
        productPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        productPdfPTable
                .addCell(new Phrase(translationService.translate("products.order.product.label", locale), arialBold10Dark));
        if (product == null) {
            productPdfPTable.addCell(new Phrase("", arialRegular10Dark));
        } else {
            productPdfPTable.addCell(new Phrase(product.getField("name").toString(), arialRegular10Dark));
        }
        mainData.addCell(productPdfPTable);
        PdfPTable statePdfPTable = new PdfPTable(1);
        statePdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        statePdfPTable.addCell(new Phrase(translationService.translate("products.order.state.label", locale), arialBold10Dark));
        statePdfPTable.addCell(new Phrase(entity.getField("state").toString(), arialRegular10Dark));
        mainData.addCell(statePdfPTable);
        document.add(mainData);

        PdfPTable detailData = new PdfPTable(2);
        detailData.setWidthPercentage(100f);
        detailData.setSpacingBefore(5);
        detailData.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        detailData.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        detailData.getDefaultCell().setPadding(5.0f);
        PdfPTable machinePdfPTable = new PdfPTable(1);
        machinePdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        machinePdfPTable
                .addCell(new Phrase(translationService.translate("products.order.machine.label", locale), arialBold9Dark));
        Object machine = entity.getField("machine");
        if (machine == null) {
            machinePdfPTable.addCell(new Phrase("", arialRegular9Dark));
        } else {
            machinePdfPTable.addCell(new Phrase(machine.toString(), arialRegular9Dark));
        }
        detailData.addCell(machinePdfPTable);
        PdfPTable effectiveDateFromPdfPTable = new PdfPTable(1);
        effectiveDateFromPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        effectiveDateFromPdfPTable.addCell(new Phrase(translationService.translate("products.order.effectiveDateFrom.label",
                locale), arialBold9Dark));
        effectiveDateFromPdfPTable.addCell(new Phrase(entity.getField("effectiveDateFrom").toString(), arialRegular9Dark));
        detailData.addCell(effectiveDateFromPdfPTable);
        PdfPTable plannedQuantityPdfPTable = new PdfPTable(1);
        plannedQuantityPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        plannedQuantityPdfPTable.addCell(new Phrase(translationService.translate("products.order.plannedQuantity.label", locale),
                arialBold9Dark));
        Object plannedQuantity = entity.getField("plannedQuantity");
        if (plannedQuantity == null) {
            plannedQuantityPdfPTable.addCell(new Phrase("", arialRegular9Dark));
        } else {
            plannedQuantityPdfPTable.addCell(new Phrase(plannedQuantity.toString(), arialRegular9Dark));
        }
        detailData.addCell(plannedQuantityPdfPTable);
        PdfPTable effectiveDateToPdfPTable = new PdfPTable(1);
        effectiveDateToPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        effectiveDateToPdfPTable.addCell(new Phrase(translationService.translate("products.order.effectiveDateTo.label", locale),
                arialBold9Dark));
        Object effectiveDateTo = entity.getField("effectiveDateTo");
        if (effectiveDateTo == null) {
            effectiveDateToPdfPTable.addCell(new Phrase(translationService.translate(
                    "products.order.report.effectiveDateToState", locale), arialRegular9Dark));
        } else {
            effectiveDateToPdfPTable.addCell(new Phrase(effectiveDateTo.toString(), arialRegular9Dark));
        }
        detailData.addCell(effectiveDateToPdfPTable);
        PdfPTable doneQuantityPdfPTable = new PdfPTable(1);
        doneQuantityPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        doneQuantityPdfPTable.addCell(new Phrase(translationService.translate("products.order.doneQuantity.label", locale),
                arialBold9Dark));
        Object doneQuantity = entity.getField("doneQuantity");
        if (doneQuantity == null) {
            doneQuantityPdfPTable.addCell(new Phrase("", arialRegular9Dark));
        } else {
            doneQuantityPdfPTable.addCell(new Phrase(doneQuantity.toString(), arialRegular9Dark));
        }
        detailData.addCell(doneQuantityPdfPTable);
        PdfPTable startWorkerPdfPTable = new PdfPTable(1);
        startWorkerPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        startWorkerPdfPTable.addCell(new Phrase(translationService.translate("products.order.startWorker.label", locale),
                arialBold9Dark));
        startWorkerPdfPTable.addCell(new Phrase(entity.getField("startWorker").toString(), arialRegular9Dark));
        detailData.addCell(startWorkerPdfPTable);
        PdfPTable instructionPdfPTable = new PdfPTable(1);
        instructionPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        instructionPdfPTable.addCell(new Phrase(translationService.translate(
                "products.orderDetailsView.mainWindow.orderDetailsForm.instruction.label", locale), arialBold9Dark));
        Entity instruction = (Entity) entity.getField("instruction");
        if (instruction == null) {
            instructionPdfPTable.addCell(new Phrase("", arialRegular9Dark));
        } else {
            instructionPdfPTable.addCell(new Phrase(instruction.getField("name").toString(), arialRegular9Dark));
        }
        detailData.addCell(instructionPdfPTable);
        PdfPTable endWorkerPdfPTable = new PdfPTable(1);
        endWorkerPdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        endWorkerPdfPTable.addCell(new Phrase(translationService.translate("products.order.endWorker.label", locale),
                arialBold9Dark));
        Object endWorker = entity.getField("endWorker");
        if (endWorker == null) {
            endWorkerPdfPTable.addCell(new Phrase("", arialRegular9Dark));
        } else {
            endWorkerPdfPTable.addCell(new Phrase(endWorker.toString(), arialRegular9Dark));
        }
        detailData.addCell(endWorkerPdfPTable);
        document.add(detailData);

        return "Order" + entity.getField("number");
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.order.report.title", locale));
    }
}
