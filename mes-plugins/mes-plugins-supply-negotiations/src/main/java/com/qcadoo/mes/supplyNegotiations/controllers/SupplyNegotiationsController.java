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
package com.qcadoo.mes.supplyNegotiations.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;

@Controller
@RequestMapping(value = SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class SupplyNegotiationsController {

    @RequestMapping(value = "requestsForQuotationReport.pdf")
    public final ModelAndView requestsForQuotationReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("requestsForQuotationReportPdf");
        mav.addObject("id", id);

        return mav;
    }

    @RequestMapping(value = "offerReport.pdf")
    public final ModelAndView offerReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("offerReportPdf");
        mav.addObject("id", id);

        return mav;
    }

}
