package com.qcadoo.mes.supplyNegotiations.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RequestForQuotationController {

    @RequestMapping(value = "supplyNegotiations/requestsForQuotationReport.pdf", method = RequestMethod.GET)
    public final ModelAndView deliveryReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("requestsForQuotationReportPdf");
        mav.addObject("id", id);

        return mav;
    }
}
