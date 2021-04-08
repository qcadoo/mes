package com.qcadoo.mes.orders.controllers;

import com.qcadoo.mes.orders.constants.OrdersConstants;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = OrdersConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class PacksLabelsController {

    @RequestMapping(value = "packsLabels.pdf")
    public final ModelAndView masterOrderLabelsPdf(@RequestParam("ids") final List<Long> ids) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("packsLabelsPdf");
        mav.addObject("ids", ids);

        return mav;
    }

}
