/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.workPlans.pdf.document.component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Locale;

@Component
public class DocumentHeader {

    public static final String MSG_TITLE  = "workPlans.workPlan.report.title";
    public static final String MSG_AUTHOR = "qcadooReport.commons.generatedBy.label";

    private TranslationService translationService;
    private PdfHelper pdfHelper;

    @Autowired
    public DocumentHeader(TranslationService translationService, PdfHelper pdfHelper) {
        this.translationService = translationService;
        this.pdfHelper = pdfHelper;
    }

    public void print(Entity workPlan, Document document, Locale locale) throws DocumentException {
        pdfHelper.addDocumentHeader(document, name(workPlan), title(locale), author(locale), date(workPlan));
    }

    private Date date(Entity workPlan) {
        return workPlan.getDateField(WorkPlanFields.DATE);
    }

    private String name(Entity workPlan) {
        return workPlan.getStringField(WorkPlanFields.NAME);
    }

    private String author(Locale locale) {
        return translationService.translate(MSG_AUTHOR, locale);
    }

    private String title(Locale locale) {
        return translationService.translate(MSG_TITLE, locale);
    }

}
