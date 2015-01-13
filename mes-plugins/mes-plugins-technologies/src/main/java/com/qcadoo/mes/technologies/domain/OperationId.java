package com.qcadoo.mes.technologies.domain;

import com.qcadoo.mes.basic.domain.ImmutableIdWrapper;

public class OperationId extends ImmutableIdWrapper {

    public OperationId(final Long id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("OperationId(%s)", get());
    }
}
