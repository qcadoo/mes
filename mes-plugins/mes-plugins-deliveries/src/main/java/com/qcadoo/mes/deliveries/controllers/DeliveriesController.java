package com.qcadoo.mes.deliveries.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;

@Controller
@RequestMapping(value = DeliveriesConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class DeliveriesController {

    @RequestMapping(value = "deliveryReport.pdf")
    public final ModelAndView deliveryReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("deliveryReportPdf");
        mav.addObject("id", id);

        return mav;
    }

    @RequestMapping(value = "orderReport.pdf")
    public final ModelAndView orderReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("orderReportPdf");
        mav.addObject("id", id);

        return mav;
    }
}
