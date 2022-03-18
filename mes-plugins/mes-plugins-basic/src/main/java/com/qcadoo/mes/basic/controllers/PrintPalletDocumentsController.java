/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.basic.controllers;

import com.qcadoo.mes.basic.constants.BasicConstants;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
@RequestMapping(value = BasicConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class PrintPalletDocumentsController {

    @RequestMapping(value = "KARTA_PALETY.pdf", produces = "application/pdf")
    public void pdfMethodCard(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {

            inputStream = getPdf("KARTA_PALETY.pdf");
            outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);

        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    @RequestMapping(value = "PALETA_MIX.pdf", produces = "application/pdf")
    public void pdfMethodMix(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {

            inputStream = getPdf("PALETA_MIX.pdf");
            outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);

        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private InputStream getPdf(String filename) throws IOException {
        return PrintPalletDocumentsController.class.getResourceAsStream("/" + filename);
    }

}
