package com.qcadoo.plugin.dependency;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

        boolean isCyclic = false;

        for (Plugin plugin : plugins) {

            if (markedNodes.contains(plugin.getIdentifier())) {
                continue;
            }

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

                    PluginDependencyResult nextLevelDependencioesResult = getDependenciesToEnable(
                            Collections.singletonList(requiredPlugin), markedNodes);

                    if (!nextLevelDependencioesResult.getUnsatisfiedDependencies().isEmpty()
                            || nextLevelDependencioesResult.isCyclic()) {
                        return nextLevelDependencioesResult;
                    }

                    disabledDependencies.addAll(nextLevelDependencioesResult.getDisabledDependencies());
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

        Iterator<PluginDependencyInformation> dependencyInfoIterator = disabledDependencies.iterator();
        while (dependencyInfoIterator.hasNext()) {
            if (argumentPluginIdentifiersSet.contains(dependencyInfoIterator.next().getKey())) {
                dependencyInfoIterator.remove();
            }
        }

        return PluginDependencyResult.disabledDependencies(disabledDependencies);
    }

    public PluginDependencyResult getDependenciesToDisable(final List<Plugin> plugins) {

        Collection<Plugin> systemPlugins = pluginAccessor.getPlugins();

        List<Plugin> enabledDependencyPlugins = new LinkedList<Plugin>();

        Set<String> argumentPluginIdentifiersSet = getArgumentIdentifiersSet(plugins);

        for (Plugin plugin : systemPlugins) {
            if (!PluginState.ENABLED.equals(plugin.getPluginState())) {
                continue;
            }
            for (Plugin pluginToDisable : plugins) {

                if (plugin.getRequiredPlugins().contains(new PluginDependencyInformation(pluginToDisable.getIdentifier()))) {
                    enabledDependencyPlugins.add(plugin);
                }
            }
        }

        Set<PluginDependencyInformation> enabledDependencies = new HashSet<PluginDependencyInformation>();
        for (Plugin plugin : enabledDependencyPlugins) {
            enabledDependencies.add(new PluginDependencyInformation(plugin.getIdentifier()));
        }

        if (!enabledDependencyPlugins.isEmpty()) {
            PluginDependencyResult nextLevelDependencioesResult = getDependenciesToDisable(enabledDependencyPlugins);
            enabledDependencies.addAll(nextLevelDependencioesResult.getEnabledDependencies());
        }

        Iterator<PluginDependencyInformation> dependencyInfoIterator = enabledDependencies.iterator();
        while (dependencyInfoIterator.hasNext()) {
            if (argumentPluginIdentifiersSet.contains(dependencyInfoIterator.next().getKey())) {
                dependencyInfoIterator.remove();
            }
        }

        return PluginDependencyResult.enabledDependencies(enabledDependencies);
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
