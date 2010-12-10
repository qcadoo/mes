package com.qcadoo.mes.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.model.DataDefinition;

@Controller
public class ProductController {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @RequestMapping(value = "products/materialRequirement.pdf", method = RequestMethod.GET)
    public ModelAndView materialRequirementPdf(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("materialRequirementPdfView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/materialRequirement.xls", method = RequestMethod.GET)
    public ModelAndView materialRequirementXls(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("materialRequirementXlsView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/order.pdf", method = RequestMethod.GET)
    public ModelAndView orderPdf(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "order");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("orderPdfView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/workPlanForWorker.pdf", method = RequestMethod.GET)
    public ModelAndView workPlanForWorkerPdf(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("workPlanForWorkerPdfView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/workPlanForMachine.pdf", method = RequestMethod.GET)
    public ModelAndView workPlanForMachinePdf(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("workPlanForMachinePdfView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/workPlanForWorker.xls", method = RequestMethod.GET)
    public ModelAndView workPlanForWorkerXls(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("workPlanForWorkerXlsView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/workPlanForMachine.xls", method = RequestMethod.GET)
    public ModelAndView workPlanForMachineXls(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("workPlanForMachineXlsView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

}
