package com.qcadoo.mes.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.crud.CrudController;
import com.qcadoo.mes.internal.MenuService;

@Controller
public final class MainPageController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private CrudController crudController;

    @Value("${com.qcadoo.mes.buildInfo.applicationName}")
    private String applicationName;

    @Value("${com.qcadoo.mes.buildInfo.applicationVersion}")
    private String applicationVersion;

    @Value("${com.qcadoo.mes.buildInfo.buildNumber}")
    private String buildNumber;

    @Value("${com.qcadoo.mes.buildInfo.buildDate}")
    private String buildDate;

    @RequestMapping(value = "main", method = RequestMethod.GET)
    public ModelAndView getMainView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("core/main");
        mav.addObject("viewsList", viewDefinitionService.list());
        mav.addObject("commonTranslations", translationService.getCommonsMessages(locale));
        mav.addObject("menuStructure", menuService.getMenu(locale).getAsJson());
        mav.addObject("userLogin", securityService.getCurrentUser().getUserName());
        return mav;
    }

    @RequestMapping(value = "homePage", method = RequestMethod.GET)
    public ModelAndView getHomePageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUser().getUserName());

        mav.addObject("translationsMap", translationService.getDashboardMessages(locale));

        mav.setViewName("core/dashboard");
        return mav;
    }

    @RequestMapping(value = "systemInfo", method = RequestMethod.GET)
    public ModelAndView getSystemInfoView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = crudController.getView("core", "systemInfoView", arguments, locale);

        Map<String, String> translationsMap = new HashMap<String, String>();
        translationsMap.put("core.systemInfo.header", translationService.translate("core.systemInfo.header", locale));
        translationsMap.put("core.systemInfo.name.label", translationService.translate("core.systemInfo.name.label", locale));
        translationsMap.put("core.systemInfo.version.label",
                translationService.translate("core.systemInfo.version.label", locale));
        translationsMap.put("core.systemInfo.build.label", translationService.translate("core.systemInfo.build.label", locale));
        translationsMap.put("core.systemInfo.buildDate.label",
                translationService.translate("core.systemInfo.buildDate.label", locale));
        mav.addObject("translationsMap", translationsMap);

        mav.addObject("applicationName", applicationName);
        mav.addObject("applicationVersion", applicationVersion);
        mav.addObject("buildNumber", buildNumber);
        mav.addObject("buildDate", buildDate);

        return mav;
    }
}
