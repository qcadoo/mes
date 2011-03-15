package com.qcadoo.mes.plugins.controller;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;

@Service
public class PluginManagmentViewHook {

    public void onDownloadButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        System.out.println("DOWNLOAD");
        viewDefinitionState.openModal("../pluginPages/downloadPage.html");
    }

    public void onEnableButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        System.out.println("ENABLE");
    }

    public void onDisableButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        System.out.println("DISABLE");
    }

    public void onRemoveButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        System.out.println("REMOVE");
    }
}
