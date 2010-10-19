package com.qcadoo.mes.plugins.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.PluginManagementService;

@Controller
public final class PluginManagementController {

    @Autowired
    private PluginManagementService pluginManagementService;

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView() {
        ModelAndView mav = new ModelAndView("download");
        return mav;
    }

    @RequestMapping(value = "downloadError", method = RequestMethod.GET)
    public ModelAndView getDownloadErrorPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("downloadError");
        return mav;
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    public String handleDownload(@RequestParam("file") final MultipartFile file) {
        return pluginManagementService.downloadPlugin(file);
    }

    @RequestMapping(value = "removePage", method = RequestMethod.GET)
    public ModelAndView getRemovePageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("remove");
        return mav;
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String getRemovePageView(@RequestParam("entityId") final String entityId) {
        return pluginManagementService.removePlugin(entityId);
    }

    @RequestMapping(value = "enablePage", method = RequestMethod.GET)
    public ModelAndView getEnablePageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("enable");
        return mav;
    }

    @RequestMapping(value = "enable", method = RequestMethod.GET)
    public String handleEnable(@RequestParam("entityId") final String entityId) {
        return pluginManagementService.enablePlugin(entityId);
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        return pluginManagementService.restartServer();
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    public String getDisablePageView(@RequestParam("entityId") final String entityId) {
        return pluginManagementService.disablePlugin(entityId);
    }

    @RequestMapping(value = "deinstallPage", method = RequestMethod.GET)
    public ModelAndView getDeinstallPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("enable");
        return mav;
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    public String handleDeinstall(@RequestParam("entityId") final String entityId) {
        return pluginManagementService.deinstallPlugin(entityId);
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public ModelAndView getUpdatePageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("update");
        return mav;
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String handleUpdate(@RequestParam("file") final MultipartFile file) {
        return pluginManagementService.updatePlugin(file);
    }
}
