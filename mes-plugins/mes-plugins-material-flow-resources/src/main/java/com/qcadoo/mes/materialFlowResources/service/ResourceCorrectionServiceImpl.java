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

    @Autowired
    private ResourceStockService resourceStockService;

    @Override
    public boolean createCorrectionForResource(final Entity resource, final BigDecimal newQuantity, Entity newStorageLocation) {
        Entity oldResource = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE)
                .get(resource.getId());
        if (isCorrectionNeeded(oldResource, newQuantity, newStorageLocation)) {

            Entity correction = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION).create();
            BigDecimal oldQuantity = oldQuantity(oldResource);
            correction.setField(ResourceCorrectionFields.BATCH, batch(oldResource));
            correction.setField(ResourceCorrectionFields.LOCATION, location(oldResource));
            correction.setField(ResourceCorrectionFields.OLD_QUANTITY, oldQuantity);
            correction.setField(ResourceCorrectionFields.NEW_QUANTITY, newQuantity);
            correction.setField(ResourceCorrectionFields.OLD_STORAGE_LOCATION, oldStorageLocation(oldResource));
            correction.setField(ResourceCorrectionFields.NEW_STORAGE_LOCATION, newStorageLocation);
            correction.setField(ResourceCorrectionFields.PRODUCT, product(oldResource));
            correction.setField(ResourceCorrectionFields.TIME, time(oldResource));
            correction.setField(ResourceCorrectionFields.NUMBER, numberGeneratorService.generateNumber(
                    MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION));

            correction.setField(ResourceCorrectionFields.RESOURCE, oldResource);

            correction.getDataDefinition().save(correction);

            resource.setField(ResourceFields.QUANTITY, newQuantity);
            resource.setField(ResourceFields.IS_CORRECTED, true);
            resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, calculateQuantityInAdditionalUnit(resource));
            Entity savedResource = resource.getDataDefinition().save(resource);
            if (savedResource.isValid()) {

                BigDecimal difference = newQuantity.subtract(oldQuantity);
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    resourceStockService.addResourceStock(product(oldResource), location(oldResource), difference);
                } else {
                    resourceStockService.removeResourceStock(product(oldResource), location(oldResource), difference.abs());
                }
            }
            return true;
        }
        return false;
    }

    private boolean isCorrectionNeeded(final Entity resource, final BigDecimal newQuantity, final Entity newStorageLocation) {
        Entity oldStorageLocation = oldStorageLocation(resource);
        boolean quantityChanged = newQuantity.compareTo(oldQuantity(resource)) != 0;

        boolean storageLocationChanged = (newStorageLocation != null && oldStorageLocation != null)
                ? (newStorageLocation.getId().compareTo(oldStorageLocation.getId()) != 0)
                : !(newStorageLocation == null && oldStorageLocation == null);
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

    private BigDecimal calculateQuantityInAdditionalUnit(Entity resource) {
        BigDecimal conversion = resource.getDecimalField(ResourceFields.CONVERSION);
        BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);

        return quantity.multiply(conversion);
    }
}
