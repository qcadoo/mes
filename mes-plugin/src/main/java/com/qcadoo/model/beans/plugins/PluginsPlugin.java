package com.qcadoo.model.beans.plugins;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import com.qcadoo.plugin.api.PersistentPlugin;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;
import com.qcadoo.plugin.internal.VersionType;

@Entity
@Table(name = "plugins_plugin")
@TypeDef(name = "version", defaultForType = Version.class, typeClass = VersionType.class)
public class PluginsPlugin implements PersistentPlugin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String identifier;

    @Column
    private Version version;

    @Column
    @Enumerated(EnumType.STRING)
    private PluginState state;

    public PluginsPlugin() {
    }

    public PluginsPlugin(final String identifier, final PluginState state, final Version version) {
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
    public PluginState getState() {
        return state;
    }

    protected void setState(final PluginState state) {
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

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (!(obj instanceof PluginsPlugin)) {
            return false;
        }
        PluginsPlugin other = (PluginsPlugin) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
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