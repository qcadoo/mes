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
package com.qcadoo.mes.orders.controllers;

import com.google.common.io.Files;
import com.qcadoo.mes.basic.MultiUploadHelper;
import com.qcadoo.mes.orders.constants.OrderAttachmentFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
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
import java.util.Iterator;

@Controller
@RequestMapping("/orders")
public class OrderMultiUploadController {

    private static final Logger logger = LoggerFactory.getLogger(OrderMultiUploadController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    private static final Integer L_SCALE = 2;

    @ResponseBody
    @RequestMapping(value = "/multiUploadFilesForOrder", method = RequestMethod.POST)
    public void upload(MultipartHttpServletRequest request, HttpServletResponse response) {
        Long orderId = Long.parseLong(request.getParameter("orderId"));
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        DataDefinition attachmentDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER_ATTACHMENT);

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
            if (MultiUploadHelper.EXTS.contains(Files.getFileExtension(path).toUpperCase())) {
                Entity attachment = attachmentDD.create();
                attachment.setField(OrderAttachmentFields.ATTACHMENT, path);
                attachment.setField(OrderAttachmentFields.NAME, mpf.getOriginalFilename());
                attachment.setField(OrderAttachmentFields.ORDER, order);
                attachment.setField(OrderAttachmentFields.EXT, Files.getFileExtension(path));
                BigDecimal fileSize = new BigDecimal(mpf.getSize(), numberService.getMathContext());
                BigDecimal divider = new BigDecimal(1024, numberService.getMathContext());
                BigDecimal size = fileSize.divide(divider, L_SCALE, BigDecimal.ROUND_HALF_UP);
                attachment.setField(OrderAttachmentFields.SIZE, size);
                attachmentDD.save(attachment);
            }
        }
    }

    @RequestMapping(value = "/getAttachmentForOrder.html", method = RequestMethod.GET)
    public final void getAttachment(@RequestParam("id") final Long[] ids, HttpServletResponse response) {
        DataDefinition attachmentDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER_ATTACHMENT);
        Entity attachment = attachmentDD.get(ids[0]);
        InputStream is = fileService.getInputStream(attachment.getStringField(OrderAttachmentFields.ATTACHMENT));

        try {
            if (is == null) {
                response.sendRedirect("/error.html?code=404");
            }

            response.setHeader("Content-disposition",
                    "inline; filename=" + attachment.getStringField(OrderAttachmentFields.NAME));
            response.setContentType(fileService.getContentType(attachment.getStringField(OrderAttachmentFields.ATTACHMENT)));

            int bytes = IOUtils.copy(is, response.getOutputStream());
            response.setContentLength(bytes);

            response.flushBuffer();

        } catch (IOException e) {
            logger.error("Unable to copy attachment file to response stream.", e);
        }
    }
}
