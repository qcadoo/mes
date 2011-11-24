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
package com.qcadoo.mes.materialRequirements.internal;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialRequirements.internal.constants.MaterialRequirementsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.xls.XlsUtil;

@Controller
public class MaterialRequirementController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "materialRequirements/materialRequirement.pdf", method = RequestMethod.GET)
    public void materialRequirementPdf(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT);
        Entity materialRequirement = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) materialRequirement.getField("date"),
                translationService.translate("materialRequirements.materialRequirement.report.fileName", locale), "",
                PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(materialRequirement.getStringField("fileName") + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "materialRequirements/materialRequirement.xls", method = RequestMethod.GET)
    public void materialRequirementXls(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT);
        Entity materialRequirement = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) materialRequirement.getField("date"),
                translationService.translate("materialRequirements.materialRequirement.report.fileName", locale), "",
                XlsUtil.XLS_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(materialRequirement.getStringField("fileName") + XlsUtil.XLS_EXTENSION,
                ReportUtil.XLS_CONTENT_TYPE, response);
    }

}
