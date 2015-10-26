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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
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
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved offer! (missing id)");

        Long palletNumberId = Long.valueOf(model.get("id").toString());

        Entity palletNumber = palletNumbersService.getPalletNumber(palletNumberId);

        if (palletNumber != null) {
            String number = palletNumber.getStringField(PalletNumberFields.NUMBER);

            addPalletNumber(document, number);
        }

        return translationService.translate("basic.palletNumber.report.fileName", locale, palletNumberId.toString());
    }

    private void addPalletNumber(final Document document, final String number) throws DocumentException {
        LineSeparator lineSeparator = new LineSeparator(1, 100f, ColorUtils.getLineDarkColor(), Element.ALIGN_LEFT, 0);
        Paragraph numberParagraph = new Paragraph(new Phrase(number, FontUtils.getDejavuBold70Dark()));

        numberParagraph.setAlignment(Element.ALIGN_CENTER);

        numberParagraph.setSpacingBefore(70F);
        numberParagraph.setSpacingAfter(180F);

        document.add(Chunk.NEWLINE);
        document.add(numberParagraph);

        document.add(lineSeparator);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("basic.palletNumber.report.title", locale));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

}
