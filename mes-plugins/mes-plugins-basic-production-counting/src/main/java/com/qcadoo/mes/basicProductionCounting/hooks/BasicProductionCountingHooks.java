package com.qcadoo.mes.basicProductionCounting.hooks;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class BasicProductionCountingHooks {

    public void onSave(final DataDefinition dd, final Entity basicProductionCountingQuantity) {
        Entity order = basicProductionCountingQuantity.getBelongsToField(BasicProductionCountingFields.ORDER);
        if (Objects.nonNull(order)) {
            for (Entity pqc : basicProductionCountingQuantity
                    .getHasManyField(BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITIES)) {
                if (Objects.isNull(pqc.getBelongsToField(ProductionCountingQuantityFields.ORDER))) {
                    pqc.setField(BasicProductionCountingFields.ORDER, order);
                }
            }
        }
    }
}
