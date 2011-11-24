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
package com.qcadoo.mes.materialFlow;

import static com.qcadoo.report.api.ReportUtil.sentFileAsAttachement;
import static com.qcadoo.report.api.ReportUtil.sentTranslatedFileName;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.xls.XlsUtil;

@Controller
public class MaterialFlowController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "materialFlow/materialsInStockAreas.pdf", method = RequestMethod.GET)
    public void materialsInStockAreasPdf(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS);
        Entity materialsInStockAreas = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName((Date) materialsInStockAreas.getField("time"),
                translationService.translate("materialFlow.materialFlow.report.fileName", locale), "", PdfUtil.PDF_EXTENSION,
                response);
        sentFileAsAttachement(materialsInStockAreas.getStringField("fileName") + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "materialFlow/materialsInStockAreas.xls", method = RequestMethod.GET)
    public void materialsInStockAreasXls(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS);
        Entity materialFlow = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName((Date) materialFlow.getField("time"),
                translationService.translate("materialFlow.materialFlow.report.fileName", locale), "", XlsUtil.XLS_EXTENSION,
                response);
        sentFileAsAttachement(materialFlow.getStringField("fileName") + XlsUtil.XLS_EXTENSION, ReportUtil.XLS_CONTENT_TYPE,
                response);
    }
}
