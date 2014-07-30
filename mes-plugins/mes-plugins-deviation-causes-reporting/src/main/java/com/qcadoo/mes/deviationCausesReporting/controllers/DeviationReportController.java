package com.qcadoo.mes.deviationCausesReporting.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DeviationReportController {

    @RequestMapping(value = "deviationCausesReporting/deviations.pdf", method = RequestMethod.GET)
    public final ModelAndView printDeviationReport(@RequestParam("dateFrom") final String dateFrom,
            @RequestParam("dateTo") final String dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("deviationProtocolPdf");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

}
