/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.products;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.xls.XlsUtil;

@Controller
public class ProductController {

    public static final String XLS_CONTENT_TYPE = "application/vnd.ms-excel";

    public static final String PDF_CONTENT_TYPE = "application/pdf";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    private void sentFileAsAttachement(final String path, final String contentType, final HttpServletResponse response) {
        try {
            File file = new File(path);
            InputStream input = new FileInputStream(file);

            response.setContentType(contentType);

            OutputStream output = response.getOutputStream();
            int bytes = IOUtils.copy(input, output);

            response.setContentLength(bytes);

            output.flush();

            IOUtils.closeQuietly(input);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void sentTranslatedFileName(final Entity entity, final String fileName, final String suffix, final String extension,
            final HttpServletResponse response) {
        Object date = entity.getField("date");
        String translatedFileName = fileName + "_" + PdfUtil.D_T_F.format((Date) date) + "_" + suffix + extension;
        response.setHeader("Content-disposition", "attachment; filename=" + translatedFileName);
    }

    @RequestMapping(value = "products/materialRequirement.pdf", method = RequestMethod.GET)
    public void materialRequirementPdf(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
        Entity materialRequirement = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(materialRequirement,
                translationService.translate("products.materialRequirement.report.fileName", locale), "", PdfUtil.PDF_EXTENSION,
                response);
        sentFileAsAttachement(materialRequirement.getStringField("fileName") + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "products/materialRequirement.xls", method = RequestMethod.GET)
    public void materialRequirementXls(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
        Entity materialRequirement = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(materialRequirement,
                translationService.translate("products.materialRequirement.report.fileName", locale), "", XlsUtil.XLS_EXTENSION,
                response);
        sentFileAsAttachement(materialRequirement.getStringField("fileName") + XlsUtil.XLS_EXTENSION, XLS_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "products/order.pdf", method = RequestMethod.GET)
    public ModelAndView orderPdf(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "order");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("orderPdfView");
        mav.addObject("value", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/workPlanForWorker.pdf", method = RequestMethod.GET)
    public void workPlanForWorkerPdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(workPlan, translationService.translate("products.workPlan.report.fileName", locale),
                translationService.translate("products.workPlan.report.fileName.suffix.forWorker", locale),
                PdfUtil.PDF_EXTENSION, response);
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_worker" + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForProduct.pdf", method = RequestMethod.GET)
    public void workPlanForProductPdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(workPlan, translationService.translate("products.workPlan.report.fileName", locale),
                translationService.translate("products.workPlan.report.fileName.suffix.forProduct", locale),
                PdfUtil.PDF_EXTENSION, response);
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_product" + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForMachine.pdf", method = RequestMethod.GET)
    public void workPlanForMachinePdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(workPlan, translationService.translate("products.workPlan.report.fileName", locale),
                translationService.translate("products.workPlan.report.fileName.suffix.forMachine", locale),
                PdfUtil.PDF_EXTENSION, response);
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_machine" + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForWorker.xls", method = RequestMethod.GET)
    public void workPlanForWorkerXls(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(workPlan, translationService.translate("products.workPlan.report.fileName", locale),
                translationService.translate("products.workPlan.report.fileName.suffix.forWorker", locale),
                XlsUtil.XLS_EXTENSION, response);
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_worker" + XlsUtil.XLS_EXTENSION, XLS_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForProduct.xls", method = RequestMethod.GET)
    public void workPlanForProductXls(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(workPlan, translationService.translate("products.workPlan.report.fileName", locale),
                translationService.translate("products.workPlan.report.fileName.suffix.forProduct", locale),
                XlsUtil.XLS_EXTENSION, response);
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_product" + XlsUtil.XLS_EXTENSION, XLS_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForMachine.xls", method = RequestMethod.GET)
    public void workPlanForMachineXls(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(workPlan, translationService.translate("products.workPlan.report.fileName", locale),
                translationService.translate("products.workPlan.report.fileName.suffix.forMachine", locale),
                XlsUtil.XLS_EXTENSION, response);
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_machine" + XlsUtil.XLS_EXTENSION, XLS_CONTENT_TYPE,
                response);
    }

}
