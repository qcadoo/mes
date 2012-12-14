/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;

@Controller
@RequestMapping(value = DeliveriesConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class DeliveriesController {

    @Autowired
    private CompanyService companyService;

    @RequestMapping(value = "deliveryReport.pdf")
    public final ModelAndView deliveryReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("deliveryReportPdf");
        mav.addObject("id", id);
        mav.addObject("company", companyService.getCompany());
        return mav;
    }

    @RequestMapping(value = "orderReport.pdf")
    public final ModelAndView orderReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("orderReportPdf");
        mav.addObject("id", id);
        mav.addObject("company", companyService.getCompany());
        return mav;
    }

}
