package com.qcadoo.mes.productionPerShift.domain;

import com.qcadoo.mes.basic.domain.ImmutableIdWrapper;

public class ProductionPerShiftId extends ImmutableIdWrapper {

    public ProductionPerShiftId(final Long id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("ProductionPerShiftId(%s)", get());
    }
}
