package com.qcadoo.plugin;

import java.util.Set;

import com.qcadoo.plugin.dependency.PluginDependencyInformation;

public interface Plugin {

    // void addModule(final Module module);

    // Module getModule(final String key);

    // Collection<Module> getModules();

    String getIdentifier();

    PluginInformation getPluginInformation();

    void setPluginInformation(final PluginInformation pluginInformation);

    PluginState getPluginState();

    Set<PluginDependencyInformation> getRequiredPlugins();

    boolean isSystemPlugin();

    // URL getResource(final String path);
    //
    // InputStream getResourceAsStream(final String name);
    //
    // void setSystemPlugin(final boolean system);
    //
    // void enable();
    //
    // void disable();
    //
    // void install();
    //
    // void uninstall();

    boolean hasState(PluginState state);

    Object changeStateTo(PluginState disabled);

    String getFilename();

    int compareVersion(Plugin plugin);

    void init();

}
