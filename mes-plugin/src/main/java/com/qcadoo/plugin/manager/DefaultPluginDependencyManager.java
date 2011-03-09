package com.qcadoo.plugin.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.qcadoo.plugin.Plugin;
import com.qcadoo.plugin.PluginAccessor;
import com.qcadoo.plugin.PluginDependencyManager;
import com.qcadoo.plugin.PluginDependencyResult;
import com.qcadoo.plugin.PluginInformation;
import com.qcadoo.plugin.PluginState;

public class DefaultPluginDependencyManager implements PluginDependencyManager {

    private final PluginAccessor pluginAccessor;

    public DefaultPluginDependencyManager(final PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public PluginDependencyResult getDependenciesToEnable(final List<Plugin> plugins) {
        return getDependenciesToEnable(plugins, new HashSet<PluginInformation>());
    }

    private PluginDependencyResult getDependenciesToEnable(final List<Plugin> plugins, final Set<PluginInformation> markedNodes) {

        List<PluginInformation> disabledDependencies = new ArrayList<PluginInformation>();
        List<PluginInformation> unsatisfiedDependencies = new ArrayList<PluginInformation>();

        List<Plugin> dependencyPlugins = new LinkedList<Plugin>();

        boolean isCyclic = false;

        for (Plugin plugin : plugins) {

            markedNodes.add(plugin.getPluginInformation());

            for (PluginInformation requiredPluginInformation : plugin.getRequiredPlugins()) {

                Plugin requiredPlugin = pluginAccessor.getPlugin(requiredPluginInformation.getPluginKey());

                if (requiredPlugin != null) {
                    if (isPluginDisabled(requiredPlugin)) {

                        if (markedNodes.contains(requiredPlugin.getPluginInformation())) {
                            isCyclic = true;
                        }

                        addToList(disabledDependencies, requiredPlugin.getPluginInformation());
                        dependencyPlugins.add(requiredPlugin);
                    }
                } else {
                    addToList(unsatisfiedDependencies, requiredPluginInformation);
                }
            }

        }

        if (unsatisfiedDependencies.isEmpty()) {
            if (isCyclic) {
                return PluginDependencyResult.cyclicDependencies();
            }
            if (dependencyPlugins.isEmpty()) {
                return PluginDependencyResult.disabledDependencies(disabledDependencies);
            }

            PluginDependencyResult nextLevelDependencioesResult = getDependenciesToEnable(dependencyPlugins, markedNodes);

            if (nextLevelDependencioesResult.getUnsatisfiedDependencies().isEmpty() && !nextLevelDependencioesResult.isCyclic()) {
                disabledDependencies.addAll(nextLevelDependencioesResult.getDisabledDependencies());

                return PluginDependencyResult.disabledDependencies(disabledDependencies);
            } else {
                return nextLevelDependencioesResult;
            }
        } else {
            return PluginDependencyResult.unsatisfiedDependencies(unsatisfiedDependencies);
        }
    }

    @Override
    public PluginDependencyResult getDependenciesToDisable(final List<Plugin> plugins) {

        List<PluginInformation> enabledDependencies = new ArrayList<PluginInformation>();

        Set<PluginInformation> argumentPluginInformationsSet = getArgumentPluginInformationsSet(plugins);

        List<Plugin> dependencyPlugins = new LinkedList<Plugin>();

        for (Plugin plugin : plugins) {
            for (PluginInformation requiredPluginInformation : plugin.getRequiredPlugins()) {
                if (argumentPluginInformationsSet.contains(requiredPluginInformation)) {
                    continue;
                }

                Plugin requiredPlugin = pluginAccessor.getPlugin(requiredPluginInformation.getPluginKey());

                if (PluginState.ENABLED.equals(requiredPlugin.getPluginState())) {
                    addToList(enabledDependencies, requiredPluginInformation);
                    dependencyPlugins.add(requiredPlugin);
                }
            }

        }

        if (dependencyPlugins.isEmpty()) {
            return PluginDependencyResult.enabledDependencies(enabledDependencies);
        }

        PluginDependencyResult nextLevelDependencioesResult = getDependenciesToDisable(dependencyPlugins);
        if (nextLevelDependencioesResult.getUnsatisfiedDependencies().isEmpty()) {
            enabledDependencies.addAll(nextLevelDependencioesResult.getEnabledDependencies());
            return PluginDependencyResult.enabledDependencies(enabledDependencies);
        } else {
            return nextLevelDependencioesResult;
        }
    }

    private Set<PluginInformation> getArgumentPluginInformationsSet(final List<Plugin> plugins) {
        Set<PluginInformation> argumentPluginInformationsSet = new HashSet<PluginInformation>();
        for (Plugin plugin : plugins) {
            argumentPluginInformationsSet.add(plugin.getPluginInformation());
        }
        return argumentPluginInformationsSet;
    }

    private boolean isPluginDisabled(final Plugin plugin) {
        return PluginState.DISABLED.equals(plugin.getPluginState()) || PluginState.TEMPORARY.equals(plugin.getPluginState());
    }

    private void addToList(final List<PluginInformation> list, final PluginInformation element) {
        if (!list.contains(element)) {
            list.add(element);
        }
    }

    @Override
    public List<Plugin> sortPlugins(final Collection<Plugin> plugins) {
        // TODO Auto-generated method stub
        return null;
    }
}
