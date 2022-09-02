/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.technologies.controller;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.qcadoo.mes.basic.MultiUploadHelper;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import org.apache.commons.io.IOUtils;
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
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technologies")
public class TechnologyMultiUploadController {

    private static final Logger logger = LoggerFactory.getLogger(TechnologyMultiUploadController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    private static final Integer L_SCALE = 2;

    @ResponseBody
    @RequestMapping(value = "/multiUploadFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        Long technologyId = Long.parseLong(request.getParameter("techId"));

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        Iterator<String> itr = request.getFileNames();
        MultipartFile mpf;

        while (itr.hasNext()) {
            mpf = request.getFile(itr.next());

            String path = "";

            try {
                path = fileService.upload(mpf);
            } catch (IOException e) {
                logger.error("Unable to upload attachment.", e);
            }

            createTechnologyAttachment(technology, mpf, path);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/technologiesMultiUploadFiles", method = RequestMethod.POST)
    public void technologiesUpload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        List<Long> technologiesIds = Lists.newArrayList(request.getParameter("technologiesIds").split(",")).stream().map(Long::valueOf)
                .collect(Collectors.toList());

        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);

        for (Long technologyId : technologiesIds) {
            Entity technology = technologyDD.get(technologyId);

            Iterator<String> itr = request.getFileNames();
            MultipartFile mpf;

            while (itr.hasNext()) {
                mpf = request.getFile(itr.next());

                String path = "";

                try {
                    path = fileService.upload(mpf);
                } catch (IOException e) {
                    logger.error("Unable to upload attachment.", e);
                }

                createTechnologyAttachment(technology, mpf, path);
            }
        }
    }

    private void createTechnologyAttachment(final Entity technology, final MultipartFile mpf, final String path) {
        DataDefinition technologyAttachmentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_ATTACHMENT);

        String extension = Files.getFileExtension(path);

        if (MultiUploadHelper.EXTS.contains(extension.toUpperCase())) {
            Entity technologyAttachment = technologyAttachmentDD.create();

            BigDecimal fileSize = new BigDecimal(mpf.getSize(), numberService.getMathContext());
            BigDecimal divider = new BigDecimal(1024, numberService.getMathContext());
            BigDecimal size = fileSize.divide(divider, L_SCALE, RoundingMode.HALF_UP);

            technologyAttachment.setField(TechnologyAttachmentFields.ATTACHMENT, path);
            technologyAttachment.setField(TechnologyAttachmentFields.NAME, mpf.getOriginalFilename());
            technologyAttachment.setField(TechnologyAttachmentFields.TECHNOLOGY, technology);
            technologyAttachment.setField(TechnologyAttachmentFields.EXT, extension);
            technologyAttachment.setField(TechnologyAttachmentFields.SIZE, size);

            technologyAttachmentDD.save(technologyAttachment);
        }
    }

    @RequestMapping(value = "/getAttachment.html", method = RequestMethod.GET)
    public final void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        DataDefinition attachmentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_ATTACHMENT);

        Entity attachment = attachmentDD.get(ids[0]);

        InputStream is = fileService.getInputStream(attachment.getStringField(TechnologyAttachmentFields.ATTACHMENT));

        try {
            if (Objects.isNull(is)) {
                response.sendRedirect("/error.html?code=404");
            }

            response.setHeader("Content-disposition", "inline; filename=" + attachment.getStringField(TechnologyAttachmentFields.NAME));
            response.setContentType(fileService.getContentType(attachment.getStringField(TechnologyAttachmentFields.ATTACHMENT)));

            int bytes = IOUtils.copy(is, response.getOutputStream());

            response.setContentLength(bytes);
            response.flushBuffer();
        } catch (IOException e) {
            logger.error("Unable to copy attachment file to response stream.", e);
        }
    }

}
