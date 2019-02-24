/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.orders.print;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "ordersOrderReportPdf")
public class OrderReportPdf extends ReportPdfView {

    private static final String L_TRANSLATION_PATH = "orders.order.report.%s.label";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    private Entity orderEntity;

	@Override
	protected void prepareWriter(final Map<String, Object> model, final PdfWriter writer, final HttpServletRequest request)
			throws DocumentException {
		super.prepareWriter(model, writer, request);

		Long orderId = Long.valueOf(model.get("id").toString());

		orderEntity = getOrderEntity(orderId);
	}

	private Entity getOrderEntity(final Long orderId) {
		return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
	}

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("orders.order.report.order", locale, orderEntity.getStringField(OrderFields.NUMBER)));
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale, final PdfWriter writer)
            throws DocumentException, IOException {
        pdfHelper.addDocumentHeader(document, "", translationService.translate("orders.order.report.order", locale, orderEntity.getStringField(OrderFields.NUMBER)), "", new Date());

		Long orderId = Long.valueOf(model.get("id").toString());

		Entity order = getOrderEntity(orderId);

        addHeaderTable(document, order, locale);
        addPlannedDateTable(document, order, locale);
        addProductQuantityTable(document, order, locale);
        addOwnTechnologyTable(document, order, locale);
        addTechnologyTable(document, order, locale);
        addMasterOrderTable(document, order, locale);

        return translationService.translate("orders.order.report.fileName", locale, order.getStringField(OrderFields.NUMBER));
    }


    private void addHeaderTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(3);

        List<HeaderPair> headerValues = getDocumentHeaderTableContent(locale);

        for (HeaderPair pair : headerValues) {
            if (pair.getValue() != null && !pair.getValue().isEmpty()) {
                pdfHelper.addTableCellAsOneColumnTable(table, pair.getLabel(), pair.getValue());
            } else {
                pdfHelper.addTableCellAsOneColumnTable(table, StringUtils.EMPTY, StringUtils.EMPTY);
            }
        }

        table.setSpacingAfter(20);

        document.add(table);
    }

	private List<HeaderPair> getDocumentHeaderTableContent(final Locale locale) {
		List<HeaderPair> headerValues = Lists.newLinkedList();

		headerValues.add(new HeaderPair(translationService.translate(String.format(L_TRANSLATION_PATH, OrderFields.NUMBER), locale), orderEntity.getStringField(OrderFields.NUMBER)));
		headerValues.add(new HeaderPair(translationService.translate(String.format(L_TRANSLATION_PATH, OrderFields.NAME), locale), orderEntity.getStringField(OrderFields.NAME)));
		headerValues.add(new HeaderPair(translationService.translate(String.format(L_TRANSLATION_PATH, OrderFields.STATE), locale), translationService.translate("orders.order.state.value." + orderEntity.getStringField(OrderFields.STATE), locale)));

		Entity productEntity = orderEntity.getBelongsToField(OrderFields.PRODUCT);

		headerValues.add(new HeaderPair(translationService.translate(String.format(L_TRANSLATION_PATH, OrderFields.PRODUCT), locale), productEntity == null ? "" : productEntity.getStringField(ProductFields.NUMBER)));

		headerValues.add(new HeaderPair(translationService.translate(String.format(L_TRANSLATION_PATH, OrderFields.DATE_FROM), locale), DateUtils.toDateTimeString(orderEntity.getDateField(OrderFields.DATE_FROM))));
		headerValues.add(new HeaderPair(translationService.translate(String.format(L_TRANSLATION_PATH, OrderFields.DATE_TO), locale), DateUtils.toDateTimeString(orderEntity.getDateField(OrderFields.DATE_TO))));

		return headerValues;
	}

	private void addPlannedDateTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
		Map<String, String> values = Maps.newLinkedHashMap();

		values.put("plannedDateFrom", DateUtils.toDateTimeString(order.getDateField(OrderFields.DATE_FROM)));
		values.put("plannedDateTo", DateUtils.toDateTimeString(order.getDateField(OrderFields.DATE_TO)));

		addTableToDocument(document, locale, "orders.order.report.date.label", values);
	}

	private void addProductQuantityTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
		Map<String, String> values = Maps.newLinkedHashMap();

		String unit = order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT);
		String plannedQuantity = order.getField(OrderFields.PLANNED_QUANTITY) == null ? " - " : numberService.format(orderEntity.getDecimalField(OrderFields.PLANNED_QUANTITY)) + " " + unit;
		String doneQuantity = order.getField(OrderFields.DONE_QUANTITY) == null ? " - " : numberService.format(orderEntity.getDecimalField(OrderFields.DONE_QUANTITY)) + " " + unit;

		values.put("plannedQuantity", plannedQuantity);
		values.put("doneQuantity", doneQuantity);

		addTableToDocument(document, locale, "orders.order.report.productQuantity.label", values);
	}

    private void addOwnTechnologyTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Map<String, String> values = Maps.newLinkedHashMap();

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (!Objects.isNull(technology)) {
			values.put("technologyNumber", technology.getStringField(TechnologyFields.NUMBER));
			values.put("technologyName", technology.getStringField(TechnologyFields.NAME));

			String tableLabelKey = "orders.order.report.technology.own";

			if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderEntity.getStringField(OrderFields.ORDER_TYPE))) {
				tableLabelKey = "orders.order.report.technology.label";
			}

			addTableToDocument(document, locale, tableLabelKey, values);
		}
    }

    private void addTechnologyTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(order.getStringField(OrderFields.ORDER_TYPE))) {
            Map<String, String> values = Maps.newLinkedHashMap();

            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

            if (!Objects.isNull(technology)) {
				values.put("technologyNumber", technology.getStringField(TechnologyFields.NUMBER));
				values.put("technologyName", technology.getStringField(TechnologyFields.NAME));

				addTableToDocument(document, locale, "orders.order.report.technologyPrototype.label", values);
			}
        }
    }

    private void addMasterOrderTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Entity masterOrderEntity = order.getBelongsToField("masterOrder");

        if (!Objects.isNull(masterOrderEntity)) {
            Map<String, String> values = Maps.newLinkedHashMap();

            values.put("masterOrderNumber", masterOrderEntity.getStringField(OrderFields.NUMBER));
            values.put("masterOrderName", masterOrderEntity.getStringField(OrderFields.NAME));

            addTableToDocument(document, locale, "orders.order.report.masterOrder.label", values);
        }
    }

    private void addTableToDocument(final Document document, final Locale locale, final String headerKey, final Map<String, String> values) throws DocumentException {
        document.add(new Paragraph(translationService.translate(headerKey, locale), FontUtils.getDejavuBold10Dark()));

        Map<String, HeaderAlignment> headerValues = Maps.newLinkedHashMap();

        for (String key : values.keySet()) {
            headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, key), locale), HeaderAlignment.LEFT);
        }

        PdfPTable table = pdfHelper.createTableWithHeader(values.size(), Lists.newArrayList(headerValues.keySet()), false, headerValues);

        table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        table.setHeaderRows(1);

        for (String value : values.values()) {
            table.addCell(createCell(value, Element.ALIGN_LEFT));
        }

        table.setSpacingAfter(20);

        document.add(table);
    }

    private PdfPCell createCell(final String content, final int alignment) {
        PdfPCell cell = new PdfPCell();

        float border = 0.2f;

        cell.setPhrase(new Phrase(content, FontUtils.getDejavuRegular7Dark()));
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(border);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell.disableBorderSide(PdfPCell.LEFT);
        cell.setPadding(5);

        return cell;
    }

}
