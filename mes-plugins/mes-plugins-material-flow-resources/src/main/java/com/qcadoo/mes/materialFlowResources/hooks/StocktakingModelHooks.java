package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingState;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StocktakingModelHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private StocktakingStateChangeDescriber describer;

    public void onCreate(final DataDefinition stocktakingDD, final Entity stocktaking) {
        setInitialState(stocktaking);
    }

    private void setInitialState(final Entity stocktaking) {
        stateChangeEntityBuilder.buildInitial(describer, stocktaking, StocktakingState.DRAFT);
    }

    public void onSave(final DataDefinition stocktakingDD, final Entity stocktaking) {
        if (StorageLocationMode.ALL.getStringValue().equals(stocktaking.getStringField(StocktakingFields.STORAGE_LOCATION_MODE))) {
            stocktaking.setField(StocktakingFields.STORAGE_LOCATIONS, null);
        }
        if (Objects.nonNull(stocktaking.getId())) {
            Entity stocktakingDb = stocktakingDD.get(stocktaking.getId());
            if (!stocktaking.getBelongsToField(StocktakingFields.LOCATION).getId()
                    .equals(stocktakingDb.getBelongsToField(StocktakingFields.LOCATION).getId())) {
                stocktaking.setField(StocktakingFields.STORAGE_LOCATIONS, null);
            }
        }
    }

    public void onCopy(final DataDefinition stocktakingDD, final Entity stocktaking) {
        setInitialState(stocktaking);
        stocktaking.setField(StocktakingFields.WMS, false);
    }
}
