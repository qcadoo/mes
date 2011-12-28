/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.workPlans.print;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public class WorkPlanPdfService2 extends PdfDocumentService {

    private static final SimpleDateFormat D_F = new SimpleDateFormat(DateUtils.DATE_FORMAT);

    private final int[] defaultWorkPlanColumnWidth = new int[] { 15, 25, 20, 20, 20 };

    private final int[] defaultWorkPlanOperationColumnWidth = new int[] { 10, 21, 23, 23, 23 };

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Override
    public void buildPdfContent(Document document, Entity entity, Locale locale) throws DocumentException {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(3);

        addMainHeader(document, entity, locale);

        PdfPTable orderTable = PdfUtil.createTableWithHeader(5, prepareOrdersTableHeader(document, entity, locale), false,
                defaultWorkPlanColumnWidth);
        List<Entity> orders = entity.getManyToManyField("orders");
        addOrderSeries(orderTable, orders, decimalFormat);
        document.add(orderTable);
        document.add(Chunk.NEWLINE);
    }

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("workPlans.workPlan.report.title", locale);
    }

    void addOrderSeries(final PdfPTable table, final List<Entity> orders, final DecimalFormat df) throws DocumentException {
        Collections.sort(orders, new EntityNumberComparator());
        for (Entity order : orders) {
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));

            String unitString = "";

            Entity product = (Entity) order.getField("product");
            if (product != null) {
                String productString = product.getField("name").toString() + " (" + product.getField("number").toString() + ")";
                table.addCell(new Phrase(productString, PdfUtil.getArialRegular9Dark()));

                Object unit = product.getField("unit");
                if (unit != null) {
                    unitString = " " + unit.toString();
                }
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }

            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            String quantityString = df.format(plannedQuantity) + unitString;
            table.addCell(new Phrase(quantityString, PdfUtil.getArialRegular9Dark()));
            String formattedDateTo = "---";
            if (order.getField("dateTo") != null) {
                formattedDateTo = D_F.format((Date) order.getField("dateTo"));
            }
            table.addCell(new Phrase(formattedDateTo, PdfUtil.getArialRegular9Dark()));
        }
    }

    void addMainHeader(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = translationService.translate("workPlans.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, entity.getField("name").toString(), documenTitle, documentAuthor,
                (Date) entity.getField("date"), securityService.getCurrentUserName());
    }

    List<String> prepareOrdersTableHeader(final Document document, final Entity entity, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(translationService.translate("workPlans.workPlan.report.ordersTable", locale), PdfUtil
                .getArialBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("orders.order.number.label", locale));
        orderHeader.add(translationService.translate("orders.order.name.label", locale));
        orderHeader.add(translationService.translate("workPlans.workPlan.report.colums.product", locale));
        orderHeader.add(translationService.translate("orders.order.plannedQuantity.label", locale));
        orderHeader.add(translationService.translate("orders.order.dateTo.label", locale));
        return orderHeader;
    }

    String getOperationsTitle(Entity workPlan) {
        String type = workPlan.getStringField("type");

        if (WorkPlanType.ALL_OPERATIONS.getStringValue().equals(type)) {
            return "workPlans.workPlan.report.title.allOperations";
        } else if (WorkPlanType.BY_END_PRODUCT.getStringValue().equals(type)) {
            return "workPlans.workPlan.report.title.byEndProduct";
        }

        throw new IllegalStateException("unnexpected workPlan type");
    }
}
