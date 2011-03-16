package com.qcadoo.mes.plugins.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.VersionOfDependency;
import com.qcadoo.plugin.internal.api.PluginOperationResult;
import com.qcadoo.plugin.internal.dependencymanager.PluginDependencyResult;

@Service
public class PluginManagmentViewHook {

    public void onDownloadButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal("../pluginPages/downloadPage.html");
    }

    public void onEnableButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        System.out.println("ENABLE");

        Set<PluginDependencyInformation> unsatisfiedDependencies = new HashSet<PluginDependencyInformation>();
        unsatisfiedDependencies.add(new PluginDependencyInformation("test1"));
        unsatisfiedDependencies.add(new PluginDependencyInformation("test2", new VersionOfDependency("(1.1.1, 2.3.4)")));
        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.unsatisfiedDependencies(unsatisfiedDependencies);
        PluginOperationResult result = PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);

        if (result.isSuccess()) {
            // TODO
        } else {
            String url = "../pluginPages/errorPage.html?status=" + result.getStatus();
            if (result.getPluginDependencyResult() != null) {
                for (PluginDependencyInformation dependencyInfo : result.getPluginDependencyResult().getUnsatisfiedDependencies()) {
                    dependencyInfo.getDependencyPluginIdentifier();
                    dependencyInfo.getDependencyPluginVersion().toString();
                }
            }
            viewDefinitionState.openModal(url);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
