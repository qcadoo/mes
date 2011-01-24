package com.qcadoo.mes.genealogies;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.crud.CrudController;

@Controller
public class GenealogyController {

    @Autowired
    private CrudController crudController;

    @Autowired
    private GenealogyAttributeService genealogyService;

    @RequestMapping(value = "genealogyAttribute", method = RequestMethod.GET)
    public ModelAndView getGenealogyAttributesPageView(final Locale locale) {

        JSONObject json = new JSONObject(ImmutableMap.of("window.currentAttribute.id", genealogyService.getGenealogyAttributeId()
                .toString()));
        Map<String, String> arguments = ImmutableMap.of("context", json.toString());
        return crudController.prepareView("genealogies", "currentAttribute", arguments, locale);
    }

    @RequestMapping(value = "genealogies/genealogyForComponent.pdf", method = RequestMethod.GET)
    public ModelAndView genealogyForComponentPdf(@RequestParam("value") final String value) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("genealogyForComponentView");
        mav.addObject("value", value);
        return mav;
    }

    @RequestMapping(value = "genealogies/genealogyForProduct.pdf", method = RequestMethod.GET)
    public ModelAndView genealogyForProductPdf(@RequestParam("value") final String value) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("genealogyForProductView");
        mav.addObject("value", value);
        return mav;
    }
}
