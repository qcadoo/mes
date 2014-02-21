/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.technologies.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.crud.CrudService;

@Controller
@RequestMapping("/rest/techologies")
public class TechnologyMultiUploadController {

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

    private static final List<String> exts = Lists.newArrayList("GIF", "JPG", "JPEG", "PNG", "PDF");

    @ResponseBody
    @RequestMapping(value = "/multiUploadFiles", method = RequestMethod.POST)
    public ModelAndView upload(MultipartHttpServletRequest request, HttpServletResponse response) {
        Long technologyId = Long.parseLong(request.getParameter("techId"));
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);
        DataDefinition atachmentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_ATTACHMENT);

        Iterator<String> itr = request.getFileNames();
        MultipartFile mpf = null;

        while (itr.hasNext()) {

            mpf = request.getFile(itr.next());

            String path = "";
            try {
                path = fileService.upload(mpf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (exts.contains(Files.getFileExtension(path).toUpperCase())) {
                Entity atchment = atachmentDD.create();
                atchment.setField(TechnologyAttachmentFields.ATTACHMENT, path);
                atchment.setField(TechnologyAttachmentFields.NAME, mpf.getOriginalFilename());
                atchment.setField(TechnologyAttachmentFields.TECHNOLOGY, technology);
                atchment.setField(TechnologyAttachmentFields.EXT, Files.getFileExtension(path));
                BigDecimal fileSize = new BigDecimal(mpf.getSize(), numberService.getMathContext());
                BigDecimal divider = new BigDecimal(1024, numberService.getMathContext());
                BigDecimal size = fileSize.divide(divider, L_SCALE, BigDecimal.ROUND_HALF_UP);
                atchment.setField(TechnologyAttachmentFields.SIZE, size);
                atachmentDD.save(atchment);
            }
        }
        Locale locale = LocaleContextHolder.getLocale();
        JSONObject json = new JSONObject(ImmutableMap.of("form.id", technologyId));
        Map<String, String> arguments = ImmutableMap.of("context", json.toString());
        ModelAndView mav = crudController.prepareView(TechnologiesConstants.PLUGIN_IDENTIFIER, "technologyDetails", arguments,
                locale);
        return mav;
    }

    @RequestMapping(value = "/getAttachment.html", method = RequestMethod.GET)
    public final void getAttachment(@RequestParam("id") final Long[] ids, HttpServletResponse response) {
        DataDefinition atachmentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_ATTACHMENT);
        Entity attachment = atachmentDD.get(ids[0]);
        InputStream is = fileService.getInputStream(attachment.getStringField(TechnologyAttachmentFields.ATTACHMENT));
        try {
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
