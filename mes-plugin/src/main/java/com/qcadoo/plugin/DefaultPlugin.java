package com.qcadoo.plugin;

import static com.qcadoo.plugin.PluginState.DISABLED;
import static com.qcadoo.plugin.PluginState.ENABLED;
import static com.qcadoo.plugin.PluginState.ENABLING;
import static com.qcadoo.plugin.PluginState.TEMPORARY;
import static com.qcadoo.plugin.PluginState.UNKNOWN;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import com.qcadoo.plugin.dependency.PluginDependencyInformation;

public class DefaultPlugin implements Plugin {

    private PluginState state = UNKNOWN;

    private final String identifier;

    private final Set<Module> modules;

    private final PluginInformation information;

    private final Set<PluginDependencyInformation> dependencies;

    private final boolean system;

    private final String filename;

    private DefaultPlugin(final String identifier, final String filename, final boolean system, final Set<Module> modules,
            final PluginInformation information, final Set<PluginDependencyInformation> dependencies) {
        this.identifier = identifier;
        this.filename = filename;
        this.modules = modules;
        this.information = information;
        this.dependencies = dependencies;
        this.system = system;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public PluginInformation getPluginInformation() {
        return information;
    }

    @Override
    public PluginState getPluginState() {
        return state;
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
    public boolean hasState(final PluginState expectedState) {
        return state.equals(expectedState);
    }

    @Override
    public void changeStateTo(final PluginState targetState) {
        if (!isTransitionPossible(this.state, targetState)) {
            throw new IllegalStateException("Cannot change state of plugin " + this + " from " + state + " to " + targetState);
        }

        if (!state.equals(UNKNOWN) && targetState.equals(ENABLED)) {
            for (Module module : modules) {
                module.enable();
            }
        } else if (!state.equals(UNKNOWN) && targetState.equals(DISABLED)) {
            for (Module module : modules) {
                module.disable();
            }
        }

        state = targetState;
    }

    private boolean isTransitionPossible(final PluginState from, final PluginState to) {
        if (from == null || to == null || to.equals(UNKNOWN) || to.equals(from)) {
            return false;
        }

        if (from.equals(UNKNOWN)) {
            return true;
        }

        if (to.equals(TEMPORARY)) {
            return false;
        }

        if (from.equals(ENABLING) && to.equals(DISABLED)) {
            return false;
        }

        if (from.equals(ENABLED) && to.equals(ENABLING)) {
            return false;
        }

        if (from.equals(TEMPORARY) && !to.equals(ENABLING)) {
            return false;
        }

        return true;
    }

    @Override
    public String getFilename() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compareVersion(final Plugin plugin) {
        if (!identifier.equals(plugin.getIdentifier())) {
            throw new IllegalStateException("Cannot compare versions of different plugins " + this + " and " + plugin);
        }

        return VersionUtils.compare(information.getVersion(), plugin.getPluginInformation().getVersion());
    }

    @Override
    public void init() {
        if (state.equals(PluginState.UNKNOWN)) {
            throw new IllegalStateException("Plugin " + getIdentifier() + " is in unknown state, cannot be initialized");
        }

        for (Module module : modules) {
            module.init();
        }
    }

    public static class Builder {

        private final String identifier;

        private int[] version;

        private String filename;

        private String description;

        private String vendor;

        private String vendorUrl;

        private String name;

        private final Set<Module> modules = new HashSet<Module>();

        private final Set<PluginDependencyInformation> dependencyInformations = new HashSet<PluginDependencyInformation>();

        public Builder(final String identifier) {
            this.identifier = identifier;
        }

        public static Builder identifier(final String identifier) {
            return new Builder(identifier);
        }

        public Builder withModule(final Module module) {
            modules.add(module);
            return this;
        }

        public Builder withVersion(final String version) {
            this.version = VersionUtils.parse(version);
            return this;
        }

        public Plugin build() {
            PluginInformation pluginInformation = new PluginInformation(name, description, vendor, vendorUrl, version);
            return new DefaultPlugin(identifier, filename, false, unmodifiableSet(modules), pluginInformation,
                    unmodifiableSet(dependencyInformations));
        }

    }

}
