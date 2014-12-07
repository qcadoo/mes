package com.qcadoo.mes.technologies.tree.domain;

import com.qcadoo.mes.basic.domain.ImmutableIdWrapper;

public final class TechnologyOperationId extends ImmutableIdWrapper {

    public TechnologyOperationId(final Long id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("TechnologyOperationId(%s)", get());
    }
}
