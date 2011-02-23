package com.qcadoo.mes.qualityControls;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class QualityControlsController {

    @RequestMapping(value = "qualityControl/qualityControlByDates.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlByDatesPdf(@RequestParam("type") final String type,
            @RequestParam("dateFrom") final Object dateFrom, @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "PdfView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlByDates.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlByDatesXls(@RequestParam("type") final String type,
            @RequestParam("dateFrom") final Object dateFrom, @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "XlsView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlReport.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlReportPdf(@RequestParam("type") final String type, @RequestParam("id") final Long[] entities) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "PdfView");
        mav.addObject("entities", entities);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlReport.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlReportXls(@RequestParam("type") final String type, @RequestParam("id") final Long[] entities) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "XlsView");
        mav.addObject("entities", entities);
        return mav;
    }

}
