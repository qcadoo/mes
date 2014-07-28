package com.qcadoo.mes.productionPerShift.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.productionPerShift.dates.ProgressDatesService;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionPerShiftHooks {

    @Autowired
    private ProgressDatesService progressDatesService;

    public void onSave(final DataDefinition dataDefinition, final Entity pps) {
        Optional<Entity> maybeOrder = Optional.fromNullable(pps.getBelongsToField(ProductionPerShiftFields.ORDER));
        for (Entity order : maybeOrder.asSet()) {
            progressDatesService.setUpDatesFor(order);
        }
    }
}
