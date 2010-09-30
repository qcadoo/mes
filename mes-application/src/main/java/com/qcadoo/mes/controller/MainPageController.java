package com.qcadoo.mes.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.menu.FirstLevelItem;
import com.qcadoo.mes.model.menu.MenuDefinition;
import com.qcadoo.mes.model.menu.SecondLevelItem;

@Controller
public final class MainPageController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "mainPage", method = RequestMethod.GET)
    public ModelAndView getView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("mainPage");
        mav.addObject("viewsList", viewDefinitionService.list());
        mav.addObject("commonTranslations", translationService.getCommonsTranslations(locale));
        return mav;
    }

    @RequestMapping(value = "main", method = RequestMethod.GET)
    public ModelAndView getMainView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("main");
        mav.addObject("viewsList", viewDefinitionService.list());
        mav.addObject("commonTranslations", translationService.getCommonsTranslations(locale));

        mav.addObject("menuStructure", generateMenuJson());

        return mav;
    }

    @RequestMapping(value = "homePage", method = RequestMethod.GET)
    public ModelAndView getHomePageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("testPage");
        return mav;
    }

    private String generateMenuJson() {

        MenuDefinition menuDef = new MenuDefinition();

        FirstLevelItem homeItem = new FirstLevelItem("home", "start");
        homeItem.addItem(new SecondLevelItem("home", "start", "homePage.html"));
        menuDef.addItem(homeItem);

        FirstLevelItem productsItem = new FirstLevelItem("products", "Zarządzanie Produktami");
        productsItem.addItem(new SecondLevelItem("products", "Produkty", "page/products/productGridView.html"));
        productsItem.addItem(new SecondLevelItem("productionOrders", "Zlecenia produkcyjne", "page/products/orderGridView.html"));
        productsItem.addItem(new SecondLevelItem("products", "Produkty", "page/products/productGridView.html"));
        menuDef.addItem(productsItem);

        FirstLevelItem administrationItem = new FirstLevelItem("administration", "Administracja");
        administrationItem.addItem(new SecondLevelItem("dictionaries", "Słowniki", "page/dictionaries/dictionaryGridView.html"));
        administrationItem.addItem(new SecondLevelItem("users", "Użytkownicy", "page/users/userGridView.html"));
        administrationItem.addItem(new SecondLevelItem("groups", "Grupy", "page/users/groupGridView.html"));
        administrationItem.addItem(new SecondLevelItem("plugins", "Pluginy", "page/plugins/pluginGridView.html"));
        menuDef.addItem(administrationItem);

        return menuDef.getAsJson();
    }
}
