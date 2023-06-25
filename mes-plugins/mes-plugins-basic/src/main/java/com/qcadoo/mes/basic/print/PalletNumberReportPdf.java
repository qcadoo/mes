/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.basic.print;

import com.lowagie.text.*;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.PalletNumbersService;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ColorUtils;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "palletNumberReportPdf")
public class PalletNumberReportPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private PalletNumbersService palletNumbersService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

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
            String number = palletNumber.getStringField(PalletNumberFields.NUMBER);

            addPalletNumber(document, writer, number);
        }

        return translationService.translate("basic.palletNumber.report.fileName", locale, palletNumberId.toString());
    }

    private void addPalletNumber(final Document document, final PdfWriter writer, final String number) throws DocumentException {
        Paragraph newLineParagraph = createNewLineParagraph(0F, 150F);
        Paragraph numberParagraph = createNumberParagraph(number, 0F, 40F);
        Image numberImage = createNumberImage(writer, number);
        Paragraph spacingParagraph = createNewLineParagraph(0F, 85F);
        LineSeparator lineSeparator = createLineSeparator();

        document.add(newLineParagraph);
        document.add(numberParagraph);
        document.add(numberImage);
        document.add(spacingParagraph);
        document.add(lineSeparator);
    }

    private static Paragraph createNewLineParagraph(final float spacingBefore, final float spacingAfter) {
        Paragraph newLineParagraph = new Paragraph(new Phrase("\n"));

        newLineParagraph.setSpacingBefore(spacingBefore);
        newLineParagraph.setSpacingAfter(spacingAfter);

        return newLineParagraph;
    }

    private static Paragraph createNumberParagraph(final String number, final float spacingBefore, final float spacingAfter) {
        Paragraph numberParagraph = new Paragraph(new Phrase(number, FontUtils.getDejavuBold140Dark()));

        numberParagraph.setAlignment(Element.ALIGN_CENTER);
        numberParagraph.setSpacingBefore(spacingBefore);
        numberParagraph.setSpacingAfter(spacingAfter);
        numberParagraph.setLeading(0, 0);

        return numberParagraph;
    }

    private Image createNumberImage(final PdfWriter writer, final String code) {
        Barcode128 code128 = new Barcode128();

        code128.setCode(code);
        code128.setBarHeight(50F);
        code128.setX(3F);
        code128.setSize(16F);
        code128.setFont(null);

        PdfContentByte cb = writer.getDirectContent();

        Image numberImage = code128.createImageWithBarcode(cb, null, null);

        numberImage.setAlignment(Element.ALIGN_CENTER);

        return numberImage;
    }

    private static LineSeparator createLineSeparator() {
        return new LineSeparator(1, 100F, ColorUtils.getLineDarkColor(), Element.ALIGN_LEFT, 0);
    }

}
