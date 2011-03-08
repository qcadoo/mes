package com.qcadoo.plugin.manager;

import java.util.ArrayList;
import java.util.HashSet;
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

    public PluginDependencyResult getDependenciesToEnable(List<Plugin> plugins) {

        List<PluginInformation> disabledDependencies = new ArrayList<PluginInformation>();
        List<PluginInformation> unsatisfiedDependencies = new ArrayList<PluginInformation>();

        Set<PluginInformation> argumentPluginInformationsSet = getArgumentPluginInformationsSet(plugins);

        for (Plugin plugin : plugins) {

            for (PluginInformation requiredPluginInformation : plugin.getRequiredPlugins()) {
                if (argumentPluginInformationsSet.contains(requiredPluginInformation)) {
                    continue;
                }

                Plugin requiredPlugin = pluginAccessor.getPlugin(requiredPluginInformation.getPluginKey());

                if (requiredPlugin != null) {
                    if (isPluginDisabled(requiredPlugin)) {
                        addToList(disabledDependencies, requiredPlugin.getPluginInformation());
                    }
                } else {
                    addToList(unsatisfiedDependencies, requiredPluginInformation);
                }
            }

        }

        if (unsatisfiedDependencies.size() == 0) {
            return PluginDependencyResult.disabledDependencies(disabledDependencies);
        } else {
            return PluginDependencyResult.unsatisfiedDependencies(unsatisfiedDependencies);
        }
    }

    public PluginDependencyResult getDependenciesToDisable(List<Plugin> plugins) {

        List<PluginInformation> enabledDependencies = new ArrayList<PluginInformation>();

        Set<PluginInformation> argumentPluginInformationsSet = getArgumentPluginInformationsSet(plugins);

        for (Plugin plugin : plugins) {
            for (PluginInformation requiredPluginInformation : plugin.getRequiredPlugins()) {
                if (argumentPluginInformationsSet.contains(requiredPluginInformation)) {
                    continue;
                }

                Plugin requiredPlugin = pluginAccessor.getPlugin(requiredPluginInformation.getPluginKey());

                if (PluginState.ENABLED.equals(requiredPlugin.getPluginState())) {
                    addToList(enabledDependencies, requiredPluginInformation);
                }
            }

        }

        return PluginDependencyResult.enabledDependencies(enabledDependencies);
    }

    private Set<PluginInformation> getArgumentPluginInformationsSet(List<Plugin> plugins) {
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
}
