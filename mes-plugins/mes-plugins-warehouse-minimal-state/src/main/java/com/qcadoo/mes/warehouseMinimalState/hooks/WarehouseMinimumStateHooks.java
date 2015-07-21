package com.qcadoo.mes.warehouseMinimalState.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

@Service
public class WarehouseMinimumStateHooks {

    public boolean validatesWith(final DataDefinition locationMinimumStateDD, final Entity locationMinimumState) {

        Entity lms = getLocationMinimumStateByProductAndLocation(locationMinimumStateDD,
                locationMinimumState.getBelongsToField("product"), locationMinimumState.getBelongsToField("location"));
        if (lms == null) {
            return true;
        } else if (locationMinimumState.getId() == null && lms != null) {
            locationMinimumState.addError(locationMinimumStateDD.getField("location"),
                    "warehouseMinimalState.locationMinimumState.location.error.minimalStateExist");
            return false;
        } else if (!locationMinimumState.getId().equals(lms.getId())) {
            locationMinimumState.addError(locationMinimumStateDD.getField("location"),
                    "warehouseMinimalState.locationMinimumState.location.error.minimalStateExist");
            return false;
        }
        return true;
    }

    private Entity getLocationMinimumStateByProductAndLocation(final DataDefinition locationMinimumStateDD, final Entity product,
            final Entity location) {
        return locationMinimumStateDD.find().add(SearchRestrictions.belongsTo("product", product))
                .add(SearchRestrictions.belongsTo("location", location)).setMaxResults(1).uniqueResult();
    }

}
