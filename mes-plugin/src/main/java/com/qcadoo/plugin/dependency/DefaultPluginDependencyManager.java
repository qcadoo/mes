package com.qcadoo.plugin.dependency;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qcadoo.plugin.PersistentPlugin;
import com.qcadoo.plugin.Plugin;
import com.qcadoo.plugin.PluginAccessor;
import com.qcadoo.plugin.PluginDependencyManager;
import com.qcadoo.plugin.PluginState;

public class DefaultPluginDependencyManager implements PluginDependencyManager {

    private final PluginAccessor pluginAccessor;

    public DefaultPluginDependencyManager(final PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
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
                if (markedNodes.contains(dependencyInfo.getDependencyPluginIdentifier())) {
                    isCyclic = true;
                    continue;
                }

                Plugin requiredPlugin = pluginAccessor.getPlugin(dependencyInfo.getDependencyPluginIdentifier());

                if (requiredPlugin == null) {
                    if (!argumentPluginIdentifiersSet.contains(dependencyInfo.getDependencyPluginIdentifier())) {
                        unsatisfiedDependencies.add(dependencyInfo);
                    }
                    continue;
                }

                if (!isPluginDisabled(requiredPlugin)) {
                    continue;
                }

                if (!dependencyInfo.isVersionSattisfied(requiredPlugin.getVersion())) {
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
            if (argumentPluginIdentifiersSet.contains(dependencyInfoIterator.next().getDependencyPluginIdentifier())) {
                dependencyInfoIterator.remove();
            }
        }

        return PluginDependencyResult.disabledDependencies(disabledDependencies);
    }

    @Override
    public PluginDependencyResult getDependenciesToDisable(final List<Plugin> plugins) {

        Collection<Plugin> systemPlugins = pluginAccessor.getPlugins();

        List<Plugin> enabledDependencyPlugins = new LinkedList<Plugin>();

        Set<String> argumentPluginIdentifiersSet = getArgumentIdentifiersSet(plugins);

        for (Plugin plugin : systemPlugins) {
            if (!PluginState.ENABLED.equals(plugin.getPluginState())) {
                continue;
            }
            for (PersistentPlugin pluginToDisable : plugins) {

                if (plugin.getRequiredPlugins().contains(new PluginDependencyInformation(pluginToDisable.getIdentifier()))) {
                    enabledDependencyPlugins.add(plugin);
                }
            }
        }

        Set<PluginDependencyInformation> enabledDependencies = new HashSet<PluginDependencyInformation>();
        for (PersistentPlugin plugin : enabledDependencyPlugins) {
            enabledDependencies.add(new PluginDependencyInformation(plugin.getIdentifier()));
        }

        if (!enabledDependencyPlugins.isEmpty()) {
            PluginDependencyResult nextLevelDependencioesResult = getDependenciesToDisable(enabledDependencyPlugins);
            enabledDependencies.addAll(nextLevelDependencioesResult.getEnabledDependencies());
        }

        Iterator<PluginDependencyInformation> dependencyInfoIterator = enabledDependencies.iterator();
        while (dependencyInfoIterator.hasNext()) {
            if (argumentPluginIdentifiersSet.contains(dependencyInfoIterator.next().getDependencyPluginIdentifier())) {
                dependencyInfoIterator.remove();
            }
        }

        return PluginDependencyResult.enabledDependencies(enabledDependencies);
    }

    @Override
    public List<Plugin> sortPluginsInDependencyOrder(final Collection<Plugin> plugins) {

        Map<String, Set<String>> notInitializedPlugins = createPluginsMapWithDependencies(plugins);

        List<String> initializedPlugins = new LinkedList<String>();

        while (!notInitializedPlugins.isEmpty()) {
            Iterator<Map.Entry<String, Set<String>>> pluginsToInitializedIt = notInitializedPlugins.entrySet().iterator();
            while (pluginsToInitializedIt.hasNext()) {
                Map.Entry<String, Set<String>> pluginToInitializeEntry = pluginsToInitializedIt.next();
                if (pluginToInitializeEntry.getValue().isEmpty()) {
                    initializedPlugins.add(pluginToInitializeEntry.getKey());
                    pluginsToInitializedIt.remove();
                    for (Set<String> dependencies : notInitializedPlugins.values()) {
                        dependencies.remove(pluginToInitializeEntry.getKey());
                    }
                }
            }
        }

        return convertIdentifiersToPlugins(initializedPlugins);
    }

    private Map<String, Set<String>> createPluginsMapWithDependencies(final Collection<Plugin> plugins) {
        Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
        for (PersistentPlugin plugin : plugins) {
            resultMap.put(plugin.getIdentifier(), null);
        }
        for (Plugin plugin : plugins) {
            Set<String> dependencyIdentifiers = new HashSet<String>();
            for (PluginDependencyInformation dependency : plugin.getRequiredPlugins()) {
                if (resultMap.containsKey(dependency.getDependencyPluginIdentifier())) {
                    dependencyIdentifiers.add(dependency.getDependencyPluginIdentifier());
                }
            }
            resultMap.put(plugin.getIdentifier(), dependencyIdentifiers);
        }
        return resultMap;
    }

    private List<Plugin> convertIdentifiersToPlugins(final Collection<String> pluginIdetifiers) {
        List<Plugin> resultList = new LinkedList<Plugin>();
        for (String pluginIdentifier : pluginIdetifiers) {
            resultList.add(pluginAccessor.getPlugin(pluginIdentifier));
        }
        return resultList;
    }

    // public List<Plugin> sortPluginsInDependencyOrder(final Collection<Plugin> plugins) {
    // List<Plugin> sortedPlugins = new LinkedList<Plugin>();
    //
    // for (Plugin plugin : plugins) {
    // sortedPlugins.add(plugin);
    // Collection<Plugin> requiredPlugins = getPluginsBasedOnDependencyInfo(plugin.getRequiredPlugins());
    // for (Plugin requiredPlugin : requiredPlugins) {
    // Collection<Plugin> dependencyPlugins = getPluginsBasedOnDependencyInfo(requiredPlugin.getRequiredPlugins());
    // if (!dependencyPlugins.isEmpty()) {
    // List<Plugin> recursivelySortedList = sortPluginsInDependencyOrder(dependencyPlugins);
    // sortedPlugins.addAll(recursivelySortedList);
    // }
    // }
    // }
    //
    // // TODO: this one could be invoked only once at the very end to boost performance
    // removeDuplicateWithOrder(sortedPlugins);
    //
    // return sortedPlugins;
    // }
    //
    // private void removeDuplicateWithOrder(List<Plugin> list) {
    //
    // Collections.reverse(list);
    //
    // Set<Plugin> set = new HashSet<Plugin>();
    // List<Plugin> sortedList = new LinkedList<Plugin>();
    //
    // for (Plugin plugin : list) {
    // if (set.add(plugin)) {
    // sortedList.add(plugin);
    // }
    // }
    //
    // list.clear();
    // list.addAll(sortedList);
    // }
    //
    // private Collection<Plugin> getPluginsBasedOnDependencyInfo(Set<PluginDependencyInformation> requiredPlugins) {
    // List<Plugin> plugins = new ArrayList<Plugin>();
    //
    // for (PluginDependencyInformation dependencyInfo : requiredPlugins) {
    // plugins.add(pluginAccessor.getPlugin(dependencyInfo.getKey()));
    // }
    //
    // return plugins;
    // }

    private Set<String> getArgumentIdentifiersSet(final List<Plugin> plugins) {
        Set<String> argumentPluginInformationsSet = new HashSet<String>();
        for (PersistentPlugin plugin : plugins) {
            argumentPluginInformationsSet.add(plugin.getIdentifier());
        }
        return argumentPluginInformationsSet;
    }

    private boolean isPluginDisabled(final PersistentPlugin plugin) {
        return PluginState.DISABLED.equals(plugin.getPluginState()) || PluginState.TEMPORARY.equals(plugin.getPluginState());
    }

}
