package com.qcadoo.mes.warehouseMinimalState.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.warehouseMinimalState.constants.WarehouseMinimalStateConstants;

@Controller
@RequestMapping(value = WarehouseMinimalStateConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class WarehouseMinimalStateController {

    @RequestMapping(value = "document.pdf")
    public final ModelAndView documentPdf() {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("warehouseMinimalStateReportPdf");

        return mav;
    }
}
