package com.qcadoo.mes.costCalculation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;

@Controller
public class CostCalculationController {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @RequestMapping(value = "costCalculation/costCalculationReport.pdf", method = RequestMethod.GET)
    public final ModelAndView costCalculationReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("costCalculationPdfView");
        mav.addObject("id", id);
        mav.addObject("company", dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .uniqueResult());
        return mav;
    }

}
