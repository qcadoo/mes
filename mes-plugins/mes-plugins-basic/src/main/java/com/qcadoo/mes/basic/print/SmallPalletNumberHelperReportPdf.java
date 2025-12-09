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

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.PalletNumbersService;
import com.qcadoo.mes.basic.constants.PalletNumberHelperFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "smallPalletNumberHelperReportPdf")
public class SmallPalletNumberHelperReportPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PalletNumbersService palletNumbersService;

    @Autowired
    private PdfHelper pdfHelper;

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
        checkState(Objects.nonNull(model.get("id")), "Unable to generate report for unsaved pallet number! (missing id)");

        Long palletNumberHelperId = Long.valueOf(model.get("id").toString());

        Entity palletNumberHelper = palletNumbersService.getPalletNumberHelper(palletNumberHelperId);

        if (Objects.nonNull(palletNumberHelper)) {
            List<Entity> palletNumbers = palletNumberHelper.getManyToManyField(PalletNumberHelperFields.PALLET_NUMBERS);
            palletNumbersService.setPalletNumbersPrinted(palletNumbers);

            addPalletNumbers(document, writer, palletNumbersService.getNumbers(palletNumbers));
        }

        return translationService.translate("basic.palletNumberHelper.report.fileName", locale, palletNumberHelperId.toString());
    }

    private void addPalletNumbers(final Document document, final PdfWriter writer, final List<String> numbers) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(2);

        table.setTableEvent(null);

        int index = 0;

        for (String number : numbers) {
            PdfPCell cell = new PdfPCell();

            cell.setFixedHeight(165F);

            cell.addElement(createBarcodeTable(writer, number));

            table.addCell(cell);

            index++;

            if (index % 8 == 0) {
                document.add(table);

                if (index < numbers.size()) {
                    document.add(Chunk.NEXTPAGE);

                    table = pdfHelper.createPanelTable(2);

                    table.setTableEvent(null);
                }
            } else if (index == numbers.size()) {
                table.completeRow();

                document.add(table);
            }
        }
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
