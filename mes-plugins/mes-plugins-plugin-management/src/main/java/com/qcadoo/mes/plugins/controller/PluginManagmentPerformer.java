package com.qcadoo.mes.plugins.controller;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.internal.api.PluginOperationResult;

@Service
public class PluginManagmentPerformer {

    public String performEnable(final List<String> pluginIdentifiers) {

        // Set<PluginDependencyInformation> unsatisfiedDependencies = new HashSet<PluginDependencyInformation>();
        // unsatisfiedDependencies.add(new PluginDependencyInformation("test1"));
        // unsatisfiedDependencies.add(new PluginDependencyInformation("test2", new VersionOfDependency("(1.1.1,2.3.4)")));
        // PluginDependencyResult pluginDependencyResult =
        // PluginDependencyResult.unsatisfiedDependencies(unsatisfiedDependencies);
        // PluginOperationResult result = PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);

        // PluginOperationResult result = PluginOperationResult.success();

        // Set<PluginDependencyInformation> dependenciesToEnable = new HashSet<PluginDependencyInformation>();
        // dependenciesToEnable.add(new PluginDependencyInformation("test1"));
        // dependenciesToEnable.add(new PluginDependencyInformation("test2", new VersionOfDependency("(1.1.1,2.3.4)")));
        // PluginDependencyResult pluginDependencyResult = PluginDependencyResult.dependenciesToEnable(dependenciesToEnable);
        // PluginOperationResult result = PluginOperationResult.dependenciesToEnable(pluginDependencyResult);

        PluginOperationResult result = PluginOperationResult.successWithRestart();

        // TODO mina get result

        String url = null;

        switch (result.getStatus()) {
            case SUCCESS:
                url = createSuccessPageUrl("enableSuccess");
                break;
            case SUCCESS_WITH_RESTART:
                url = createRestartPageUrl(createSuccessPageUrl("enableSuccess"));
                break;
            case UNSATISFIED_DEPENDENCIES:
                url = createErrorPageUrl("unsatisfiedDependencies", result.getPluginDependencyResult()
                        .getUnsatisfiedDependencies());
                break;
            case DEPENDENCIES_TO_ENABLE:
                // TODO mina add plugins to url
                url = createConfirmPageUrl("dependenciesToEnable", "dependenciesToEnableCancelLabel",
                        "dependenciesToEnableAcceptLabel", "performEnablingMultiplePlugins.html", result
                                .getPluginDependencyResult().getDependenciesToEnable());
                break;
            case CANNOT_INSTALL_PLUGIN:
                url = createErrorPageUrl("cannotInstall", null);
                break;
            default:
                throw new IllegalStateException("Wrong status");
        }

        return url;
    }

    public String performDisable(final List<String> pluginIdentifiers) {

        // TODO mina get result
        PluginOperationResult result = PluginOperationResult.success();

        String url = null;

        switch (result.getStatus()) {
            case SUCCESS:
                url = createSuccessPageUrl("disableSuccess");
                break;
            case SYSTEM_PLUGIN_DISABLING:
                url = createErrorPageUrl("disablingSystemPlugin", null);
                break;
            case DEPENDENCIES_TO_DISABLE:
                // TODO mina add plugins to url
                url = createConfirmPageUrl("dependenciesToDisable", "dependenciesToDisableCancelLabel",
                        "dependenciesToDisableAcceptLabel", "performDisablingMultiplePlugins.html", result
                                .getPluginDependencyResult().getDependenciesToDisable());
                break;
            default:
                throw new IllegalStateException("Wrong status");
        }

        return url;
    }

    public String performRemove(final List<String> pluginIdentifiers) {

        // TODO mina get result
        PluginOperationResult result = PluginOperationResult.success();

        String url = null;

        switch (result.getStatus()) {
            case SUCCESS:
                url = createSuccessPageUrl("uninstallSuccess");
                break;
            case SUCCESS_WITH_RESTART:
                url = createRestartPageUrl(createSuccessPageUrl("uninstallSuccess"));
                break;
            case SYSTEM_PLUGIN_UNINSTALLING:
                url = createErrorPageUrl("uninstallingSystemPlugin", null);
                break;
            case DEPENDENCIES_TO_UNINSTALL:
                // TODO mina add plugins to url
                url = createConfirmPageUrl("dependenciesToUninstall", "dependenciesToUninstallCancelLabel",
                        "dependenciesToUninstallAcceptLabel", "performUninstallingMultiplePlugins.html", result
                                .getPluginDependencyResult().getDependenciesToUninstall());
                break;
            default:
                throw new IllegalStateException("Wrong status");
        }

        return url;
    }

    private String createSuccessPageUrl(final String statusKey) {
        return "../pluginPages/infoPage.html?type=success&status=" + statusKey;
    }

    private String createErrorPageUrl(final String statusKey, final Set<PluginDependencyInformation> dependencies) {
        StringBuilder url = new StringBuilder("../pluginPages/infoPage.html?type=error&status=");
        url.append(statusKey);
        addDependenciesToUrl(url, dependencies);
        return url.toString();
    }

    private String createRestartPageUrl(final String redirectAfterSuccessPage) {
        StringBuilder url = new StringBuilder("../pluginPages/restartPage.html?redirect=");
        url.append(redirectAfterSuccessPage);
        return url.toString();
    }

    private String createConfirmPageUrl(final String statusKey, final String cancelLabel, final String acceptLabel,
            final String acceptRedirect, final Set<PluginDependencyInformation> dependencies) {
        StringBuilder url = new StringBuilder("../pluginPages/confirmPage.html?status=");
        url.append(statusKey);
        url.append("&cancelLabel=");
        url.append(cancelLabel);
        url.append("&acceptLabel=");
        url.append(acceptLabel);
        url.append("&acceptRedirect=");
        url.append(acceptRedirect);
        addDependenciesToUrl(url, dependencies);
        return url.toString();
    }

    private void addDependenciesToUrl(final StringBuilder url, final Set<PluginDependencyInformation> dependencies) {
        if (dependencies != null) {
            for (PluginDependencyInformation dependencyInfo : dependencies) {
                url.append("&dep_");
                url.append(dependencyInfo.getDependencyPluginIdentifier());
                url.append("=");
                if (dependencyInfo.getDependencyPluginVersion() == null
                        || "0.0.0".equals(dependencyInfo.getDependencyPluginVersion().toString())) {
                    url.append("none");
                } else {
                    url.append(dependencyInfo.getDependencyPluginVersion().toString());
                }
            }
        }
    }
}
