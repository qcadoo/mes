/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.products.print.DocumentService;
import com.qcadoo.mes.products.print.pdf.util.PdfPageNumbering;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

public abstract class PdfDocumentService extends DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(PdfDocumentService.class);

    private DecimalFormat decimalFormat;

    @Value("${windowsFonts}")
    private String windowsFontsPath;

    @Value("${macosFonts}")
    private String macosFontsPath;

    @Value("${linuxFonts}")
    private String linuxFontsPath;

    @Override
    public void generateDocument(final Entity entity, final Locale locale) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        try {
            decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
            decimalFormat.setMaximumFractionDigits(3);
            decimalFormat.setMinimumFractionDigits(3);
            FileOutputStream fileOutputStream = new FileOutputStream((String) entity.getField("fileName") + getSuffix(locale)
                    + PdfUtil.PDF_EXTENSION);
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
            writer.setPageEvent(new PdfPageNumbering(getTranslationService().translate("products.report.page", locale),
                    getTranslationService().translate("products.report.in", locale), PdfUtil.getFontsPath(windowsFontsPath,
                            macosFontsPath, linuxFontsPath)));
            document.setMargins(40, 40, 60, 60);
            buildPdfMetadata(document, locale);
            writer.createXmpMetadata();
            document.open();
            buildPdfContent(document, entity, locale);
            String text = getTranslationService().translate("products.report.endOfReport", locale);
            PdfUtil.addEndOfDocument(document, writer, text);
            document.close();
        } catch (DocumentException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            document.close();
            throw e;
        }
    }

    protected abstract void buildPdfMetadata(final Document document, final Locale locale);

    protected abstract void buildPdfContent(final Document document, final Entity entity, final Locale locale)
            throws DocumentException;

    public final DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

}
