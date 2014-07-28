package com.qcadoo.mes.basic.shift;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public final class ShiftsDataProviderImpl implements ShiftsDataProvider {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<Shift> findAll() {
        return FluentIterable.from(getAllShifts()).transform(new Function<Entity, Shift>() {

            @Override
            public Shift apply(final Entity shiftEntity) {
                return new Shift(shiftEntity);
            }
        }).toList();
    }

    private List<Entity> getAllShifts() {
        return getShiftDataDefinition().find().list().getEntities();
    }

    private DataDefinition getShiftDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT);
    }
}
