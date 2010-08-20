package com.qcadoo.mes.core.data.internal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class SimpleDatabaseObject {

    private Long id;

    private String name;

    private int age;

    private boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
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
        return new HashCodeBuilder(17, 37).append(name).append(age).append(id).append(deleted).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleDatabaseObject)) {
            return false;
        }
        SimpleDatabaseObject other = (SimpleDatabaseObject) obj;
        return new EqualsBuilder().append(name, other.name).append(age, other.age).append(id, other.id)
                .append(deleted, other.deleted).isEquals();
    }

}
