/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.warehouseMinimalState.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.warehouseMinimalState.constants.WarehouseMinimumStateFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class WarehouseMinimumStateHooks {

    public boolean validatesWith(final DataDefinition locationMinimumStateDD, final Entity locationMinimumState) {

        Entity lms = getLocationMinimumStateByProductAndLocation(locationMinimumStateDD,
                locationMinimumState.getBelongsToField(WarehouseMinimumStateFields.PRODUCT),
                locationMinimumState.getBelongsToField(WarehouseMinimumStateFields.LOCATION));
        if (lms == null) {
            return true;
        } else if (locationMinimumState.getId() == null) {
            locationMinimumState.addError(locationMinimumStateDD.getField(WarehouseMinimumStateFields.LOCATION),
                    "warehouseMinimalState.locationMinimumState.location.error.minimalStateExist");
            return false;
        } else if (!locationMinimumState.getId().equals(lms.getId())) {
            locationMinimumState.addError(locationMinimumStateDD.getField(WarehouseMinimumStateFields.LOCATION),
                    "warehouseMinimalState.locationMinimumState.location.error.minimalStateExist");
            return false;
        }
        return true;
    }

    private Entity getLocationMinimumStateByProductAndLocation(final DataDefinition locationMinimumStateDD, final Entity product,
            final Entity location) {
        return locationMinimumStateDD.find().add(SearchRestrictions.belongsTo(WarehouseMinimumStateFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(WarehouseMinimumStateFields.LOCATION, location)).setMaxResults(1)
                .uniqueResult();
    }

}
