package com.qcadoo.plugin.internal.dependencymanager;

import java.util.Set;

import com.google.common.collect.Sets;
import com.qcadoo.plugin.api.PluginDependencyInformation;

public final class PluginDependencyResult {

    private Set<PluginDependencyInformation> dependenciesToEnable = Sets.newHashSet();

    private Set<PluginDependencyInformation> unsatisfiedDependencies = Sets.newHashSet();

    private Set<PluginDependencyInformation> dependenciesToDisable = Sets.newHashSet();

    private Set<PluginDependencyInformation> dependenciesToDisableUnsatisfiedAfterUpdate = Sets.newHashSet();

    private Set<PluginDependencyInformation> dependenciesToUninstall = Sets.newHashSet();

    private boolean cycleExists;

    private PluginDependencyResult() {
    }

    public Set<PluginDependencyInformation> getDependenciesToEnable() {
        return dependenciesToEnable;
    }

    public Set<PluginDependencyInformation> getUnsatisfiedDependencies() {
        return unsatisfiedDependencies;
    }

    public Set<PluginDependencyInformation> getDependenciesToDisable() {
        return dependenciesToDisable;
    }

    public Set<PluginDependencyInformation> getDependenciesToUninstall() {
        return dependenciesToUninstall;
    }

    public Set<PluginDependencyInformation> getDependenciesToDisableUnsatisfiedAfterUpdate() {
        return dependenciesToDisableUnsatisfiedAfterUpdate;
    }

    public static PluginDependencyResult dependenciesToEnable(final Set<PluginDependencyInformation> disabledDependencies) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setDisabledDependencies(disabledDependencies);
        return result;
    }

    public static PluginDependencyResult satisfiedDependencies() {
        return new PluginDependencyResult();
    }

    public static PluginDependencyResult unsatisfiedDependencies(final Set<PluginDependencyInformation> unsatisfiedDependencies) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setUnsatisfiedDependencies(unsatisfiedDependencies);
        return result;
    }

    public static PluginDependencyResult dependenciesToDisable(final Set<PluginDependencyInformation> enabledDependencies) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setEnabledDependencies(enabledDependencies);
        return result;
    }

    public static PluginDependencyResult dependenciesToUninstall(final Set<PluginDependencyInformation> uninstallDependencies) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setDependenciesToUninstall(uninstallDependencies);
        return result;
    }

    public static PluginDependencyResult cyclicDependencies() {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setCycleExists(true);
        return result;
    }

    public boolean isDependenciesSatisfied() {
        return dependenciesToEnable.isEmpty() && unsatisfiedDependencies.isEmpty() && dependenciesToDisable.isEmpty()
                && !isCyclic();
    }

    public boolean isCyclic() {
        return cycleExists;
    }

    private void setDependenciesToUninstall(Set<PluginDependencyInformation> dependenciesToUninstall) {
        this.dependenciesToUninstall = dependenciesToUninstall;
    }

    private void setCycleExists(boolean cycleExists) {
        this.cycleExists = cycleExists;
    }

    private void setDisabledDependencies(Set<PluginDependencyInformation> disabledDependencies) {
        this.dependenciesToEnable = disabledDependencies;
    }

    private void setUnsatisfiedDependencies(Set<PluginDependencyInformation> unsatisfiedDependencies) {
        this.unsatisfiedDependencies = unsatisfiedDependencies;
    }

    private void setEnabledDependencies(Set<PluginDependencyInformation> enabledDependencies) {
        this.dependenciesToDisable = enabledDependencies;
    }

    private void setDependenciesToDisableUnsatisfiedAfterUpdate(
            Set<PluginDependencyInformation> dependenciesToDisableUnsatisfiedAfterUpdate) {
        this.dependenciesToDisableUnsatisfiedAfterUpdate = dependenciesToDisableUnsatisfiedAfterUpdate;
    }
}
