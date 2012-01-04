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
import java.util.Map;
import java.util.Map.Entry;

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
import com.qcadoo.mes.technologies.print.ReportDataService;
import com.qcadoo.mes.workPlans.util.EntityOperationInPairNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.Pair;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public class WorkPlanPdfService extends PdfDocumentService {

    private static final String DATE_FIELD = "date";

    private static final String DATE_TO_FIELD = "dateTo";

    private static final String PLANNED_QUANTITY_FIELD = "plannedQuantity";

    private static final String SURNAME_FIELD = "surname";

    private static final String TYPE_FIELD = "type";

    private static final String UNIT_FIELD = "unit";

    private static final String PRODUCT_FIELD = "product";

    private static final String OPERATION_FIELD = "operation";

    private static final String NAME_FIELD = "name";

    private static final String NUMBER_FIELD = "number";

    private static final SimpleDateFormat D_F = new SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.getDefault());

    private final int[] defaultWorkPlanColumnWidth = new int[] { 20, 20, 20, 13, 13, 13 };

    private final int[] defaultWorkPlanOperationColumnWidth = new int[] { 10, 21, 23, 23, 23 };

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportDataService reportDataService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        addSeries(document, entity, locale);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("workPlans.workPlan.report.title", locale);
    }

    private void addSeries(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(3);
        boolean firstPage = true;
        String type = entity.getStringField(TYPE_FIELD);
        for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry : reportDataService
                .prepareOperationSeries(entity, type).entrySet()) {
            if (!firstPage) {
                document.newPage();
            }

            List<Entity> orders = new ArrayList<Entity>();

            BigDecimal totalQuantity = createUniqueOrdersList(orders, entry);

            PdfPTable orderTable = PdfUtil.createTableWithHeader(6, prepareOrderHeader(document, entity, locale), false,
                    defaultWorkPlanColumnWidth);
            addOrderSeries(orderTable, orders, decimalFormat);
            document.add(orderTable);
            document.add(Chunk.NEWLINE);

            document.add(prepareTitle(totalQuantity, entry, locale, type));

            addOperationSeries(entry, document, decimalFormat, locale);
            firstPage = false;
        }
    }

    private void addOperationSeries(
            final Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry,
            final Document document, final DecimalFormat decimalFormat, final Locale locale) throws DocumentException {
        PdfPTable table = PdfUtil.createTableWithHeader(5, prepareOperationHeader(locale), false,
                defaultWorkPlanOperationColumnWidth);

        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operationMap = SortUtil
                .sortMapUsingComparator(entry.getValue(), new EntityOperationInPairNumberComparator());

        for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entryComponent : operationMap
                .entrySet()) {

            Pair<Entity, Entity> entryPair = entryComponent.getKey();
            Entity operation = (Entity) entryPair.getKey().getField(OPERATION_FIELD);
            table.addCell(new Phrase(operation.getField(NUMBER_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(operation.getField(NAME_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entryPair.getValue().getField(NUMBER_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            addProductSeries(table, entryComponent.getValue().getValue(), decimalFormat);
            addProductSeries(table, entryComponent.getValue().getKey(), decimalFormat);
        }
        document.add(table);
    }

    private void addProductSeries(final PdfPTable table, final Map<Entity, BigDecimal> productsQuantity, final DecimalFormat df) {
        StringBuilder products = new StringBuilder();
        for (Entry<Entity, BigDecimal> entry : productsQuantity.entrySet()) {
            products.append(entry.getKey().getField(NUMBER_FIELD).toString() + " "
                    + entry.getKey().getField(NAME_FIELD).toString() + " x " + df.format(entry.getValue()) + " ["
                    + (entry.getKey().getField(UNIT_FIELD) == null ? "" : entry.getKey().getField(UNIT_FIELD).toString())
                    + "] \n\n");

        }
        table.addCell(new Phrase(products.toString(), PdfUtil.getArialRegular9Dark()));
    }

    private Paragraph prepareTitle(final BigDecimal totalQuantity,
            final Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry,
            final Locale locale, final String type) {
        Paragraph title = new Paragraph();
        if ("machine".equals(type)) {
            Entity machine = entry.getKey();
            title.add(new Phrase(translationService.translate("workPlans.workPlan.report.paragrah3", locale), PdfUtil
                    .getArialBold11Light()));
            String name = "";
            if (machine != null) {
                name = machine.getField(NAME_FIELD).toString();
            }
            title.add(new Phrase(" " + name, PdfUtil.getArialBold19Dark()));
        } else if ("worker".equals(type)) {
            // TODO mici there's no staff field anymore.
            Entity staff = entry.getKey();
            title.add(new Phrase(translationService.translate("workPlans.workPlan.report.paragrah2", locale), PdfUtil
                    .getArialBold11Light()));
            String name = "";
            if (staff != null) {
                name = staff.getField(NAME_FIELD) + " " + staff.getField(SURNAME_FIELD);
            }
            title.add(new Phrase(" " + name, PdfUtil.getArialBold19Dark()));
        } else if (PRODUCT_FIELD.equals(type)) {
            Entity product = entry.getKey();
            title.add(new Phrase(translationService.translate("workPlans.workPlan.report.paragrah4", locale), PdfUtil
                    .getArialBold11Light()));
            title.add(new Phrase(" " + totalQuantity + " x " + product.getField(NAME_FIELD), PdfUtil.getArialBold19Dark()));
        }
        return title;
    }

    private BigDecimal createUniqueOrdersList(final List<Entity> orders,
            final Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry) {
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (Pair<Entity, Entity> pair : entry.getValue().keySet()) {
            if (!orders.contains(pair.getValue())) {
                totalQuantity = totalQuantity.add((BigDecimal) pair.getValue().getField(PLANNED_QUANTITY_FIELD));
                orders.add(pair.getValue());
            }
        }
        return totalQuantity;
    }

    private void addOrderSeries(final PdfPTable table, final List<Entity> orders, final DecimalFormat df)
            throws DocumentException {
        Collections.sort(orders, new EntityNumberComparator());
        for (Entity order : orders) {
            table.addCell(new Phrase(order.getField(NUMBER_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField(NAME_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            Entity product = (Entity) order.getField(PRODUCT_FIELD);
            if (product == null) {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase(product.getField(NAME_FIELD).toString(), PdfUtil.getArialRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            BigDecimal plannedQuantity = (BigDecimal) order.getField(PLANNED_QUANTITY_FIELD);
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            table.addCell(new Phrase(df.format(plannedQuantity), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            if (product == null) {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            } else {
                Object unit = product.getField(UNIT_FIELD);
                if (unit == null) {
                    table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                } else {
                    table.addCell(new Phrase(unit.toString(), PdfUtil.getArialRegular9Dark()));
                }
            }
            String formattedDateTo = "---";
            if (order.getField(DATE_TO_FIELD) != null) {
                formattedDateTo = D_F.format((Date) order.getField(DATE_TO_FIELD));
            }
            table.addCell(new Phrase(formattedDateTo, PdfUtil.getArialRegular9Dark()));
        }
    }

    private List<String> prepareOrderHeader(final Document document, final Entity entity, final Locale locale)
            throws DocumentException {
        String documenTitle = translationService.translate("workPlans.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, entity.getField(NAME_FIELD).toString(), documenTitle, documentAuthor,
                (Date) entity.getField(DATE_FIELD), securityService.getCurrentUserName());
        // document.add(generateBarcode(entity.getField("name").toString()));
        document.add(new Paragraph(translationService.translate("workPlans.workPlan.report.paragrah", locale), PdfUtil
                .getArialBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("orders.order.number.label", locale));
        orderHeader.add(translationService.translate("orders.order.name.label", locale));
        orderHeader.add(translationService.translate("orders.order.product.label", locale));
        orderHeader.add(translationService.translate("orders.order.plannedQuantity.label", locale));
        orderHeader.add(translationService.translate("basic.product.unit.label", locale));
        orderHeader.add(translationService.translate("orders.order.dateTo.label", locale));
        return orderHeader;
    }

    private List<String> prepareOperationHeader(final Locale locale) {
        List<String> operationHeader = new ArrayList<String>();
        operationHeader.add(translationService.translate("technologies.operation.number.label", locale));
        operationHeader.add(translationService.translate("technologies.operation.name.label", locale));
        operationHeader.add(translationService.translate("workPlans.workPlan.report.operationTable.order.column", locale));
        operationHeader.add(translationService.translate("workPlans.workPlan.report.operationTable.productsOut.column", locale));
        operationHeader.add(translationService.translate("workPlans.workPlan.report.operationTable.productsIn.column", locale));
        return operationHeader;
    }
}
