package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StocktakingModelHooks {

    public void onSave(final DataDefinition stocktakingDD, final Entity stocktaking) {
        if (StorageLocationMode.ALL.getStringValue().equals(stocktaking.getStringField(StocktakingFields.STORAGE_LOCATION_MODE))) {
            stocktaking.setField(StocktakingFields.STORAGE_LOCATIONS, null);
        }
        if (Objects.nonNull(stocktaking.getId())) {
            Entity stocktakingDb = stocktakingDD.get(stocktaking.getId());
            if (!stocktaking.getBelongsToField("location").getId()
                    .equals(stocktakingDb.getBelongsToField("location").getId())) {
                stocktaking.setField("storageLocations", null);
            }
        }
    }
}
