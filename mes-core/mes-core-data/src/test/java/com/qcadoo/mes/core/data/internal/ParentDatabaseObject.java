package com.qcadoo.mes.core.data.internal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class ParentDatabaseObject {

    private Long id;

    private String name;

    private boolean deleted;

    public ParentDatabaseObject() {
    }

    public ParentDatabaseObject(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 37).append(name).append(id).append(deleted).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ParentDatabaseObject)) {
            return false;
        }
        ParentDatabaseObject other = (ParentDatabaseObject) obj;
        return new EqualsBuilder().append(name, other.name).append(id, other.id).append(deleted, other.deleted).isEquals();
    }

}
