package com.qcadoo.mes.core.data.internal.controls;

import com.qcadoo.mes.core.data.controls.FieldControl;

public final class StringControl implements FieldControl {

    private final int type;

    public StringControl(final int type) {
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

}
