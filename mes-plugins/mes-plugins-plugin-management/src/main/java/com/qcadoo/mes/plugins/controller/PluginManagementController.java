package com.qcadoo.mes.plugins.controller;

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

@Controller
public final class PluginManagementController {

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public String getDownloadPageView(final Locale locale) {
        return getDownloadPageRedirect("download.html", locale);
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    public String handleDownload(@RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageRedirect(pluginManagementService.downloadPlugin(file), locale);
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String getRemovePageView(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageRedirect(pluginManagementService.removePlugin(entityId), locale);
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

    @RequestMapping(value = "restartPagePing", method = RequestMethod.GET)
    public String getRestartPagePagePing() {
        return "ok";
    }

    @RequestMapping(value = "enable", method = RequestMethod.GET)
    public String handleEnable(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageRedirect(pluginManagementService.enablePlugin(entityId), locale);
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        return pluginManagementService.restartServer();
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    public String getDisablePageView(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageRedirect(pluginManagementService.disablePlugin(entityId), locale);
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    public String handleDeinstall(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageRedirect(pluginManagementService.deinstallPlugin(entityId), locale);
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String getUpdatePageView(final Locale locale) {
        return getDownloadPageRedirect("update.html", locale);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String handleUpdate(@RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageRedirect(pluginManagementService.updatePlugin(file), locale);
    }

    @RequestMapping(value = "restartInfoView", method = RequestMethod.GET)
    public String getRestartInfoView(@RequestParam("message") final String message, final Locale locale) {
        return getInfoMessageRedirect(new PluginManagementOperationStatus(false, message), locale);
    }

    private String getDownloadPageRedirect(final String downloadAction, final Locale locale) {
        String headerLabel = translationService.translate("plugins.downloadView.header", locale);
        String buttonLabel = translationService.translate("plugins.downloadView.button", locale);
        return "redirect:page/plugins/pluginDownloadView.html?iframe=true&downloadAction=" + downloadAction + "&headerLabel="
                + headerLabel + "&buttonLabel=" + buttonLabel;
    }

    private String getInfoMessageRedirect(PluginManagementOperationStatus operationStatus, final Locale locale) {

        String message = translationService.translate(operationStatus.getMessage(), locale);

        if (operationStatus.isRestartRequired()) {
            return "redirect:restartPage.html?message=" + message;
        }
        String arguments = "";
        if (operationStatus.isError()) {
            String messageHeader = translationService.translate("plugins.messages.error.header", locale);
            arguments += "pluginStatusError=true&pluginStatusMessageHeader=" + messageHeader + "&pluginStatusMessage=" + message;
        } else {
            String messageHeader = translationService.translate("plugins.messages.success.header", locale);
            arguments += "pluginStatusError=false&pluginStatusMessageHeader=" + messageHeader + "&pluginStatusMessage=" + message;
        }

        return "redirect:page/plugins/pluginInfoView.html?iframe=true&" + arguments;
    }
}
