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
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.PalletNumberGenerator;
import com.qcadoo.mes.basic.PalletNumbersService;
import com.qcadoo.mes.basic.constants.PalletNumberHelperFields;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "palletNumberHelperReportPdf")
public class PalletNumberHelperReportPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private PalletNumbersService palletNumbersService;

    @Autowired
    private PalletNumberGenerator palletNumberGenerator;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved offer! (missing id)");

        Long palletNumberHelperId = Long.valueOf(model.get("id").toString());

        Entity palletNumberHelper = palletNumbersService.getPalletNumberHelper(palletNumberHelperId);

        if (palletNumberHelper != null) {
            String firstNumber = palletNumberHelper.getStringField(PalletNumberHelperFields.FIRST_NUMBER);
            Integer quantity = palletNumberHelper.getIntegerField(PalletNumberHelperFields.QUANTITY);

            List<String> numbers = palletNumberGenerator.list(firstNumber, quantity);

            addPalletNumbers(document, numbers);
        }

        return translationService.translate("basic.palletNumberHelper.report.fileName", locale, palletNumberHelperId.toString());
    }

    private void addPalletNumbers(final Document document, final List<String> numbers) throws DocumentException {
        int i = 0;

        for (String number : numbers) {
            if (i % 2 == 0) {
                Paragraph firstNumberParagraph = new Paragraph(new Phrase(number, FontUtils.getDejavuBold70Dark()));

                firstNumberParagraph.setAlignment(Element.ALIGN_CENTER);



                //firstNumberParagraph.setSpacingBefore(70F);
                firstNumberParagraph.setSpacingAfter(180F);

                if(i==0) {
                    Paragraph p = new Paragraph(new Phrase("\n"));
                    p.setSpacingAfter(80f);
                    document.add(p);
                }
                document.add(firstNumberParagraph);

                LineSeparator lineSeparator = new LineSeparator(1, 100f, ColorUtils.getLineDarkColor(), Element.ALIGN_LEFT, 0);

                document.add(lineSeparator);
            }
            if (i % 2 == 1) {
                Paragraph secondNumberParagraph = new Paragraph(new Phrase(number, FontUtils.getDejavuBold70Dark()));

                secondNumberParagraph.setAlignment(Element.ALIGN_CENTER);

                secondNumberParagraph.setSpacingBefore(100f);



                document.add(secondNumberParagraph);
                if(i<numbers.size()-1){
                    document.newPage();
                    document.add(new Phrase("\n"));
                }
            }

            i++;
        }
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
