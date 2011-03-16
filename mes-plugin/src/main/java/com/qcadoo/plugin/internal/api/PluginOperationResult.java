package com.qcadoo.plugin.internal.api;

import com.qcadoo.plugin.internal.dependencymanager.PluginDependencyResult;

public class PluginOperationResult {

    private final PluginOperationStatus status;

    private final PluginDependencyResult pluginDependencyResult;

    private PluginOperationResult(final PluginOperationStatus status) {
        this.status = status;
        this.pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
    }

    private PluginOperationResult(final PluginOperationStatus status, final PluginDependencyResult pluginDependencyResult) {
        this.status = status;
        this.pluginDependencyResult = pluginDependencyResult;
    }

    public static PluginOperationResult successWithRestart() {
        return new PluginOperationResult(PluginOperationStatus.SUCCESS_WITH_RESTART);
    }

    public static PluginOperationResult success() {
        return new PluginOperationResult(PluginOperationStatus.SUCCESS);
    }

    public static PluginOperationResult systemPluginDisabling() {
        return new PluginOperationResult(PluginOperationStatus.SYSTEM_PLUGIN_DISABLING);
    }

    public static PluginOperationResult systemPluginUninstalling() {
        return new PluginOperationResult(PluginOperationStatus.SYSTEM_PLUGIN_UNINSTALLING);
    }

    public static PluginOperationResult systemPluginUpdating() {
        return new PluginOperationResult(PluginOperationStatus.SYSTEM_PLUGIN_UPDATING);
    }

    public static PluginOperationResult corruptedPlugin() {
        return new PluginOperationResult(PluginOperationStatus.CORRUPTED_PLUGIN);
    }

    public static PluginOperationResult cannotUploadPlugin() {
        return new PluginOperationResult(PluginOperationStatus.CANNOT_UPLOAD_PLUGIN);
    }

    public static PluginOperationResult incorrectVersionPlugin() {
        return new PluginOperationResult(PluginOperationStatus.INCORRECT_VERSION_PLUGIN);
    }

    public static PluginOperationResult dependenciesToEnable(final PluginDependencyResult pluginDependencyResult) {
        return new PluginOperationResult(PluginOperationStatus.DEPENDENCIES_TO_ENABLE, pluginDependencyResult);
    }

    public static PluginOperationResult unsatisfiedDependencies(final PluginDependencyResult pluginDependencyResult) {
        return new PluginOperationResult(PluginOperationStatus.UNSATISFIED_DEPENDENCIES, pluginDependencyResult);
    }

    public static PluginOperationResult successWithMissingDependencies(final PluginDependencyResult pluginDependencyResult) {
        return new PluginOperationResult(PluginOperationStatus.SUCCESS_WITH_MISSING_DEPENDENCIES, pluginDependencyResult);
    }

    public PluginOperationStatus getStatus() {
        return status;
    }

    public PluginDependencyResult getPluginDependencyResult() {
        return pluginDependencyResult;
    }

    public static PluginOperationResult dependenciesToDisable(final PluginDependencyResult pluginDependencyResult) {
        return new PluginOperationResult(PluginOperationStatus.DEPENDENCIES_TO_DISABLE, pluginDependencyResult);
    }

    public static PluginOperationResult dependenciesToUninstall(final PluginDependencyResult pluginDependencyResult) {
        return new PluginOperationResult(PluginOperationStatus.DEPENDENCIES_TO_UNINSTALL, pluginDependencyResult);
    }

    public static PluginOperationResult unsatisfiedDependenciesAfterUpdate(final PluginDependencyResult pluginDependencyResult) {
        return new PluginOperationResult(PluginOperationStatus.UNSATISFIED_DEPENDENCIES_AFTER_UPDATE, pluginDependencyResult);
    }

    public boolean isSuccess() {
        switch (status) {
            case SUCCESS:
            case SUCCESS_WITH_RESTART:
            case SUCCESS_WITH_MISSING_DEPENDENCIES:
                return true;
            default:
                return false;
        }
    }

    public boolean isRestartNeccessary() {
        return PluginOperationStatus.SUCCESS_WITH_RESTART.equals(status);
    }

    public static PluginOperationResult cannotInstallPlugin() {
        return new PluginOperationResult(PluginOperationStatus.CANNOT_INSTALL_PLUGIN);
    }
}
