package com.qcadoo.plugin.internal;

import static com.qcadoo.plugin.api.PluginState.DISABLED;
import static com.qcadoo.plugin.api.PluginState.ENABLED;
import static com.qcadoo.plugin.api.PluginState.ENABLING;
import static com.qcadoo.plugin.api.PluginState.TEMPORARY;
import static com.qcadoo.plugin.api.PluginState.UNKNOWN;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.PluginInformation;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;
import com.qcadoo.plugin.api.VersionOfDependency;
import com.qcadoo.plugin.internal.api.Module;

public class DefaultPlugin implements Plugin {

    private final List<Module> modules;

    private final PluginInformation information;

    private final Set<PluginDependencyInformation> dependencies;

    private final boolean system;

    private final String identifier;

    private final Version version;

    private PluginState state;

    private DefaultPlugin(final String identifier, final boolean system, final Version version, final List<Module> modules,
            final PluginInformation information, final Set<PluginDependencyInformation> dependencies) {
        this.state = UNKNOWN;
        this.identifier = identifier;
        this.version = version;
        this.modules = modules;
        this.information = information;
        this.dependencies = dependencies;
        this.system = system;
    }

    @Override
    public PluginInformation getPluginInformation() {
        return information;
    }

    @Override
    public Set<PluginDependencyInformation> getRequiredPlugins() {
        return dependencies;
    }

    @Override
    public boolean isSystemPlugin() {
        return system;
    }

    @Override
    public void changeStateTo(final PluginState targetState) {
        if (!isTransitionPossible(getState(), targetState)) {
            throw new IllegalStateException("Cannot change state of plugin " + this + " from " + getState() + " to "
                    + targetState);
        }

        if (!hasState(UNKNOWN) && targetState.equals(ENABLED)) {
            for (Module module : modules) {
                module.enable();
            }
        } else if (!hasState(UNKNOWN) && targetState.equals(DISABLED)) {
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
        return identifier + "-" + version + ".jar";
    }

    @Override
    public int compareVersion(final Version version) {
        return this.version.compareTo(version);
    }

    @Override
    public void init() {
        if (hasState(PluginState.UNKNOWN)) {
            throw new IllegalStateException("Plugin " + getIdentifier() + " is in unknown state, cannot be initialized");
        }

        for (Module module : modules) {
            module.init(getState());
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public static class Builder {

        private final String identifier;

        private Version version;

        private String description;

        private String vendor;

        private String vendorUrl;

        private String name;

        private boolean system;

        private final List<Module> modules = new ArrayList<Module>();

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
            this.version = new Version(version);
            return this;
        }

        public Builder withDependency(final String identifier, final String version) {
            dependencyInformations.add(new PluginDependencyInformation(identifier, new VersionOfDependency(version)));
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder withVendor(final String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder withVendorUrl(final String vendorUrl) {
            this.vendorUrl = vendorUrl;
            return this;
        }

        public Builder asSystem() {
            this.system = true;
            return this;
        }

        public Plugin build() {
            PluginInformation pluginInformation = new PluginInformation(name, description, vendor, vendorUrl);
            return new DefaultPlugin(identifier, system, version, unmodifiableList(modules), pluginInformation,
                    unmodifiableSet(dependencyInformations));
        }

    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public PluginState getState() {
        return state;
    }

    @Override
    public boolean hasState(final PluginState expectedState) {
        return state.equals(expectedState);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultPlugin)) {
            return false;
        }
        DefaultPlugin other = (DefaultPlugin) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
