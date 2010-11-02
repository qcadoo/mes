package com.qcadoo.mes.plugins.controller;

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

@Controller
public final class PluginManagementController {

    @Autowired
    private PluginManagementService pluginManagementService;

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public String getDownloadPageView() {

        // TODO mina translate

        // ModelAndView mav = new ModelAndView("download");
        // return mav;

        return "redirect:page/plugins/pluginDownloadView.html?iframe=true&downloadAction=download.html";
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    public String handleDownload(@RequestParam("file") final MultipartFile file) {
        return getInfoMessageRedirect(pluginManagementService.downloadPlugin(file));
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String getRemovePageView(@RequestParam("entityId") final String entityId) {
        return getInfoMessageRedirect(pluginManagementService.removePlugin(entityId));
    }

    @RequestMapping(value = "restartPage", method = RequestMethod.GET)
    public ModelAndView getRestartPagePageView(@RequestParam("message") final String message) {

        // TODO mina translate

        ModelAndView mav = new ModelAndView();
        mav.setViewName("restartPage");
        mav.addObject("message", message);
        return mav;
    }

    @RequestMapping(value = "enable", method = RequestMethod.GET)
    public String handleEnable(@RequestParam("entityId") final String entityId) {
        return getInfoMessageRedirect(pluginManagementService.enablePlugin(entityId));
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        return pluginManagementService.restartServer();
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    public String getDisablePageView(@RequestParam("entityId") final String entityId) {
        return getInfoMessageRedirect(pluginManagementService.disablePlugin(entityId));
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    public String handleDeinstall(@RequestParam("entityId") final String entityId) {
        return getInfoMessageRedirect(pluginManagementService.deinstallPlugin(entityId));
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String getUpdatePageView() {

        // TODO mina translate

        return "redirect:page/plugins/pluginDownloadView.html?iframe=true&downloadAction=update.html";

        // ModelAndView mav = new ModelAndView();
        // mav.setViewName("update");
        // return mav;
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String handleUpdate(@RequestParam("file") final MultipartFile file) {
        return getInfoMessageRedirect(pluginManagementService.updatePlugin(file));
    }

    @RequestMapping(value = "restartInfoView", method = RequestMethod.GET)
    public String getRestartInfoView(@RequestParam("message") final String message) {
        return getInfoMessageRedirect(new PluginManagementOperationStatus(false, message));
    }

    private String getInfoMessageRedirect(PluginManagementOperationStatus operationStatus) {

        // TODO mina translate

        if (operationStatus.isRestartRequired()) {
            return "redirect:restartPage.html?message=" + operationStatus.getMessage();
        }
        String arguments = "";
        if (operationStatus.isError()) {
            arguments += "pluginStatusError=true&pluginStatusMessageHeader=GLUPCZE!!!&pluginStatusMessage="
                    + operationStatus.getMessage();
        } else {
            arguments += "pluginStatusError=false&pluginStatusMessageHeader=OK&pluginStatusMessage="
                    + operationStatus.getMessage();
        }

        return "redirect:page/plugins/pluginInfoView.html?iframe=true&" + arguments;
    }
}
