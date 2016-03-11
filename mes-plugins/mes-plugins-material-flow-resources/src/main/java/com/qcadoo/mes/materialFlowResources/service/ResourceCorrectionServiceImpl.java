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
package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceCorrectionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ResourceCorrectionServiceImpl implements ResourceCorrectionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Override
    public boolean createCorrectionForResource(final Long resourceId, final BigDecimal newQuantity, Entity newStorageLocation) {

        Entity resource = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE).get(resourceId);
        if (isCorrectionNeeded(resource, newQuantity, newStorageLocation)) {

            Entity correction = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION).create();
            correction.setField(ResourceCorrectionFields.BATCH, batch(resource));
            correction.setField(ResourceCorrectionFields.LOCATION, location(resource));
            correction.setField(ResourceCorrectionFields.OLD_QUANTITY, oldQuantity(resource));
            correction.setField(ResourceCorrectionFields.NEW_QUANTITY, newQuantity);
            correction.setField(ResourceCorrectionFields.OLD_STORAGE_LOCATION, oldStorageLocation(resource));
            correction.setField(ResourceCorrectionFields.NEW_STORAGE_LOCATION, newStorageLocation);
            correction.setField(ResourceCorrectionFields.PRODUCT, product(resource));
            correction.setField(ResourceCorrectionFields.TIME, time(resource));
            correction.setField(ResourceCorrectionFields.NUMBER, numberGeneratorService.generateNumber(
                    MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION));

            correction.setField(ResourceCorrectionFields.RESOURCE, resource);

            correction.getDataDefinition().save(correction);
            return true;
        }
        return false;
    }

    private boolean isCorrectionNeeded(final Entity resource, final BigDecimal newQuantity, final Entity newStorageLocation) {
        Entity oldStorageLocation = oldStorageLocation(resource);
        boolean quantityChanged = newQuantity.compareTo(oldQuantity(resource)) != 0;

        boolean storageLocationChanged = (newStorageLocation != null && oldStorageLocation != null) ? (newStorageLocation.getId()
                .compareTo(oldStorageLocation.getId()) != 0) : !(newStorageLocation == null && oldStorageLocation == null);
        return quantityChanged || storageLocationChanged;
    }

    private Entity product(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.PRODUCT);
    }

    private BigDecimal oldQuantity(final Entity resource) {
        return resource.getDecimalField(ResourceFields.QUANTITY);
    }

    private Entity location(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.LOCATION);
    }

    private Date time(final Entity resource) {
        return resource.getDateField(ResourceFields.TIME);
    }

    private String batch(final Entity resource) {
        return resource.getStringField(ResourceFields.BATCH);
    }

    private Entity oldStorageLocation(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
    }
}
