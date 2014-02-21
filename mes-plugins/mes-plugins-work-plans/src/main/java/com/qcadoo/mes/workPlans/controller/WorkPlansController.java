/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.1
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
package com.qcadoo.mes.workPlans.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Controller
@RequestMapping("/rest/workplans")
public class WorkPlansController {

    private static final Logger LOG = LoggerFactory.getLogger(PdfDocumentService.class);

    private static final String PDF_EXT = "PDF";

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PdfHelper pdfHelper;

    @RequestMapping(value = "/printAtachment.html", method = RequestMethod.GET)
    public final void printAtachment(@RequestParam("id") final Long[] ids, HttpServletResponse response) {
        DataDefinition atachmentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_ATTACHMENT);
        Entity attachment = atachmentDD.get(ids[0]);

        if (PDF_EXT.equalsIgnoreCase(attachment.getStringField(TechnologyAttachmentFields.EXT))) {
            printPdfFile(attachment, response);
        } else {
            printImageToPdf(attachment, response);
        }

    }

    private void printImageToPdf(final Entity attachment, HttpServletResponse response) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            document.setPageSize(PageSize.A4);
            pdfHelper.addMetaData(document);
            pdfHelper.addImage(document, attachment.getStringField(TechnologyAttachmentFields.ATTACHMENT));
            document.close();
        } catch (Exception e) {
            LOG.error("Problem with printing document - " + e.getMessage());
            document.close();
            e.printStackTrace();
        }

    }

    private void printPdfFile(final Entity attachment, HttpServletResponse response) {
        InputStream is = fileService.getInputStream(attachment.getStringField(TechnologyAttachmentFields.ATTACHMENT));
        try {
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            LOG.error("Problem with printing document - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
