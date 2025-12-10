package com.qcadoo.mes.basic.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.constants.BasicConstants;

@Controller
@RequestMapping(value = BasicConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class StaffLabelsController {

    @RequestMapping(value = "staffLabelsReport.pdf")
    public final ModelAndView staffLabelsReportPdf(@RequestParam("ids") final List<Long> ids) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("staffLabelsReportPdf");
        mav.addObject("ids", ids);

        return mav;
    }

    @RequestMapping(value = "productLabelsReport.pdf")
    public final ModelAndView productLabelsReportPdf(@RequestParam("ids") final List<Long> ids) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("productLabelsReportPdf");
        mav.addObject("ids", ids);

        return mav;
    }

}
