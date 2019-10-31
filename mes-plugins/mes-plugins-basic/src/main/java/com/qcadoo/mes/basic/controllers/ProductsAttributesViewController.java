package com.qcadoo.mes.basic.controllers;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.view.api.crud.CrudService;

@Controller
public class ProductsAttributesViewController {

    @Autowired
    private CrudService crudService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping("/productsAttributes")
    public ModelAndView getProductsAttributesView(final Locale locale) {
        ModelAndView mav = crudService.prepareView(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.VIEW_PRODUCTS_ATTRIBUTES,
                Collections.emptyMap(), locale);
        Map<String, String> slickGridTranslations = translationService.getMessagesGroup("slickGrid", locale);
        mav.addObject("slickGridTranslations", slickGridTranslations);
        return mav;
    }
}
