package com.qcadoo.mes.materialFlowResources.controllers;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.view.api.crud.CrudService;

@Controller
public class ResourcesAttributesViewController {

    @Autowired
    private CrudService crudService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping("/resourcesAttributes")
    public ModelAndView getResourcesAttributesView(final Locale locale) {
        ModelAndView mav = crudService.prepareView(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.VIEW_RESOURCES_ATTRIBUTES, Collections.emptyMap(), locale);
        Map<String, String> slickGridTranslations = translationService.getMessagesGroup("slickGrid", locale);
        mav.addObject("slickGridTranslations", slickGridTranslations);
        return mav;
    }
}
