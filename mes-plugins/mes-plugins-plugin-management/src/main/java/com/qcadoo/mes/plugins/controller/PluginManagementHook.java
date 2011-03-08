/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.PluginManagementOperationStatus;
import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;

@Service
public final class PluginManagementHook {

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private TranslationService translationService;

    public void onPluginDownloadClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.redirectTo("../pluginPages/downloadPage.html", false, true);
    }

    public void onPluginUpdateClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long pluginId = getPluginId(triggerState);
        if (pluginId != null) {
            viewDefinitionState.redirectTo("../pluginPages/downloadPage.html?entityId=" + pluginId, false, true);
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

    public void onPluginRemoveClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long pluginId = getPluginId(triggerState);
        if (pluginId != null) {
            PluginManagementOperationStatus operationStatus = pluginManagementService.removePlugin(pluginId);
            updatePluginManagementOperationStatus(viewDefinitionState, triggerState, operationStatus, triggerState.getLocale());
        }
    }

    private Long getPluginId(final ComponentState triggerState) {
        if (triggerState.getFieldValue() instanceof Long) {
            return (Long) triggerState.getFieldValue();
        } else {
            triggerState.addMessage(
                    translationService.translate("plugins.messages.error.pluginNotFound", triggerState.getLocale()),
                    MessageType.FAILURE);
            return null;
        }
    }

    private void updatePluginManagementOperationStatus(final ViewDefinitionState viewDefinitionState,
            final ComponentState triggerState, final PluginManagementOperationStatus operationStatus, final Locale locale) {

        if (operationStatus.isRestartRequired()) {
            String message = translationService.translate("plugins.messages.success.restartSucces", triggerState.getLocale());
            viewDefinitionState.redirectTo("../pluginPages/restart.html?message=" + message, false, false);
            return;
        }

        if (operationStatus.isError()) {
            triggerState.addMessage(translationService.translate(operationStatus.getMessage(), locale), MessageType.FAILURE);
        } else {
            triggerState.addMessage(translationService.translate(operationStatus.getMessage(), locale), MessageType.SUCCESS);
        }

    }
}
