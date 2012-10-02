package com.qcadoo.mes.deliveries;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DeliveriesController {

    @RequestMapping(value = "deliveries/deliveryReport.pdf", method = RequestMethod.GET)
    public final ModelAndView deliveryReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("deliveryReportPdf");
        mav.addObject("id", id);

        return mav;
    }

    @RequestMapping(value = "deliveries/orderReport.pdf", method = RequestMethod.GET)
    public final ModelAndView orderReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("orderReportPdf");
        mav.addObject("id", id);

        return mav;
    }
}
