/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

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
import com.qcadoo.mes.internal.PluginManagementOperationStatusImpl;

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
        return getDownloadPageView("download.html", null, locale);
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    public ModelAndView handleDownload(@RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageView(pluginManagementService.downloadPlugin(file), locale);
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public ModelAndView getRemovePageView(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.removePlugin(Long.parseLong(entityId)), locale);
    }

    @RequestMapping(value = "restartPage", method = RequestMethod.GET)
    public ModelAndView getRestartPagePageView(@RequestParam("message") final String message, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("plugins/restartPage");
        mav.addObject("message", message);
        mav.addObject("messageHeader", translationService.translate("plugins.messages.success.header", locale));
        mav.addObject("restartMessage", translationService.translate("plugins.restartView.message", locale));
        return mav;
    }

    @RequestMapping(value = "enable", method = RequestMethod.GET)
    public ModelAndView handleEnable(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.enablePlugin(Long.parseLong(entityId)), locale);
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        pluginManagementService.restartServer();
        return "ok";
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    public ModelAndView getDisablePageView(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.disablePlugin(Long.parseLong(entityId)), locale);
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    public ModelAndView handleDeinstall(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getInfoMessageView(pluginManagementService.deinstallPlugin(Long.parseLong(entityId)), locale);
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public ModelAndView getUpdatePageView(@RequestParam("entityId") final String entityId, final Locale locale) {
        return getDownloadPageView("update.html", entityId, locale);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ModelAndView handleUpdate(@RequestParam("entityId") final String entityId,
            @RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageView(pluginManagementService.updatePlugin(Long.parseLong(entityId), file), locale);
    }

    @RequestMapping(value = "restartInfoView", method = RequestMethod.GET)
    public ModelAndView getRestartInfoView(@RequestParam("message") final String message, final Locale locale) {
        return getInfoMessageView(new PluginManagementOperationStatusImpl(false, message), locale);
    }

    private ModelAndView getDownloadPageView(final String downloadAction, final String entityId, final Locale locale) {

        if (entityId != null && !pluginManagementService.pluginIsInstalled(Long.parseLong(entityId))) {
            return getInfoMessageView(
                    new PluginManagementOperationStatusImpl(true, "plugins.messages.error.wrongStatusToUpdate"), locale);

        }

        ModelAndView mav = crudController.getView("plugins", "pluginDownloadView", new HashMap<String, String>(), locale);

        String headerLabel = translationService.translate("plugins.downloadView.header", locale);
        if (entityId != null) {
            String pluginName = pluginManagementService.get(Long.parseLong(entityId)).getName();
            headerLabel = translationService.translate("plugins.downloadView.update.header", locale) + ": <span class='grey'>"
                    + pluginName + "</span>";
        }

        String buttonLabel = translationService.translate("plugins.downloadView.button", locale);
        String firstCheckExtensionMessage = translationService.translate("plugins.downloadView.checkExtensionMessage.first",
                locale);
        String lastCheckExtensionMessage = translationService
                .translate("plugins.downloadView.checkExtensionMessage.last", locale);

        mav.addObject("headerLabel", headerLabel);
        mav.addObject("buttonLabel", buttonLabel);
        mav.addObject("downloadAction", downloadAction);
        mav.addObject("entityId", entityId);
        mav.addObject("firstCheckExtensionMessage", firstCheckExtensionMessage);
        mav.addObject("lastCheckExtensionMessage", lastCheckExtensionMessage);

        return mav;
    }

    private ModelAndView getInfoMessageView(final PluginManagementOperationStatus operationStatus, final Locale locale) {
        if (operationStatus.isRestartRequired()) {
            return getRestartPagePageView(operationStatus.getMessage(), locale);
        }
        ModelAndView mav = crudController.getView("plugins", "pluginInfoView", new HashMap<String, String>(), locale);
        String message = translationService.translate(operationStatus.getMessage(), locale);
        mav.addObject("pluginStatusMessage", message);
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
