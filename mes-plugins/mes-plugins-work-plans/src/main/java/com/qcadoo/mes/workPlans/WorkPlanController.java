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
package com.qcadoo.mes.workPlans;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.xls.XlsUtil;

@Controller
public class WorkPlanController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "workPlans/workPlanForWorker.pdf", method = RequestMethod.GET)
    public void workPlanForWorkerPdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) workPlan.getField("date"),
                translationService.translate("workPlans.workPlan.report.fileName", locale),
                translationService.translate("workPlans.workPlan.report.fileName.suffix.forWorker", locale),
                PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(workPlan.getStringField("fileName") + "_for_worker" + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "workPlans/workPlanForProduct.pdf", method = RequestMethod.GET)
    public void workPlanForProductPdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) workPlan.getField("date"),
                translationService.translate("workPlans.workPlan.report.fileName", locale),
                translationService.translate("workPlans.workPlan.report.fileName.suffix.forProduct", locale),
                PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(workPlan.getStringField("fileName") + "_for_product" + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "workPlans/workPlanForMachine.pdf", method = RequestMethod.GET)
    public void workPlanForMachinePdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) workPlan.getField("date"),
                translationService.translate("workPlans.workPlan.report.fileName", locale),
                translationService.translate("workPlans.workPlan.report.fileName.suffix.forMachine", locale),
                PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(workPlan.getStringField("fileName") + "_for_machine" + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "workPlans/workPlanForWorker.xls", method = RequestMethod.GET)
    public void workPlanForWorkerXls(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) workPlan.getField("date"),
                translationService.translate("workPlans.workPlan.report.fileName", locale),
                translationService.translate("workPlans.workPlan.report.fileName.suffix.forWorker", locale),
                XlsUtil.XLS_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(workPlan.getStringField("fileName") + "_for_worker" + XlsUtil.XLS_EXTENSION,
                ReportUtil.XLS_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "workPlans/workPlanForProduct.xls", method = RequestMethod.GET)
    public void workPlanForProductXls(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) workPlan.getField("date"),
                translationService.translate("workPlans.workPlan.report.fileName", locale),
                translationService.translate("workPlans.workPlan.report.fileName.suffix.forProduct", locale),
                XlsUtil.XLS_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(workPlan.getStringField("fileName") + "_for_product" + XlsUtil.XLS_EXTENSION,
                ReportUtil.XLS_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "workPlans/workPlanForMachine.xls", method = RequestMethod.GET)
    public void workPlanForMachineXls(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) workPlan.getField("date"),
                translationService.translate("workPlans.workPlan.report.fileName", locale),
                translationService.translate("workPlans.workPlan.report.fileName.suffix.forMachine", locale),
                XlsUtil.XLS_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(workPlan.getStringField("fileName") + "_for_machine" + XlsUtil.XLS_EXTENSION,
                ReportUtil.XLS_CONTENT_TYPE, response);
    }

}
