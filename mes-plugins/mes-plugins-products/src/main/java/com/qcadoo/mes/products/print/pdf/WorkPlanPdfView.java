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

import java.io.IOException;
import java.util.Locale;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

public final class WorkPlanPdfView extends ProductsPdfView {

    @Override
    protected String addContent(final Document document, final DefaultEntity entity, final Locale locale, final PdfWriter writer)
            throws DocumentException, IOException {
        Object fileName = entity.getField("fileName");
        String fileNameWithoutPath = "";
        if (fileName != null && !"".equals(fileName.toString().trim())) {
            PdfUtil.copyPdf(document, writer, (String) fileName);
            fileNameWithoutPath = ((String) fileName).substring(((String) fileName).lastIndexOf("/") + 1);
        }
        return fileNameWithoutPath;
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("products.workPlan.report.title", locale));
    }

}
