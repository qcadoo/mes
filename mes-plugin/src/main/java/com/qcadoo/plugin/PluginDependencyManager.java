package com.qcadoo.plugin;

import java.util.Collection;
import java.util.List;

import com.qcadoo.plugin.dependency.PluginDependencyResult;

public interface PluginDependencyManager {

    /**
     * 
     * This method returns a PluginDependencyResult which contains a list of either disabled or unsatisfied dependencies for given
     * plugins depending on the dependencies' states.
     * 
     * @param plugins
     * @return PluginDependencyResult
     */
    PluginDependencyResult getDependenciesToEnable(List<Plugin> plugins);

    /**
     * 
     * This method returns a PluginDependencyResult which contains a list of enabled dependencies for given plugins.
     * 
     * @param plugins
     * @return PluginDependencyResult
     */
    PluginDependencyResult getDependenciesToDisable(List<Plugin> plugins);

    List<Plugin> sortPluginsInDependencyOrder(Collection<Plugin> plugins);

}
