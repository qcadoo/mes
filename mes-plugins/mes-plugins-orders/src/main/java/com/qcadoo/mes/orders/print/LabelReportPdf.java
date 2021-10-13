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
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.CountryFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "ordersLabelReportPdf")
public class LabelReportPdf extends ReportPdfView {

    private static final Logger LOG = LoggerFactory.getLogger(LabelReportPdf.class);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private Entity orderEntity;

    @Autowired
    private ParameterService parameterService;

    @Override
    protected Document newDocument() {
        Document doc = super.newDocument();
        Rectangle size = new Rectangle(Utilities.millimetersToPoints(104), Utilities.millimetersToPoints(160));
        doc.setPageSize(size);
        doc.setMargins(10, 10, 10, 10);

        return doc;
    }

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
        document.addTitle(translationService.translate("orders.label.report.label", locale,
                orderEntity.getStringField(OrderFields.NUMBER)));
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {

        Entity parameter = parameterService.getParameter();
        String fileName = parameter.getStringField(ParameterFields.LOGO);
        if (fileName != null) {
            try {
                Image img = Image.getInstance(fileName);
                if (img.getWidth() > 270 || img.getHeight() > 220) {
                    img.scaleToFit(270, 220);
                }
                img.setAlignment(Element.ALIGN_CENTER);

                document.add(img);
            } catch (IOException | DocumentException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        PdfPTable table = new PdfPTable(2);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setPaddingBottom(10);
        table.setWidths(new int[] { 1, 3 });
        table.setWidthPercentage(100f);
        table.setSpacingBefore(20);

        table.addCell(new Phrase(translationService.translate("orders.label.report.product.label", locale),
                FontUtils.getDejavuBold10Dark()));
        table.addCell(new Phrase(orderEntity.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.NAME),
                FontUtils.getDejavuRegular10Dark()));

        Entity company = orderEntity.getBelongsToField(OrderFields.COMPANY);
        if (company != null) {
            table.addCell(new Phrase(translationService.translate("orders.label.report.company.label", locale),
                    FontUtils.getDejavuBold10Dark()));
            table.addCell(new Phrase(company.getStringField(CompanyFields.NAME), FontUtils.getDejavuRegular10Dark()));
            Entity country = company.getBelongsToField(CompanyFields.COUNTRY);
            if (country != null) {
                table.addCell(new Phrase(translationService.translate("orders.label.report.country.label", locale),
                        FontUtils.getDejavuBold10Dark()));
                table.addCell(new Phrase(country.getStringField(CountryFields.COUNTRY), FontUtils.getDejavuRegular10Dark()));
            }
        }

        String description = orderEntity.getStringField(OrderFields.DESCRIPTION);
        if (StringUtils.isNotEmpty(description)) {
            table.addCell(new Phrase(translationService.translate("orders.label.report.description.label", locale),
                    FontUtils.getDejavuBold10Dark()));
            table.addCell(new Phrase(description, FontUtils.getDejavuRegular10Dark()));
        }

        document.add(table);

        return translationService.translate("orders.label.report.fileName", locale,
                orderEntity.getStringField(OrderFields.NUMBER));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

}
