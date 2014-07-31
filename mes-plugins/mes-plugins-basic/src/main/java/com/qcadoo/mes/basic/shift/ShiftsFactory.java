package com.qcadoo.mes.basic.shift;

import org.springframework.stereotype.Component;

import com.qcadoo.model.api.Entity;

@Component
public class ShiftsFactory {

    public Shift buildFrom(final Entity shiftEntity) {
        return new Shift(shiftEntity);
    }

}
