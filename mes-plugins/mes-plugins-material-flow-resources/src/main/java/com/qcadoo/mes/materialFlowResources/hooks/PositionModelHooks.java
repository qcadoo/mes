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
package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.service.ReservationsService;
import com.qcadoo.mes.materialFlowResources.validators.PositionValidators;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PositionModelHooks {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourceService;

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private PositionValidators positionValidators;

    public void onSave(final DataDefinition positionDD, final Entity position) {
        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
        if (resource != null) {
            position.setField(PositionFields.BATCH, resource.getField(ResourceFields.BATCH));
            position.setField(PositionFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
            position.setField(PositionFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
        }

        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);
        if (document != null) {
            DocumentType type = DocumentType.of(document);
            DocumentState state = DocumentState.of(document);

            position.setField(PositionFields.TYPE, type.getStringValue());
            position.setField(PositionFields.STATE, state.getStringValue());
        }

        // if (positionValidators.validateAvailableQuantity(positionDD, position)) {
        reservationsService.updateReservationFromDocumentPosition(position);
        // }

    }

    public void onCreate(final DataDefinition positionDD, final Entity position) {
        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);
        if (DocumentType.of(document).compareTo(DocumentType.RECEIPT) == 0) {
            Entity warehouse = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_TO);
            position.setField(PositionFields.ATRRIBUTE_VALUES,
                    materialFlowResourceService.getAttributesForPosition(position, warehouse));
        }
        if (positionValidators.validateAvailableQuantity(positionDD, position)) {
            reservationsService.createReservationFromDocumentPosition(position);
        }
    }

}
