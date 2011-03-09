package com.qcadoo.plugin.dependency;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.qcadoo.plugin.Plugin;
import com.qcadoo.plugin.PluginAccessor;
import com.qcadoo.plugin.PluginDependencyManager;
import com.qcadoo.plugin.PluginState;

public class DefaultPluginDependencyManager implements PluginDependencyManager {

    private final PluginAccessor pluginAccessor;

    public DefaultPluginDependencyManager(final PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    public PluginDependencyResult getDependenciesToEnable(final List<Plugin> plugins) {
        return getDependenciesToEnable(plugins, new HashSet<String>());
    }

    private PluginDependencyResult getDependenciesToEnable(final List<Plugin> plugins, final Set<String> markedNodes) {

        Set<PluginDependencyInformation> disabledDependencies = new HashSet<PluginDependencyInformation>();
        Set<PluginDependencyInformation> unsatisfiedDependencies = new HashSet<PluginDependencyInformation>();

        Set<String> argumentPluginIdentifiersSet = getArgumentIdentifiersSet(plugins);

        List<Plugin> dependencyPlugins = new LinkedList<Plugin>();

        boolean isCyclic = false;

        for (Plugin plugin : plugins) {

            markedNodes.add(plugin.getIdentifier());

            for (PluginDependencyInformation dependencyInfo : plugin.getRequiredPlugins()) {

                if (markedNodes.contains(dependencyInfo.getKey())) {
                    isCyclic = true;
                    continue;
                }

                Plugin requiredPlugin = pluginAccessor.getPlugin(dependencyInfo.getKey());

                if (requiredPlugin == null) {
                    if (!argumentPluginIdentifiersSet.contains(dependencyInfo.getKey())) {
                        unsatisfiedDependencies.add(dependencyInfo);
                    }
                    continue;
                }

                if (!isPluginDisabled(requiredPlugin)) {
                    continue;
                }

                if (!dependencyInfo.isVersionSattisfied(requiredPlugin.getPluginInformation().getVersion())) {
                    unsatisfiedDependencies.add(dependencyInfo);
                } else {
                    disabledDependencies.add(dependencyInfo);
                    dependencyPlugins.add(requiredPlugin);
                }

            }
        }

        if (isCyclic) {
            return PluginDependencyResult.cyclicDependencies();
        }

        if (!unsatisfiedDependencies.isEmpty()) {
            return PluginDependencyResult.unsatisfiedDependencies(unsatisfiedDependencies);
        }

        if (disabledDependencies.isEmpty()) {
            return PluginDependencyResult.satisfiedDependencies();
        }

        PluginDependencyResult nextLevelDependencioesResult = getDependenciesToEnable(dependencyPlugins, markedNodes);

        if (!nextLevelDependencioesResult.getUnsatisfiedDependencies().isEmpty() || nextLevelDependencioesResult.isCyclic()) {
            return nextLevelDependencioesResult;
        }

        disabledDependencies.addAll(nextLevelDependencioesResult.getDisabledDependencies());
        return PluginDependencyResult.disabledDependencies(disabledDependencies);
    }

    public PluginDependencyResult getDependenciesToDisable(final List<Plugin> plugins) {

        return null;
    }

    public List<Plugin> sortPluginsInDependencyOrder(final Collection<Plugin> plugins) {
        return null;
    }

    private Set<String> getArgumentIdentifiersSet(final List<Plugin> plugins) {
        Set<String> argumentPluginInformationsSet = new HashSet<String>();
        for (Plugin plugin : plugins) {
            argumentPluginInformationsSet.add(plugin.getIdentifier());
        }
        return argumentPluginInformationsSet;
    }

    private boolean isPluginDisabled(final Plugin plugin) {
        return PluginState.DISABLED.equals(plugin.getPluginState()) || PluginState.TEMPORARY.equals(plugin.getPluginState());
    }

}
