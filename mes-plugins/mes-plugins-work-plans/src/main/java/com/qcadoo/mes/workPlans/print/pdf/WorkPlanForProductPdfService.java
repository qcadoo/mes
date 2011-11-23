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
package com.qcadoo.mes.workPlans.print.pdf;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.qcadoo.mes.workPlans.print.WorkPlanReportDataService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;

@Service
public final class WorkPlanForProductPdfService extends PdfDocumentService {

    @Autowired
    private WorkPlanReportDataService workPlanReportDataService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        workPlanReportDataService.addSeries(document, entity, locale, "product");
    }

    @Override
    protected String getSuffix() {
        return "_for_product";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("workPlans.workPlan.report.title", locale);
    }
}
