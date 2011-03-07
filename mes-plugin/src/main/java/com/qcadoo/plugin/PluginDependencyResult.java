package com.qcadoo.plugin;

import java.util.Collections;
import java.util.List;

public class PluginDependencyResult {

    private final List<PluginInformation> disabledDependencies;

    private final List<PluginInformation> unsatisfiedDependencies;

    private final List<PluginInformation> enabledDependencies;

    private PluginDependencyResult(final List<PluginInformation> disabledDependencies,
            final List<PluginInformation> unsatisfiedDependencies, final List<PluginInformation> enabledDependencies) {
        this.disabledDependencies = disabledDependencies;
        this.unsatisfiedDependencies = unsatisfiedDependencies;
        this.enabledDependencies = enabledDependencies;
    }

    public List<PluginInformation> getDisabledDependencies() {
        return disabledDependencies;
    }

    public List<PluginInformation> getUnsatisfiedDependencies() {
        return unsatisfiedDependencies;
    }

    public List<PluginInformation> getEnabledDependencies() {
        return enabledDependencies;
    }

    public static PluginDependencyResult disabledDependencies(final List<PluginInformation> disabledDependencies) {
        return new PluginDependencyResult(disabledDependencies, Collections.<PluginInformation> emptyList(),
                Collections.<PluginInformation> emptyList());
    }

    public static PluginDependencyResult satisfiedDependencies() {
        return new PluginDependencyResult(Collections.<PluginInformation> emptyList(),
                Collections.<PluginInformation> emptyList(), Collections.<PluginInformation> emptyList());
    }

    public static PluginDependencyResult unsatisfiedDependencies(final List<PluginInformation> unsatisfiedDependencies) {
        return new PluginDependencyResult(Collections.<PluginInformation> emptyList(), unsatisfiedDependencies,
                Collections.<PluginInformation> emptyList());
    }

    public static PluginDependencyResult enabledDependencies(final List<PluginInformation> enabledDependencies) {
        return new PluginDependencyResult(Collections.<PluginInformation> emptyList(),
                Collections.<PluginInformation> emptyList(), enabledDependencies);
    }

    public boolean isDependenciesSatisfied() {
        return disabledDependencies.isEmpty() && unsatisfiedDependencies.isEmpty() && enabledDependencies.isEmpty();
    }

}
