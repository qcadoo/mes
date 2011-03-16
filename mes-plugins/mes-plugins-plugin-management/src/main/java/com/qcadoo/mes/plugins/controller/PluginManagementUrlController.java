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
public class PluginManagementUrlController {

    @Autowired
    private PluginManagmentPerformer pluginManagmentPerformer;

    @Autowired
    private CrudController crudController;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "pluginPages/downloadPage", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView(final Locale locale) {
        ModelAndView mav = getCrudPopupView("pluginDownload", locale);

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

    @RequestMapping(value = "pluginPages/infoPage", method = RequestMethod.GET)
    public ModelAndView getInfoPageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = getCrudPopupView("pluginInfo", locale);

        mav.addObject("type", arguments.get("type"));
        mav.addObject("content", "CONTENT: " + arguments.get("status"));
        mav.addObject("dependencies", createDependenciesMap(arguments));
        mav.addObject("headerLabel", translationService.translate("plugins2.errorView.header", locale));
        mav.addObject("inVersion", "ver");

        return mav;
    }

    @RequestMapping(value = "pluginPages/confirmPage", method = RequestMethod.GET)
    public ModelAndView getConfirmPageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = getCrudPopupView("confirm", locale);

        mav.addObject("headerLabel", "CONFIRM");
        mav.addObject("content", "CONTENT: " + arguments.get("status"));
        mav.addObject("cancelButtonLabel", arguments.get("cancelLabel"));
        mav.addObject("acceptButtonLabel", arguments.get("acceptLabel"));
        mav.addObject("acceptRedirect", arguments.get("acceptRedirect"));
        mav.addObject("dependencies", createDependenciesMap(arguments));

        return mav;
    }

    @RequestMapping(value = "pluginPages/restartPage", method = RequestMethod.GET)
    public ModelAndView getRestartPageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = getCrudPopupView("restart", locale);

        mav.addObject("headerLabel", "RESTART");
        mav.addObject("restartMessage", "RESTART MESSAGE");
        mav.addObject("redirectPage", arguments.get("redirect"));

        return mav;
    }

    @RequestMapping(value = "pluginPages/performEnablingMultiplePlugins", method = RequestMethod.GET)
    public String performEnablingMultiplePlugins(@RequestParam final Map<String, String> arguments, final Locale locale) {

        // TODO mina parse args to list
        return "redirect:" + pluginManagmentPerformer.performEnable(null);
    }

    @RequestMapping(value = "pluginPages/performDisablingMultiplePlugins", method = RequestMethod.GET)
    public String performDisablingMultiplePlugins(@RequestParam final Map<String, String> arguments, final Locale locale) {

        // TODO mina parse args to list
        return "redirect:" + pluginManagmentPerformer.performDisable(null);
    }

    @RequestMapping(value = "pluginPages/performUninstallingMultiplePlugins", method = RequestMethod.GET)
    public String performUnonstallMultiplePlugins(@RequestParam final Map<String, String> arguments, final Locale locale) {

        // TODO mina parse args to list
        return "redirect:" + pluginManagmentPerformer.performRemove(null);
    }

    private ModelAndView getCrudPopupView(final String viewName, final Locale locale) {
        Map<String, String> crudArgs = new HashMap<String, String>();
        crudArgs.put("popup", "true");
        return crudController.prepareView("plugins", viewName, crudArgs, locale);
    }

    private Map<String, String> createDependenciesMap(final Map<String, String> arguments) {
        Map<String, String> dependencies = new HashMap<String, String>();
        for (Map.Entry<String, String> arg : arguments.entrySet()) {
            if (arg.getKey().length() < 5) {
                continue;
            }
            if ("dep_".equals(arg.getKey().substring(0, 4))) {
                if ("none".equals(arg.getValue())) {
                    dependencies.put(arg.getKey().substring(4), null);
                } else {
                    dependencies.put(arg.getKey().substring(4), arg.getValue());
                }
            }
        }
        if (dependencies.size() > 0) {
            return dependencies;
        } else {
            return null;
        }
    }
}
