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

import com.google.common.base.Strings;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceCorrectionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.*;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class ResourceCorrectionServiceImpl implements ResourceCorrectionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    @Override
    @Transactional
    public Optional<Entity> createCorrectionForResource(final Entity resource, boolean fromAttribute) {
        Entity oldResource = getResourceDD().get(resource.getId());

        BigDecimal newQuantity = resource.getDecimalField(ResourceFields.QUANTITY);
        BigDecimal newPrice = resource.getDecimalField(ResourceFields.PRICE);
        BigDecimal newConversion = resource.getDecimalField(ResourceFields.CONVERSION);
        Entity newStorageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
        Entity newBatch = resource.getBelongsToField(ResourceFields.BATCH);
        Entity newTypeOfLoadUnit = resource.getBelongsToField(ResourceFields.TYPE_OF_LOAD_UNIT);
        Date newExpirationDate = resource.getDateField(ResourceFields.EXPIRATION_DATE);
        Entity newPalletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
        String qualityRating = resource.getStringField(ResourceFields.QUALITY_RATING);

        if (isCorrectionNeeded(oldResource, newQuantity, newStorageLocation, newPrice, newBatch, newTypeOfLoadUnit, newPalletNumber, newExpirationDate, newConversion, qualityRating) || fromAttribute) {
            Entity oldStorageLocation = oldResource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
            Entity oldPalletNumber = oldResource.getBelongsToField(ResourceFields.PALLET_NUMBER);

            if (Objects.nonNull(newStorageLocation) && (isEntityChanged(oldStorageLocation, newStorageLocation) || isEntityChanged(oldPalletNumber, newPalletNumber))) {
                if (palletValidatorService.checkMaximumNumberOfPallets(newStorageLocation, resource)) {
                    resource.addGlobalError("materialFlow.error.correction.invalidStorageLocation");
                    resource.setNotValid();

                    return Optional.empty();
                }
            }

            Entity resourceCorrection = createResourceCorrection(oldResource, newQuantity, newPrice, newConversion, newStorageLocation, newBatch, newTypeOfLoadUnit, newExpirationDate, newPalletNumber, qualityRating);

            resource.setField(ResourceFields.QUANTITY, newQuantity);
            resource.setField(ResourceFields.IS_CORRECTED, true);
            resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, calculateQuantityInAdditionalUnit(resource, oldResource.getStringField(ResourceFields.GIVEN_UNIT)));
            resource.setField(ResourceFields.AVAILABLE_QUANTITY, newQuantity.subtract(resource.getDecimalField(ResourceFields.RESERVED_QUANTITY)));

            Entity savedResource = resource.getDataDefinition().save(resource);

            if (savedResource.isValid()) {
                Entity savedCorrection = resourceCorrection.getDataDefinition().save(resourceCorrection);

                if (!savedCorrection.isValid()) {
                    throw new IllegalStateException("Could not save correction");
                }

                return Optional.of(savedCorrection);
            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private Entity createResourceCorrection(final Entity oldResource, final BigDecimal newQuantity,
                                            final BigDecimal newPrice, final BigDecimal newConversion,
                                            final Entity newStorageLocation, final Entity newBatch,
                                            final Entity newTypeOfLoadUnit, final Date newExpirationDate,
                                            final Entity newPalletNumber, final String qualityRating) {
        Entity resourceCorrection = getResourceCorrectionDD().create();

        resourceCorrection.setField(ResourceCorrectionFields.OLD_BATCH, oldResource.getBelongsToField(ResourceFields.BATCH));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_BATCH, newBatch);
        resourceCorrection.setField(ResourceCorrectionFields.LOCATION, oldResource.getBelongsToField(ResourceFields.LOCATION));
        resourceCorrection.setField(ResourceCorrectionFields.OLD_QUANTITY, oldResource.getDecimalField(ResourceFields.QUANTITY));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_QUANTITY, newQuantity);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_PRICE, oldResource.getDecimalField(ResourceFields.PRICE));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_PRICE, newPrice);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_STORAGE_LOCATION, oldResource.getBelongsToField(ResourceFields.STORAGE_LOCATION));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_STORAGE_LOCATION, newStorageLocation);
        resourceCorrection.setField(ResourceCorrectionFields.PRODUCT, oldResource.getBelongsToField(ResourceFields.PRODUCT));
        resourceCorrection.setField(ResourceCorrectionFields.TIME, oldResource.getDateField(ResourceFields.TIME));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_TYPE_OF_LOAD_UNIT, newTypeOfLoadUnit);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_TYPE_OF_LOAD_UNIT, oldResource.getBelongsToField(ResourceFields.TYPE_OF_LOAD_UNIT));
        resourceCorrection.setField(ResourceCorrectionFields.OLD_EXPIRATION_DATE, oldResource.getDateField(ResourceFields.EXPIRATION_DATE));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_EXPIRATION_DATE, newExpirationDate);
        resourceCorrection.setField(ResourceCorrectionFields.NEW_PALLET_NUMBER, newPalletNumber);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_PALLET_NUMBER, oldResource.getBelongsToField(ResourceFields.PALLET_NUMBER));
        resourceCorrection.setField(ResourceCorrectionFields.PRODUCTION_DATE, oldResource.getDateField(ResourceFields.PRODUCTION_DATE));
        resourceCorrection.setField(ResourceCorrectionFields.OLD_CONVERSION, oldResource.getDecimalField(ResourceFields.CONVERSION));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_CONVERSION, newConversion);
        resourceCorrection.setField(ResourceCorrectionFields.NUMBER, numberGeneratorService.generateNumber(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION));

        resourceCorrection.setField(ResourceCorrectionFields.RESOURCE, oldResource);
        resourceCorrection.setField(ResourceCorrectionFields.RESOURCE_NUMBER, oldResource.getStringField(ResourceFields.NUMBER));
        resourceCorrection.setField(ResourceCorrectionFields.DELIVERY_NUMBER, oldResource.getStringField(ResourceFields.DELIVERY_NUMBER));

        resourceCorrection.setField(ResourceCorrectionFields.NEW_QUALITY_RATING, qualityRating);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_QUALITY_RATING, oldResource.getStringField(ResourceFields.QUALITY_RATING));

        return resourceCorrection;
    }

    private boolean isCorrectionNeeded(final Entity resource, final BigDecimal newQuantity,
                                       final Entity newStorageLocation, final BigDecimal newPrice,
                                       final Entity newBatch, final Entity newTypeOfLoadUnit,
                                       final Entity newPalletNumber, final Date newExpirationDate,
                                       final BigDecimal newConversion, final String qualityRating) {
        boolean quantityChanged = newQuantity.compareTo(resource.getDecimalField(ResourceFields.QUANTITY)) != 0;
        boolean priceChanged = isPriceChanged(resource.getDecimalField(ResourceFields.PRICE), newPrice);
        boolean batchChanged = isEntityChanged(resource.getBelongsToField(ResourceFields.BATCH), newBatch);
        boolean typeOfLoadUnitChanged = isEntityChanged(resource.getBelongsToField(ResourceFields.TYPE_OF_LOAD_UNIT), newTypeOfLoadUnit);
        boolean palletNumberChanged = isEntityChanged(resource.getBelongsToField(ResourceFields.PALLET_NUMBER), newPalletNumber);
        boolean expirationDateChanged = isExpirationDateChanged(resource.getDateField(ResourceFields.EXPIRATION_DATE), newExpirationDate);
        boolean conversionChanged = newConversion.compareTo(resource.getDecimalField(ResourceFields.CONVERSION)) != 0;
        boolean storageLocationChanged = isEntityChanged(resource, newStorageLocation);
        boolean qualityRatingChanged = isStringChanged(resource.getStringField(ResourceFields.QUALITY_RATING), qualityRating);

        return quantityChanged || storageLocationChanged || priceChanged || batchChanged || typeOfLoadUnitChanged || palletNumberChanged || expirationDateChanged || conversionChanged || qualityRatingChanged;
    }

    private boolean isPriceChanged(final BigDecimal oldPrice, final BigDecimal newPrice) {
        if (Objects.isNull(oldPrice) && Objects.isNull(newPrice)) {
            return false;
        }

        if (Objects.isNull(oldPrice) || Objects.isNull(newPrice)) {
            return true;
        }

        return oldPrice.compareTo(newPrice) != 0;
    }

    private boolean isStringChanged(final String oldString, final String newString) {
        if (Strings.isNullOrEmpty(oldString) && Strings.isNullOrEmpty(newString)) {
            return false;
        }

        if (Strings.isNullOrEmpty(oldString) || Strings.isNullOrEmpty(newString)) {
            return true;
        }

        return oldString.compareTo(newString) != 0;
    }

    private boolean isExpirationDateChanged(final Date oldExpirationDate, final Date newExpirationDate) {
        if (Objects.isNull(oldExpirationDate) && Objects.isNull(newExpirationDate)) {
            return false;
        }

        if (Objects.isNull(oldExpirationDate) || Objects.isNull(newExpirationDate)) {
            return true;
        }

        return oldExpirationDate.compareTo(newExpirationDate) != 0;
    }

    private boolean isEntityChanged(final Entity oldEntity, final Entity newEntity) {
        if (Objects.isNull(oldEntity) && Objects.isNull(newEntity)) {
            return false;
        }

        if (Objects.isNull(oldEntity) || Objects.isNull(newEntity)) {
            return true;
        }

        return oldEntity.getId().compareTo(newEntity.getId()) != 0;
    }

    private BigDecimal calculateQuantityInAdditionalUnit(final Entity resource, final String unit) {
        BigDecimal conversion = resource.getDecimalField(ResourceFields.CONVERSION);
        BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);

        boolean isInteger = dictionaryService.checkIfUnitIsInteger(unit);

        BigDecimal value = quantity.multiply(conversion, numberService.getMathContext());

        if (isInteger) {
            return numberService.setScaleWithDefaultMathContext(value, 0);
        } else {
            return numberService.setScaleWithDefaultMathContext(value);
        }
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition getResourceCorrectionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION);
    }

}
