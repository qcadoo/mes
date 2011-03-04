/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.products.print.pdf;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.mes.utils.pdf.ReportPdfView;
import com.qcadoo.model.api.Entity;

public final class OrderPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        Entity entity = (Entity) model.get("value");
        String documentTitle = getTranslationService().translate("products.order.report.order", locale);
        String documentAuthor = getTranslationService().translate("products.order.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity.getField("name").toString(), documentTitle, documentAuthor, new Date(), user);
        addMainTable(document, entity, locale);
        addDetailTable(document, entity, locale);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("products.order.report.fileName", locale) + "_" + entity.getField("number");
    }

    private void addMainTable(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        PdfPTable mainData = PdfUtil.createPanelTable(2);
        mainData.setSpacingBefore(20);
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
                getTranslationService().translate("products.order.state.value." + entity.getField("state"), locale), "",
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        document.add(mainData);
    }

    private void addDetailTable(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        PdfPTable detailData = createDetailTable();
        PdfUtil.addTableCellAsTable(detailData,
                getTranslationService().translate("products.order.effectiveDateFrom.label", locale),
                entity.getField("effectiveDateFrom"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        PdfUtil.addTableCellAsTable(detailData,
                getTranslationService().translate("products.order.plannedQuantity.label", locale),
                entity.getField("plannedQuantity"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark(),
                getDecimalFormat());
        PdfUtil.addTableCellAsTable(detailData,
                getTranslationService().translate("products.order.effectiveDateTo.label", locale),
                entity.getField("effectiveDateTo"),
                getTranslationService().translate("products.order.report.effectiveDateToState", locale),
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.doneQuantity.label", locale),
                entity.getField("doneQuantity"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark(),
                getDecimalFormat());
        PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.startWorker.label", locale),
                entity.getField("startWorker"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        Entity technology = (Entity) entity.getField("technology");
        if (technology == null) {
            PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.technology.label", locale),
                    null, "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        } else {
            PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.technology.label", locale),
                    technology.getField("name"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        }
        PdfUtil.addTableCellAsTable(detailData, getTranslationService().translate("products.order.endWorker.label", locale),
                entity.getField("endWorker"), "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
        PdfUtil.addTableCellAsTable(detailData, "", "", "", PdfUtil.getArialBold9Dark(), PdfUtil.getArialRegular9Dark());
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
