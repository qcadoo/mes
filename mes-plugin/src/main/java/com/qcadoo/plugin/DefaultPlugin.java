package com.qcadoo.plugin;

import java.util.Set;

import com.qcadoo.plugin.dependency.PluginDependencyInformation;

public class DefaultPlugin implements Plugin {

    @Override
    public String getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginInformation getPluginInformation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginState getPluginState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<PluginDependencyInformation> getRequiredPlugins() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSystemPlugin() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasState(final PluginState state) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object changeStateTo(final PluginState disabled) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFilename() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compareVersion(final Plugin plugin) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    public void addModule(final Module module1) {
        // TODO Auto-generated method stub

    }

}
