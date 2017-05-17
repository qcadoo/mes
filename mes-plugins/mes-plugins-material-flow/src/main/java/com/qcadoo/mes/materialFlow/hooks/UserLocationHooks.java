package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.materialFlow.constants.UserLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Component
public class UserLocationHooks {

    public boolean validatesWith(final DataDefinition userLocationDD, final Entity userLocation) {
        return userLocation.getId() != null || (!isLocationNull(userLocationDD, userLocation)
                && !checkIfUserLocationAlreadyExists(userLocationDD, userLocation));
    }

    private boolean checkIfUserLocationAlreadyExists(final DataDefinition userLocationDD, final Entity userLocation) {
        Entity location = requireNonNull(userLocation.getBelongsToField(UserLocationFields.LOCATION));
        Entity user = requireNonNull(userLocation.getBelongsToField(UserLocationFields.USER));

        boolean exists = 0 != userLocationDD.count(SearchRestrictions.and(belongsTo(UserLocationFields.LOCATION, location),
                belongsTo(UserLocationFields.USER, user)));

        if (exists) {
            userLocation.addError(userLocationDD.getField(UserLocationFields.LOCATION),
                    "qcadooView.validate.field.error.invalidUniqueType");
        }
        return exists;
    }

    private boolean isLocationNull(final DataDefinition userLocationDD, final Entity userLocation) {
        boolean result = userLocation.getBelongsToField(UserLocationFields.LOCATION) == null;
        if (result) {
            userLocation.addError(userLocationDD.getField(UserLocationFields.LOCATION),
                    "qcadooView.validate.field.error.missing");
        }
        return result;
    }

}
