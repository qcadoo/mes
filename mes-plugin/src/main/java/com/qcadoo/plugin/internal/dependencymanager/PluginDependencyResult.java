package com.qcadoo.plugin.internal.dependencymanager;

import java.util.Collections;
import java.util.Set;

import com.qcadoo.plugin.api.PluginDependencyInformation;

public final class PluginDependencyResult {

    private final Set<PluginDependencyInformation> disabledDependencies;

    private final Set<PluginDependencyInformation> unsatisfiedDependencies;

    private final Set<PluginDependencyInformation> enabledDependencies;

    private boolean cycleExists;

    private PluginDependencyResult(final Set<PluginDependencyInformation> disabledDependencies,
            final Set<PluginDependencyInformation> unsatisfiedDependencies,
            final Set<PluginDependencyInformation> enabledDependencies) {
        this.disabledDependencies = disabledDependencies;
        this.unsatisfiedDependencies = unsatisfiedDependencies;
        this.enabledDependencies = enabledDependencies;
    }

    private PluginDependencyResult(final boolean cycleExists) {
        this(Collections.<PluginDependencyInformation> emptySet(), Collections.<PluginDependencyInformation> emptySet(),
                Collections.<PluginDependencyInformation> emptySet());
        this.cycleExists = cycleExists;
    }

    public Set<PluginDependencyInformation> getDisabledDependencies() {
        return disabledDependencies;
    }

    public Set<PluginDependencyInformation> getUnsatisfiedDependencies() {
        return unsatisfiedDependencies;
    }

    public Set<PluginDependencyInformation> getEnabledDependencies() {
        return enabledDependencies;
    }

    public static PluginDependencyResult disabledDependencies(final Set<PluginDependencyInformation> disabledDependencies) {
        return new PluginDependencyResult(disabledDependencies, Collections.<PluginDependencyInformation> emptySet(),
                Collections.<PluginDependencyInformation> emptySet());
    }

    public static PluginDependencyResult satisfiedDependencies() {
        return new PluginDependencyResult(Collections.<PluginDependencyInformation> emptySet(),
                Collections.<PluginDependencyInformation> emptySet(), Collections.<PluginDependencyInformation> emptySet());
    }

    public static PluginDependencyResult unsatisfiedDependencies(final Set<PluginDependencyInformation> unsatisfiedDependencies) {
        return new PluginDependencyResult(Collections.<PluginDependencyInformation> emptySet(), unsatisfiedDependencies,
                Collections.<PluginDependencyInformation> emptySet());
    }

    public static PluginDependencyResult enabledDependencies(final Set<PluginDependencyInformation> enabledDependencies) {
        return new PluginDependencyResult(Collections.<PluginDependencyInformation> emptySet(),
                Collections.<PluginDependencyInformation> emptySet(), enabledDependencies);
    }

    public static PluginDependencyResult cyclicDependencies() {
        return new PluginDependencyResult(true);
    }

    public boolean isDependenciesSatisfied() {
        return disabledDependencies.isEmpty() && unsatisfiedDependencies.isEmpty() && enabledDependencies.isEmpty()
                && !isCyclic();
    }

    public boolean isCyclic() {
        return cycleExists;
    }
}
