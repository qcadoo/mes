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
package com.qcadoo.mes.materialFlowResources.validators;

import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.RepackingPositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class RepackingPositionValidators {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity repackingPosition) {
        Entity resource = repackingPosition.getBelongsToField(PositionFields.RESOURCE);


        if (repackingPosition.getDecimalField(RepackingPositionFields.QUANTITY).compareTo(resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY)) > 0) {
            repackingPosition.addError(dataDefinition.getField(RepackingPositionFields.QUANTITY),
                    "materialFlowResources.error.repackingPosition.quantity.greaterThenResourceAvailableQuantity");

            return false;
        }

        return true;
    }

}