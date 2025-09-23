package com.qcadoo.mes.ordersForSubproductsGeneration.controllers;

import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrdersForSubproductsGenerationConstans;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OrdersOFSPGController {

    @RequestMapping(value = OrdersForSubproductsGenerationConstans.PLUGIN_IDENTIFIER + "/listOfProductionOrdersReport.pdf", method = RequestMethod.GET)
    public final ModelAndView listOfProductionOrdersReport(@RequestParam("ids") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("listOfProductionOrdersReportPdf");
        mav.addObject("id", id);

        return mav;
    }
}
