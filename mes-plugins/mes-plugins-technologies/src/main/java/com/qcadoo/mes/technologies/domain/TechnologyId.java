package com.qcadoo.mes.technologies.domain;

import com.qcadoo.mes.basic.domain.ImmutableIdWrapper;

public class TechnologyId extends ImmutableIdWrapper {

    public TechnologyId(final Long id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("TechnologyId(%s)", get());
    }
}
