package com.qcadoo.mes.plugins.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.crud.CrudController;

@Controller
public class PluginManagementController {

    @Autowired
    private CrudController crudController;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "pluginPages/downloadPage", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView(final Locale locale) {

        Map<String, String> crudArgs = new HashMap<String, String>();
        crudArgs.put("popup", "true");

        ModelAndView mav = crudController.prepareView("plugins", "pluginDownload", crudArgs, locale);

        mav.addObject("headerLabel", translationService.translate("plugins2.downloadView.header", locale));
        mav.addObject("buttonLabel", translationService.translate("plugins2.downloadView.button", locale));
        mav.addObject("chooseFileLabel", translationService.translate("plugins2.downloadView.chooseFileLabel", locale));

        return mav;
    }

    @RequestMapping(value = "performDownload.html", method = RequestMethod.POST)
    public ModelAndView handleDownload(@RequestParam("file") final MultipartFile file, final Locale locale) {
        // return getInfoMessageView(pluginManagementService.downloadPlugin(file), locale);
        return new ModelAndView();
    }

    @RequestMapping(value = "pluginPages/errorPage", method = RequestMethod.GET)
    public ModelAndView getErrorPageView(@RequestParam("status") final String status, final Locale locale) {

        Map<String, String> crudArgs = new HashMap<String, String>();
        crudArgs.put("popup", "true");

        ModelAndView mav = crudController.prepareView("plugins", "pluginError", crudArgs, locale);

        mav.addObject("headerLabel", translationService.translate("plugins2.errorView.header", locale));
        mav.addObject("content", "ERROR: " + status);

        return mav;
    }
}
