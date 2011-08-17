package com.qcadoo.mes.technologies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;

@Controller
public class TechnologyController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @RequestMapping(value = "technologies/technologyDetailsReport.pdf", method = RequestMethod.GET)
    public final ModelAndView technologiesReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("technologiesTechnologyDetailsPdfView");
        mav.addObject("id", id);
        mav.addObject("company", dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .uniqueResult());
        return mav;
    }

    @RequestMapping(value = "technologies/technologyDetailsReport.xls", method = RequestMethod.GET)
    public final ModelAndView technologiesReportXls(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("technologiesTechnologyDetailsXlsView");
        mav.addObject("id", id);
        return mav;
    }

}
