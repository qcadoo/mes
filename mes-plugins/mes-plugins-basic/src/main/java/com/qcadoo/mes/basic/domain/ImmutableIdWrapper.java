package com.qcadoo.mes.basic.domain;

import java.util.Objects;

public abstract class ImmutableIdWrapper {

    private final Long id;

    public ImmutableIdWrapper(final Long id) {
        this.id = id;
    }

    public Long get() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ImmutableIdWrapper rhs = (ImmutableIdWrapper) obj;
        return Objects.equals(this.id, rhs.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
