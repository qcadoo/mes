package com.qcadoo.mes.plugins.controller;

import java.util.HashMap;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.PluginManagementOperationStatus;
import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.crud.CrudController;

@Controller
public final class PluginManagementController {

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private CrudController crudController;

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView(final Locale locale) {
        return getDownloadPageView("download.html", locale);
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    public ModelAndView handleDownload(@RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageView(pluginManagementService.downloadPlugin(file), locale);
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public ModelAndView getRemovePageView(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.removePlugin(entityId), locale);
    }

    @RequestMapping(value = "restartPage", method = RequestMethod.GET)
    public ModelAndView getRestartPagePageView(@RequestParam("message") final String message, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("restartPage");
        mav.addObject("message", message);
        mav.addObject("messageHeader", translationService.translate("plugins.messages.success.header", locale));
        mav.addObject("restartMessage", translationService.translate("plugins.restartView.message", locale));
        return mav;
    }

    @RequestMapping(value = "enable", method = RequestMethod.GET)
    public ModelAndView handleEnable(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.enablePlugin(entityId), locale);
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        return pluginManagementService.restartServer();
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    public ModelAndView getDisablePageView(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.disablePlugin(entityId), locale);
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    public ModelAndView handleDeinstall(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.deinstallPlugin(entityId), locale);
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public ModelAndView getUpdatePageView(final Locale locale) {
        // return getDownloadPageView("update.html", locale);

        PluginManagementOperationStatus operationStatus = new PluginManagementOperationStatus(false, "lalala");
        operationStatus.setRestartRequired(true);

        return getInfoMessageView(operationStatus, locale);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ModelAndView handleUpdate(@RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageView(pluginManagementService.updatePlugin(file), locale);
    }

    @RequestMapping(value = "restartInfoView", method = RequestMethod.GET)
    public ModelAndView getRestartInfoView(@RequestParam("message") final String message, final Locale locale) {
        return getInfoMessageView(new PluginManagementOperationStatus(false, message), locale);
    }

    private ModelAndView getDownloadPageView(final String downloadAction, final Locale locale) {
        String headerLabel = translationService.translate("plugins.downloadView.header", locale);
        String buttonLabel = translationService.translate("plugins.downloadView.button", locale);

        ModelAndView mav = crudController.getView("plugins", "pluginDownloadView", new HashMap<String, String>(), locale);

        mav.addObject("headerLabel", headerLabel);
        mav.addObject("buttonLabel", buttonLabel);

        mav.addObject("downloadAction", downloadAction);

        return mav;
    }

    private ModelAndView getInfoMessageView(PluginManagementOperationStatus operationStatus, final Locale locale) {

        ModelAndView mav = crudController.getView("plugins", "pluginInfoView", new HashMap<String, String>(), locale);

        String message = translationService.translate(operationStatus.getMessage(), locale);
        mav.addObject("pluginStatusMessage", message);

        if (operationStatus.isRestartRequired()) {
            return getRestartPagePageView(message, locale);
        }
        if (operationStatus.isError()) {
            mav.addObject("pluginStatusError", true);
            mav.addObject("pluginStatusMessageHeader", translationService.translate("plugins.messages.error.header", locale));
        } else {
            mav.addObject("pluginStatusError", false);
            mav.addObject("pluginStatusMessageHeader", translationService.translate("plugins.messages.success.header", locale));
        }

        return mav;
    }
}
