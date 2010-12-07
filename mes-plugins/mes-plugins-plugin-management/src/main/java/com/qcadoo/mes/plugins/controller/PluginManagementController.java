/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;

@Controller
public final class PluginManagementController {

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private CrudController crudController;

    @RequestMapping(value = "pluginPages/restart", method = RequestMethod.GET)
    public ModelAndView getRestartPagePageView(@RequestParam("message") final String message, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("plugins/restart");
        mav.addObject("message", message);
        mav.addObject("messageHeader", translationService.translate("plugins.messages.success.header", locale));
        mav.addObject("restartMessage", translationService.translate("plugins.restartView.message", locale));
        return mav;
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        pluginManagementService.restartServer();
        return "ok";
    }

    @RequestMapping(value = "pluginPages/downloadPage", method = RequestMethod.GET)
    public ModelAndView getUpdatePageView(@RequestParam(required = false, value = "entityId") final Long pluginId,
            final Locale locale) {
        if (pluginId == null) {
            return getDownloadPageView("download.html", locale);
        } else {
            return getDownloadPageView("update.html", locale);
        }
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    public ModelAndView handleDownload(@RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageView(pluginManagementService.downloadPlugin(file), locale);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ModelAndView handleUpdate(@RequestParam("file") final MultipartFile file, final Locale locale) {
        return getInfoMessageView(pluginManagementService.updatePlugin(file), locale);
    }

    @RequestMapping(value = "restartInfo", method = RequestMethod.GET)
    public ModelAndView getRestartInfoView(@RequestParam("message") final String message, final Locale locale) {
        return getInfoMessageView(new PluginManagementOperationStatusImpl(false, message), locale);
    }

    private ModelAndView getDownloadPageView(final String downloadAction, final Locale locale) {
        String headerLabel = translationService.translate("plugins.pluginDownload.header", locale);
        String buttonLabel = translationService.translate("plugins.pluginDownload.button", locale);

        ModelAndView mav = crudController.prepareView("plugins", "pluginDownload", new HashMap<String, String>(), locale);

        mav.addObject("headerLabel", headerLabel);
        mav.addObject("buttonLabel", buttonLabel);

        mav.addObject("downloadAction", downloadAction);

        return mav;
    }

    private ModelAndView getInfoMessageView(final PluginManagementOperationStatus operationStatus, final Locale locale) {
        if (operationStatus.isRestartRequired()) {
            return getRestartPagePageView(operationStatus.getMessage(), locale);
        }
        ModelAndView mav = crudController.prepareView("plugins", "pluginInfo", new HashMap<String, String>(), locale);
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

    public void onPluginDownloadClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.redirectTo("../pluginPages/downloadPage.html", false);
    }

    public void onPluginUpdateClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long pluginId = getPluginId(triggerState);
        if (pluginId != null) {
            viewDefinitionState.redirectTo("../pluginPages/downloadPage.html?entityId=" + pluginId, false);
        }
    }

    public void onPluginEnableClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long pluginId = getPluginId(triggerState);
        if (pluginId != null) {
            PluginManagementOperationStatus operationStatus = pluginManagementService.enablePlugin(pluginId);
            updatePluginManagementOperationStatus(viewDefinitionState, triggerState, operationStatus, triggerState.getLocale());
        }
    }

    public void onPluginDisableClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long pluginId = getPluginId(triggerState);
        if (pluginId != null) {
            PluginManagementOperationStatus operationStatus = pluginManagementService.disablePlugin(pluginId);
            updatePluginManagementOperationStatus(viewDefinitionState, triggerState, operationStatus, triggerState.getLocale());
        }
    }

    public void onPluginDeinstallClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long pluginId = getPluginId(triggerState);
        if (pluginId != null) {
            PluginManagementOperationStatus operationStatus = pluginManagementService.deinstallPlugin(pluginId);
            updatePluginManagementOperationStatus(viewDefinitionState, triggerState, operationStatus, triggerState.getLocale());
        }
    }

    public void onPluginRemoveClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long pluginId = getPluginId(triggerState);
        if (pluginId != null) {
            PluginManagementOperationStatus operationStatus = pluginManagementService.removePlugin(pluginId);
            updatePluginManagementOperationStatus(viewDefinitionState, triggerState, operationStatus, triggerState.getLocale());
        }
    }

    private Long getPluginId(final ComponentState triggerState) {
        if (triggerState.getFieldValue() != null && triggerState.getFieldValue() instanceof Long) {
            return (Long) triggerState.getFieldValue();
        } else {
            triggerState.addMessage("Nie ma takiego numeru", MessageType.FAILURE); // TODO mina i18n
            return null;
        }
    }

    private void updatePluginManagementOperationStatus(final ViewDefinitionState viewDefinitionState,
            final ComponentState triggerState, final PluginManagementOperationStatus operationStatus, final Locale locale) {

        if (operationStatus.isRestartRequired()) {
            viewDefinitionState.redirectTo("../pluginPages/restart.html?message=TODO", false);
            return;
        }

        if (operationStatus.isError()) {
            triggerState.addMessage(translationService.translate(operationStatus.getMessage(), locale), MessageType.FAILURE);
        } else {
            triggerState.addMessage(translationService.translate(operationStatus.getMessage(), locale), MessageType.SUCCESS);
        }

    }
}
