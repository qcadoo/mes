package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class StocktakingPositionHooks {

    public void onSave(final DataDefinition stocktakingPositionDD, final Entity stocktakingPosition) {
        if (stocktakingPosition.getDecimalField(StocktakingPositionFields.STOCK) == null) {
            stocktakingPosition.setField(StocktakingPositionFields.STOCK, 0);
        }
    }
}
