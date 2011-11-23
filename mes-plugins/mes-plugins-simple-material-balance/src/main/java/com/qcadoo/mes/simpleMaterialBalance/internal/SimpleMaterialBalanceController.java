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
package com.qcadoo.mes.simpleMaterialBalance.internal;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.xls.XlsUtil;

@Controller
public class SimpleMaterialBalanceController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "simpleMaterialBalance/simpleMaterialBalance.pdf", method = RequestMethod.GET)
    public void simpleMaterialBalancePdf(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE);
        Entity simpleMaterialBalance = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) simpleMaterialBalance.getField("date"),
                translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.fileName", locale), "",
                PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(simpleMaterialBalance.getStringField("fileName") + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "simpleMaterialBalance/simpleMaterialBalance.xls", method = RequestMethod.GET)
    public void simpleMaterialBalanceXls(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE);
        Entity simpleMaterialBalance = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) simpleMaterialBalance.getField("date"),
                translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.fileName", locale), "",
                XlsUtil.XLS_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(simpleMaterialBalance.getStringField("fileName") + XlsUtil.XLS_EXTENSION,
                ReportUtil.XLS_CONTENT_TYPE, response);
    }

}
