package com.qcadoo.mes.productionPerShift.domain;

import com.qcadoo.mes.basic.domain.ImmutableIdWrapper;

public class ProgressForDayId extends ImmutableIdWrapper {

    public ProgressForDayId(final Long id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("ProgressForDayId(%s)", get());
    }
}
