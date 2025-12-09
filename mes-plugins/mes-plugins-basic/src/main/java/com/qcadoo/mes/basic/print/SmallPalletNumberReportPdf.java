/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.print;

import com.google.common.collect.Lists;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.PalletNumbersService;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "smallPalletNumberReportPdf")
public class SmallPalletNumberReportPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private PalletNumbersService palletNumbersService;

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("basic.palletNumber.report.title", locale));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
                                final PdfWriter writer) throws DocumentException, IOException {
        checkState(Objects.nonNull(model.get("id")), "Unable to generate report for unsaved offer! (missing id)");

        Long palletNumberId = Long.valueOf(model.get("id").toString());

        Entity palletNumber = palletNumbersService.getPalletNumber(palletNumberId);

        if (Objects.nonNull(palletNumber)) {
            palletNumbersService.setPalletNumbersPrinted(Lists.newArrayList(palletNumber));
            String number = palletNumber.getStringField(PalletNumberFields.NUMBER);

            addPalletNumber(document, writer, number);
        }

        return translationService.translate("basic.palletNumber.report.fileName", locale, palletNumberId.toString());
    }

    private void addPalletNumber(final Document document, final PdfWriter writer, final String number) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(2);

        table.setTableEvent(null);

        PdfPCell cell = new PdfPCell();

        cell.setFixedHeight(165F);

        cell.addElement(createBarcodeTable(writer, number));

        table.addCell(cell);

        table.completeRow();

        document.add(table);
    }

    private PdfPTable createBarcodeTable(final PdfWriter writer, final String number) {
        PdfPTable barcodeTable = new PdfPTable(1);

        barcodeTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        barcodeTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        barcodeTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        barcodeTable.getDefaultCell().setPaddingTop(10F);
        barcodeTable.getDefaultCell().setPaddingBottom(10F);

        barcodeTable.addCell(number);

        barcodeTable.getDefaultCell().setPaddingTop(0F);
        barcodeTable.getDefaultCell().setPaddingLeft(30F);
        barcodeTable.getDefaultCell().setPaddingRight(30F);
        barcodeTable.getDefaultCell().setPaddingBottom(0F);

        barcodeTable.addCell(createNumberImage(writer, number));

        return barcodeTable;
    }

    private Image createNumberImage(final PdfWriter writer, final String code) {
        Barcode128 code128 = new Barcode128();

        code128.setCode(code);
        code128.setFont(null);

        PdfContentByte cb = writer.getDirectContent();

        Image numberImage = code128.createImageWithBarcode(cb, null, null);

        numberImage.setAlignment(Element.ALIGN_CENTER);

        return numberImage;
    }

}
