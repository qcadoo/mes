package com.qcadoo.plugin;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import com.qcadoo.plugin.dependency.PluginDependencyInformation;

public interface Plugin {

    void addModule(final Module module);

    Module getModule(final String key);

    Collection<Module> getModules();

    String getName();

    PluginInformation getPluginInformation();

    void setPluginInformation(final PluginInformation pluginInformation);

    PluginState getPluginState();

    Set<PluginDependencyInformation> getRequiredPlugins();

    URL getResource(final String path);

    InputStream getResourceAsStream(final String name);

    boolean isSystemPlugin();

    void setSystemPlugin(final boolean system);

    void enable();

    void disable();

    void install();

    void uninstall();

    boolean hasState(PluginState state);

    Object changeStateTo(PluginState disabled);

    String getFilename();

}
