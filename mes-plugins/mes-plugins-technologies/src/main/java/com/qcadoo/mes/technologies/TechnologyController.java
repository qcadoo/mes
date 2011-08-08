package com.qcadoo.mes.technologies;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TechnologyController {

    @RequestMapping(value = "technologies/technologyDetailsReport.pdf", method = RequestMethod.GET)
    public final ModelAndView technologiesReportPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("technologiesTechnologyDetailsPdfView");
        mav.addObject("id", id);
        return mav;
    }

}
