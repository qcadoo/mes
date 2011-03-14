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

    public static PluginDependencyResult dependenciesToEnable(final Set<PluginDependencyInformation> dependenciesToEnable) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setDependenciesToEnable(dependenciesToEnable);
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

    public static PluginDependencyResult dependenciesToDisable(final Set<PluginDependencyInformation> dependenciesToDisable) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setDependenciesToDisable(dependenciesToDisable);
        return result;
    }

    public static PluginDependencyResult dependenciesToUninstall(final Set<PluginDependencyInformation> dependenciesToUninstall) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setDependenciesToUninstall(dependenciesToUninstall);
        return result;
    }

    public static PluginDependencyResult dependenciesToUpdate(final Set<PluginDependencyInformation> dependenciesToDisable,
            final Set<PluginDependencyInformation> dependenciesToDisableUnsatisfiedAfterUpdate) {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setDependenciesToDisable(dependenciesToDisable);
        result.setDependenciesToDisableUnsatisfiedAfterUpdate(dependenciesToDisableUnsatisfiedAfterUpdate);
        return result;
    }

    public static PluginDependencyResult cyclicDependencies() {
        PluginDependencyResult result = new PluginDependencyResult();
        result.setCycleExists(true);
        return result;
    }

    public boolean isDependenciesSatisfied() {
        return dependenciesToEnable.isEmpty() && unsatisfiedDependencies.isEmpty() && dependenciesToDisable.isEmpty()
                && dependenciesToUninstall.isEmpty() && dependenciesToDisableUnsatisfiedAfterUpdate.isEmpty() && !isCyclic();
    }

    public boolean isCyclic() {
        return cycleExists;
    }

    private void setDependenciesToUninstall(final Set<PluginDependencyInformation> dependenciesToUninstall) {
        this.dependenciesToUninstall = dependenciesToUninstall;
    }

    private void setCycleExists(final boolean cycleExists) {
        this.cycleExists = cycleExists;
    }

    private void setDependenciesToEnable(final Set<PluginDependencyInformation> dependenciesToEnable) {
        this.dependenciesToEnable = dependenciesToEnable;
    }

    private void setUnsatisfiedDependencies(final Set<PluginDependencyInformation> unsatisfiedDependencies) {
        this.unsatisfiedDependencies = unsatisfiedDependencies;
    }

    private void setDependenciesToDisable(final Set<PluginDependencyInformation> dependenciesToDisable) {
        this.dependenciesToDisable = dependenciesToDisable;
    }

    private void setDependenciesToDisableUnsatisfiedAfterUpdate(
            final Set<PluginDependencyInformation> dependenciesToDisableUnsatisfiedAfterUpdate) {
        this.dependenciesToDisableUnsatisfiedAfterUpdate = dependenciesToDisableUnsatisfiedAfterUpdate;
    }
}
