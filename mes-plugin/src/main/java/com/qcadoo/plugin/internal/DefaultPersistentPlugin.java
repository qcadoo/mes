package com.qcadoo.plugin.internal;

import javax.persistence.Basic;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.hibernate.annotations.Entity;
import org.hibernate.annotations.Table;

import com.qcadoo.plugin.api.PersistentPlugin;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;

@Entity
@Table(appliesTo = "plugins_plugin")
public class DefaultPersistentPlugin implements PersistentPlugin {

    private String identifier;

    @Transient
    private Version version;

    private PluginState state;

    public DefaultPersistentPlugin() {
    }

    public DefaultPersistentPlugin(final String identifier, final PluginState state, final Version version) {
        this.identifier = identifier;
        this.version = version;
        this.state = state;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    protected void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    @Enumerated(EnumType.STRING)
    public PluginState getPluginState() {
        return state;
    }

    protected void setPluginState(final PluginState state) {
        this.state = state;
    }

    @Override
    public boolean hasState(final PluginState expectedState) {
        return state.equals(expectedState);
    }

    @Override
    public Version getVersion() {
        return version;
    }

    protected void setVersion(final Version version) {
        this.version = version;
    }

    @Basic
    public String getStringVersion() {
        return version.toString();
    }

    public void setStringVersion(final String version) {
        this.version = new Version(version);
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
        if (!(obj instanceof DefaultPersistentPlugin)) {
            return false;
        }
        DefaultPersistentPlugin other = (DefaultPersistentPlugin) obj;
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