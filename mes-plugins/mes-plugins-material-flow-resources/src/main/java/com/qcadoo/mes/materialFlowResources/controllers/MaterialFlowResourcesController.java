package com.qcadoo.mes.materialFlowResources.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;

@Controller
@RequestMapping(value = MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, method = RequestMethod.GET)
public class MaterialFlowResourcesController {

    @RequestMapping(value = "document.pdf")
    public final ModelAndView documentPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("documentPdf");
        mav.addObject("id", id);

        return mav;
    }

}
