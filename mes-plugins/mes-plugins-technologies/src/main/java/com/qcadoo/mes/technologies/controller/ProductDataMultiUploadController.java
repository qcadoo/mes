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
package com.qcadoo.mes.technologies.controller;

import com.google.common.io.Files;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.MultiUploadHelper;
import com.qcadoo.mes.technologies.constants.ProductDataAttachmentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.crud.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

@Controller
@RequestMapping("/productData")
public class ProductDataMultiUploadController {

    private static final Logger logger = LoggerFactory.getLogger(ProductDataMultiUploadController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CrudService crudController;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    private static final Integer L_SCALE = 2;

    @ResponseBody
    @RequestMapping(value = "/multiUploadFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        Long productDataId = Long.parseLong(request.getParameter("productDataId"));
        Entity productData = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_DATA).get(productDataId);


        Iterator<String> itr = request.getFileNames();
        MultipartFile mpf = null;

        while (itr.hasNext()) {
            mpf = request.getFile(itr.next());

            String path = "";

            try {
                path = fileService.upload(mpf);
            } catch (IOException e) {
                logger.error("Unable to upload attachment.", e);
            }

            createProductDataAttachment(productData, mpf, path);
        }
    }

    private void createProductDataAttachment(final Entity productData, final MultipartFile mpf, final String path) {
        DataDefinition productDataAttachmentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_DATA_ATTACHMENT);

        if (MultiUploadHelper.EXTS.contains(Files.getFileExtension(path).toUpperCase())) {
            Entity productDataAttachment = productDataAttachmentDD.create();

            BigDecimal fileSize = new BigDecimal(mpf.getSize(), numberService.getMathContext());
            BigDecimal divider = new BigDecimal(1024, numberService.getMathContext());
            BigDecimal size = fileSize.divide(divider, L_SCALE, BigDecimal.ROUND_HALF_UP);

            productDataAttachment.setField(ProductDataAttachmentFields.ATTACHMENT, path);
            productDataAttachment.setField(ProductDataAttachmentFields.NAME, mpf.getOriginalFilename());
            productDataAttachment.setField(ProductDataAttachmentFields.PRODUCT_DATA, productData);
            productDataAttachment.setField(ProductDataAttachmentFields.EXT, Files.getFileExtension(path));
            productDataAttachment.setField(ProductDataAttachmentFields.SIZE, size);

            productDataAttachmentDD.save(productDataAttachment);
        }
    }

    @RequestMapping(value = "/getAttachment.html", method = RequestMethod.GET)
    public final void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        /*
         * DataDefinition attachmentDD = dataDefinitionService.get(
         * TechnologiesConstants.PLUGIN_IDENTIFIER,
         * TechnologiesConstants.MODEL_TECHNOLOGY_ATTACHMENT); Entity attachment
         * = attachmentDD.get(ids[0]); InputStream is =
         * fileService.getInputStream(attachment
         * .getStringField(TechnologyAttachmentFields.ATTACHMENT));
         *
         * try { if (is == null) {
         * response.sendRedirect("/error.html?code=404"); }
         *
         * response.setHeader( "Content-disposition", "inline; filename=" +
         * attachment .getStringField(TechnologyAttachmentFields.NAME));
         * response.setContentType(fileService.getContentType(attachment
         * .getStringField(TechnologyAttachmentFields.ATTACHMENT)));
         *
         * int bytes = IOUtils.copy(is, response.getOutputStream());
         * response.setContentLength(bytes);
         *
         * response.flushBuffer();
         *
         * } catch (IOException e) {
         * logger.error("Unable to copy attachment file to response stream.",
         * e); }
         */
    }

}
