package com.qcadoo.mes.qualityControl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class QualityControlsController {

    @RequestMapping(value = "qualityControl/qualityControlForOrder.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlForOrderPdf(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForOrderPdfView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlForOrder.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlForOrderXls(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForOrderXlsView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlForUnit.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlForUnitPdf(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForUnitPdfView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlForUnit.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlForUnitXls(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForUnitXlsView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlForBatch.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlForBatchPdf(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForBatchPdfView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlForBatch.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlForBatchXls(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForBatchXlsView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlForOperation.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlForOperationPdf(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForOperationPdfView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlForOperation.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlForOperationXls(@RequestParam("dateFrom") final Object dateFrom,
            @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlForOperationXlsView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

}
