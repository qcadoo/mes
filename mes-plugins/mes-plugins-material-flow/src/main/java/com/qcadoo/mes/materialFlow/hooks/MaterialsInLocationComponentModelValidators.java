/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.MaterialsInLocationComponentFields.LOCATION;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInLocationComponentFields.MATERIALS_IN_LOCATION;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class MaterialsInLocationComponentModelValidators {

    public boolean checkMaterialFlowComponentUniqueness(final DataDefinition materialsInLocationComponentDD,
            final Entity materialsInLocationComponent) {
        Entity location = materialsInLocationComponent.getBelongsToField(LOCATION);
        Entity materialsInLocation = materialsInLocationComponent.getBelongsToField(MATERIALS_IN_LOCATION);

        if (materialsInLocation == null || location == null) {
            return false;
        }

        SearchResult searchResult = materialsInLocationComponentDD.find().add(SearchRestrictions.belongsTo(LOCATION, location))
                .add(SearchRestrictions.belongsTo(MATERIALS_IN_LOCATION, materialsInLocation)).list();

        if (searchResult.getTotalNumberOfEntities() == 1
                && !searchResult.getEntities().get(0).getId().equals(materialsInLocationComponent.getId())) {
            materialsInLocationComponent.addError(materialsInLocationComponentDD.getField(LOCATION),
                    "materialFlow.validate.global.error.materialsInLocationDuplicated");
            return false;
        } else {
            return true;
        }
    }
}
