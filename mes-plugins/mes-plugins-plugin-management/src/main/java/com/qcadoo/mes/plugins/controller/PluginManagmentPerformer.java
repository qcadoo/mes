package com.qcadoo.mes.plugins.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.internal.api.PluginArtifact;
import com.qcadoo.plugin.internal.api.PluginOperationResult;

@Service
public class PluginManagmentPerformer {

    @Autowired
    private PluginManagmentConnector pluginManagmentConnector;

    public String performInstall(final PluginArtifact artifact) {

        PluginOperationResult result = pluginManagmentConnector.performInstall(artifact);

        String url = null;

        switch (result.getStatus()) {
            case SUCCESS:
                url = createSuccessPageUrl("install.success");
                break;
            case SUCCESS_WITH_RESTART:
                url = createRestartPageUrl(createSuccessPageUrl("install.success"));
                break;
            case SUCCESS_WITH_MISSING_DEPENDENCIES:
                url = createSuccessPageUrl("install.successWithMissingDependencies", result.getPluginDependencyResult()
                        .getUnsatisfiedDependencies());
                break;
            case CANNOT_UPLOAD_PLUGIN:
                url = createErrorPageUrl("install.cannotUploadPlugin");
                break;
            case CORRUPTED_PLUGIN:
                url = createErrorPageUrl("install.corruptedPlugin");
                break;
            case SYSTEM_PLUGIN_UPDATING:
                url = createErrorPageUrl("install.systemPlugin");
                break;
            case INCORRECT_VERSION_PLUGIN:
                url = createErrorPageUrl("install.incorrectVersion");
                break;
            case CANNOT_INSTALL_PLUGIN:
                url = createErrorPageUrl("install.cannotInstallPlugin");
                break;
            case DEPENDENCIES_TO_ENABLE:
                url = createErrorPageUrl("install.dependenciesToEnable", result.getPluginDependencyResult()
                        .getDependenciesToEnable());
                break;
            case UNSATISFIED_DEPENDENCIES:
                url = createErrorPageUrl("install.unsatisfiedDependencies", result.getPluginDependencyResult()
                        .getUnsatisfiedDependencies());
                break;
            case UNSATISFIED_DEPENDENCIES_AFTER_UPDATE:
                url = createErrorPageUrl("install.unsatisfiedDependenciesAfterUpdate", result.getPluginDependencyResult()
                        .getDependenciesToDisableUnsatisfiedAfterUpdate());
                break;
            default:
                throw new IllegalStateException("Wrong status");
        }

        return url;
    }

    public String performEnable(final List<String> pluginIdentifiers) {

        PluginOperationResult result = pluginManagmentConnector.performEnable(pluginIdentifiers);

        String url = null;

        switch (result.getStatus()) {
            case SUCCESS:
                url = createSuccessPageUrl("enable.success");
                break;
            case SUCCESS_WITH_RESTART:
                url = createRestartPageUrl(createSuccessPageUrl("enable.success"));
                break;
            case UNSATISFIED_DEPENDENCIES:
                url = createErrorPageUrl("enable.unsatisfiedDependencies", result.getPluginDependencyResult()
                        .getUnsatisfiedDependencies());
                break;
            case DEPENDENCIES_TO_ENABLE:
                url = createConfirmPageUrl("enable.dependenciesToEnable", "enable.dependenciesToEnableCancelLabel",
                        "enable.dependenciesToEnableAcceptLabel", "performEnablingMultiplePlugins", result
                                .getPluginDependencyResult().getDependenciesToEnable(), pluginIdentifiers);
                break;
            case CANNOT_INSTALL_PLUGIN:
                url = createErrorPageUrl("cannotInstall");
                break;
            default:
                throw new IllegalStateException("Wrong status");
        }

        return url;
    }

    public String performDisable(final List<String> pluginIdentifiers) {

        PluginOperationResult result = pluginManagmentConnector.performDisable(pluginIdentifiers);

        String url = null;

        switch (result.getStatus()) {
            case SUCCESS:
                url = createSuccessPageUrl("disable.success");
                break;
            case SYSTEM_PLUGIN_DISABLING:
                url = createErrorPageUrl("disable.systemPlugin");
                break;
            case DEPENDENCIES_TO_DISABLE:
                url = createConfirmPageUrl("disable.dependenciesToDisable", "disable.dependenciesToDisableCancelLabel",
                        "disable.dependenciesToDisableAcceptLabel", "performDisablingMultiplePlugins", result
                                .getPluginDependencyResult().getDependenciesToDisable(), pluginIdentifiers);
                break;
            default:
                throw new IllegalStateException("Wrong status");
        }

        return url;
    }

    public String performRemove(final List<String> pluginIdentifiers) {

        PluginOperationResult result = pluginManagmentConnector.performRemove(pluginIdentifiers);

        String url = null;

        switch (result.getStatus()) {
            case SUCCESS:
                url = createSuccessPageUrl("uninstall.success");
                break;
            case SUCCESS_WITH_RESTART:
                url = createRestartPageUrl(createSuccessPageUrl("uninstall.success"));
                break;
            case SYSTEM_PLUGIN_UNINSTALLING:
                url = createErrorPageUrl("uninstall.systemPlugin");
                break;
            case DEPENDENCIES_TO_UNINSTALL:
                url = createConfirmPageUrl("uninstall.dependenciesToUninstall", "uninstall.dependenciesToUninstallCancelLabel",
                        "uninstall.dependenciesToUninstallAcceptLabel", "performUninstallingMultiplePlugins", result
                                .getPluginDependencyResult().getDependenciesToUninstall(), pluginIdentifiers);
                break;
            default:
                throw new IllegalStateException("Wrong status");
        }

        return url;
    }

    public void performRestart() {
        pluginManagmentConnector.performRestart();
    }

    private String createSuccessPageUrl(final String statusKey) {
        return createSuccessPageUrl(statusKey, null);
    }

    private String createSuccessPageUrl(final String statusKey, final Set<PluginDependencyInformation> dependencies) {
        StringBuilder url = new StringBuilder("../pluginPages/infoPage.html?type=success&status=");
        url.append(statusKey);
        addDependenciesToUrl(url, dependencies);
        return url.toString();
    }

    private String createErrorPageUrl(final String statusKey) {
        return createErrorPageUrl(statusKey, null);
    }

    private String createErrorPageUrl(final String statusKey, final Set<PluginDependencyInformation> dependencies) {
        StringBuilder url = new StringBuilder("../pluginPages/infoPage.html?type=error&status=");
        url.append(statusKey);
        addDependenciesToUrl(url, dependencies);
        return url.toString();
    }

    private String createConfirmPageUrl(final String statusKey, final String cancelLabel, final String acceptLabel,
            final String acceptRedirect, final Set<PluginDependencyInformation> dependencies, final List<String> pluginIdentifiers) {

        StringBuilder redirectUrl = new StringBuilder(acceptRedirect);
        redirectUrl.append(".html?");
        for (String pluginIdentifier : pluginIdentifiers) {
            if (redirectUrl.charAt(redirectUrl.length() - 1) != '?') {
                redirectUrl.append("&");
            }
            redirectUrl.append("plugin=");
            redirectUrl.append(pluginIdentifier);
        }
        for (PluginDependencyInformation dependency : dependencies) {
            redirectUrl.append("&plugin=");
            redirectUrl.append(dependency.getDependencyPluginIdentifier());
        }

        StringBuilder url = new StringBuilder("../pluginPages/infoPage.html?type=confirm&status=");
        url.append(statusKey);
        url.append("&cancelLabel=");
        url.append(cancelLabel);
        url.append("&acceptLabel=");
        url.append(acceptLabel);
        url.append("&acceptRedirect=");
        try {
            url.append(URLEncoder.encode(redirectUrl.toString(), "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Error while generating redirect url", e);
        }

        addDependenciesToUrl(url, dependencies);
        return url.toString();
    }

    private String createRestartPageUrl(final String redirectAfterSuccessPage) {
        StringBuilder url = new StringBuilder("../pluginPages/restartPage.html?redirect=");
        try {
            url.append(URLEncoder.encode(redirectAfterSuccessPage, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Error while generating redirect url", e);
        }
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
