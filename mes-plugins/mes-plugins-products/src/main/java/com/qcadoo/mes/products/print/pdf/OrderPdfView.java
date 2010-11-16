/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.products.print.pdf;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;
import com.qcadoo.mes.products.print.pdf.util.TableBorderEvent;

public final class OrderPdfView extends ProductsPdfView {

    @Autowired
    private SecurityService securityService;

    @Override
    protected String addContent(final Document document, final DefaultEntity entity, final Locale locale)
            throws DocumentException, IOException {
        String documentTitle = getTranslationService().translate("products.order.report.order", locale);
        String documentAuthor = getTranslationService().translate("products.order.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity, documentTitle, documentAuthor, new Date(), user);
        addMainTable(document, entity, locale);
        addDetailTable(document, entity, locale);
        return "Order" + entity.getField("number");
    }

    private void addMainTable(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        PdfPTable mainData = createMainTable();
        PdfUtil.addTableCellAsTable(mainData, getTranslationService().translate("products.order.number.label", locale),
                entity.getField("number"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(mainData, getTranslationService().translate("products.order.dateFrom.label", locale),
                entity.getField("dateFrom"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(mainData, getTranslationService().translate("products.order.name.label", locale),
                entity.getField("name"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(mainData, getTranslationService().translate("products.order.dateTo.label", locale),
                entity.getField("dateTo"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        Entity product = (Entity) entity.getField("product");
        if (product == null) {
            PdfUtil.addTableCellAsTable(mainData, getTranslationService().translate("products.order.product.label", locale),
                    null, "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        } else {
            PdfUtil.addTableCellAsTable(mainData, getTranslationService().translate("products.order.product.label", locale),
                    product.getField("name"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        }
        PdfUtil.addTableCellAsTable(mainData, getTranslationService().translate("products.order.state.label", locale),
                entity.getField("state"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        document.add(mainData);
    }

    private PdfPTable createMainTable() {
        PdfPTable mainData = new PdfPTable(2);
        mainData.setWidthPercentage(100f);
        mainData.setSpacingBefore(20);
        mainData.getDefaultCell().setBackgroundColor(PdfUtil.getBackgroundColor());
        mainData.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        mainData.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        mainData.getDefaultCell().setPadding(8.0f);
        mainData.setTableEvent(new TableBorderEvent());
        return mainData;
    }

    private void addDetailTable(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        PdfPTable detailData = createDetailTable();
        PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.machine.label", locale),
                entity.getField("machine"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        PdfUtil.addTableCellAsTable(detailData,
                getTranslationService().translate("products.order.effectiveDateFrom.label", locale),
                entity.getField("effectiveDateFrom"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        PdfUtil.addTableCellAsTable(detailData,
                getTranslationService().translate("products.order.plannedQuantity.label", locale),
                entity.getField("plannedQuantity"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark(), df);
        PdfUtil.addTableCellAsTable(detailData,
                getTranslationService().translate("products.order.effectiveDateTo.label", locale),
                entity.getField("effectiveDateTo"),
                getTranslationService().translate("products.order.report.effectiveDateToState", locale),
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.doneQuantity.label", locale),
                entity.getField("doneQuantity"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark(), df);
        PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.startWorker.label", locale),
                entity.getField("startWorker"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        Entity instruction = (Entity) entity.getField("instruction");
        if (instruction == null) {
            PdfUtil.addTableCellAsTable(
                    detailData,
                    getTranslationService().translate("products.orderDetailsView.mainWindow.orderDetailsForm.instruction.label",
                            locale), null, "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        } else {
            PdfUtil.addTableCellAsTable(
                    detailData,
                    getTranslationService().translate("products.orderDetailsView.mainWindow.orderDetailsForm.instruction.label",
                            locale), instruction.getField("name"), "", PdfUtil.getArialBold9Dark(),
                    PdfUtil.getArialRegular9Dark());
        }
        PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.endWorker.label", locale),
                entity.getField("endWorker"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        document.add(detailData);
    }

    private PdfPTable createDetailTable() {
        PdfPTable detailData = new PdfPTable(2);
        detailData.setWidthPercentage(100f);
        detailData.setSpacingBefore(5);
        detailData.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        detailData.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        detailData.getDefaultCell().setPadding(5.0f);
        return detailData;
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("products.order.report.title", locale));
    }
}
