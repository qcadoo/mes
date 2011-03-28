package com.qcadoo.mes.plugins.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.plugin.api.artifact.InputStreamPluginArtifact;
import com.qcadoo.plugin.api.artifact.PluginArtifact;
import com.qcadoo.view.api.crud.CrudController;

@Controller
public class PluginManagmentUrlController {

    @Autowired
    private PluginManagmentPerformer pluginManagmentPerformer;

    @Autowired
    private CrudController crudController;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "pluginPages/downloadPage", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView(final Locale locale) {
        ModelAndView mav = getCrudPopupView("pluginDownload", locale);

        mav.addObject("headerLabel", translationService.translate("plugins.downloadView.header", locale));
        mav.addObject("buttonLabel", translationService.translate("plugins.downloadView.button", locale));
        mav.addObject("chooseFileLabel", translationService.translate("plugins.downloadView.chooseFileLabel", locale));

        return mav;
    }

    @RequestMapping(value = "performDownload.html", method = RequestMethod.POST)
    @ResponseBody
    public String handleDownload(@RequestParam("file") final MultipartFile file, final Locale locale) {
        try {
            PluginArtifact artifact = new InputStreamPluginArtifact(file.getOriginalFilename(), file.getInputStream());
            return pluginManagmentPerformer.performInstall(artifact);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading file", e);
        }
    }

    @RequestMapping(value = "pluginPages/infoPage", method = RequestMethod.GET)
    public ModelAndView getInfoPageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = getCrudPopupView("pluginInfo", locale);

        if ("success".equals(arguments.get("type"))) {
            mav.addObject("headerClass", "successHeader");
            mav.addObject("headerLabel", translationService.translate("plugins.pluginInfo.successHeader", locale));

        } else if ("error".equals(arguments.get("type"))) {
            mav.addObject("headerClass", "errorHeader");
            mav.addObject("headerLabel", translationService.translate("plugins.pluginInfo.errorHeader", locale));

        } else if ("confirm".equals(arguments.get("type"))) {
            mav.addObject("headerLabel", translationService.translate("plugins.pluginInfo.confirmHeader", locale));
            mav.addObject("isConfirm", true);
            mav.addObject("cancelButtonLabel",
                    translationService.translate("plugins.pluginInfo.buttons." + arguments.get("cancelLabel"), locale));
            mav.addObject("acceptButtonLabel",
                    translationService.translate("plugins.pluginInfo.buttons." + arguments.get("acceptLabel"), locale));
            mav.addObject("acceptRedirect", arguments.get("acceptRedirect"));

        } else {
            throw new IllegalStateException("Unsuported plugin info type: " + arguments.get("type"));
        }
        mav.addObject("content", translationService.translate("plugins.pluginInfo.content." + arguments.get("status"), locale));
        mav.addObject("dependencies", createDependenciesMap(arguments));
        mav.addObject("inVersion", translationService.translate("plugins.pluginInfo.inVersion", locale));

        return mav;
    }

    @RequestMapping(value = "pluginPages/restartPage", method = RequestMethod.GET)
    public ModelAndView getRestartPageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = getCrudPopupView("restartView", locale);

        mav.addObject("headerLabel", translationService.translate("plugins.restartView.header", locale));
        mav.addObject("restartMessage", translationService.translate("plugins.restartView.message", locale));
        mav.addObject("redirectPage", arguments.get("redirect"));

        return mav;
    }

    @RequestMapping(value = "pluginPages/performRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        pluginManagmentPerformer.performRestart();
        return "ok";
    }

    @RequestMapping(value = "pluginPages/performEnablingMultiplePlugins", method = RequestMethod.GET)
    public String performEnablingMultiplePlugins(@RequestParam("plugin") final List<String> plugins, final Locale locale) {
        return "redirect:" + pluginManagmentPerformer.performEnable(plugins);
    }

    @RequestMapping(value = "pluginPages/performDisablingMultiplePlugins", method = RequestMethod.GET)
    public String performDisablingMultiplePlugins(@RequestParam("plugin") final List<String> plugins, final Locale locale) {
        return "redirect:" + pluginManagmentPerformer.performDisable(plugins);
    }

    @RequestMapping(value = "pluginPages/performUninstallingMultiplePlugins", method = RequestMethod.GET)
    public String performUnonstallMultiplePlugins(@RequestParam("plugin") final List<String> plugins, final Locale locale) {
        return "redirect:" + pluginManagmentPerformer.performRemove(plugins);
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
