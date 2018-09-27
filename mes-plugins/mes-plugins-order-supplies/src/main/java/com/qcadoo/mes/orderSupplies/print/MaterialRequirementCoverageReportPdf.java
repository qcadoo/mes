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
package com.qcadoo.mes.orderSupplies.print;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "materialRequirementCoverageReportPdf")
public class MaterialRequirementCoverageReportPdf extends ReportPdfView {

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private MaterialRequirementCoverageReportPdfService materialRequirementCoverageReportPdfService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

    @Override
    protected Document newDocument() {
        Document doc = super.newDocument();
        doc.setPageSize(PageSize.A4.rotate());

        return doc;
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved materialRequirementCoverage! (missing id)");

        String documentTitle = translationService.translate("orderSupplies.materialRequirementCoverage.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        Long materialRequirementCoverageId = Long.valueOf(model.get("id").toString());

        Entity materialRequirementCoverage = orderSuppliesService.getMaterialRequirementCoverage(materialRequirementCoverageId);

        materialRequirementCoverageReportPdfService.createHeaderTable(document, materialRequirementCoverage, locale);
        materialRequirementCoverageReportPdfService.createOptionsTable(document, materialRequirementCoverage, locale);
        materialRequirementCoverageReportPdfService.createProductsTable(document, materialRequirementCoverage, locale);

        return translationService.translate("orderSupplies.materialRequirementCoverage.report.generatedFileName", locale,
                materialRequirementCoverage.getStringField(MaterialRequirementCoverageFields.NUMBER),
                getStringFromDate(materialRequirementCoverage.getDateField("updateDate")));
    }

    private String getStringFromDate(final Date date) {
        return simpleDateFormat.format(date);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("orderSupplies.materialRequirementCoverage.report.title", locale));
    }

}
