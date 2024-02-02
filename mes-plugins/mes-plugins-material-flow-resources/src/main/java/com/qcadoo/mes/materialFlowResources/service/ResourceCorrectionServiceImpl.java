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
        String newTypeOfPallet = resource.getStringField(ResourceFields.TYPE_OF_PALLET);
        Date newExpirationDate = resource.getDateField(ResourceFields.EXPIRATION_DATE);
        Entity newPalletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
        String qualityRating = resource.getStringField(ResourceFields.QUALITY_RATING);

        if (isCorrectionNeeded(oldResource, newQuantity, newStorageLocation, newPrice, newBatch, newTypeOfPallet, newPalletNumber, newExpirationDate, newConversion, qualityRating) || fromAttribute) {
            Entity oldStorageLocation = getStorageLocation(oldResource);
            Entity oldPalletNumber = getPalletNumber(oldResource);

            if (Objects.nonNull(newStorageLocation) && (isStorageLocationChanged(oldStorageLocation, newStorageLocation) || isPalletNumberChanged(oldPalletNumber, newPalletNumber))) {
                if (palletValidatorService.checkMaximumNumberOfPallets(newStorageLocation, resource)) {
                    resource.addGlobalError("materialFlow.error.correction.invalidStorageLocation");
                    resource.setNotValid();

                    return Optional.empty();
                }
            }

            Entity resourceCorrection = createResourceCorrection(oldResource, newQuantity, newPrice, newConversion, newStorageLocation, newBatch, newTypeOfPallet, newExpirationDate, newPalletNumber, qualityRating);

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

    private Entity createResourceCorrection(final Entity oldResource, final BigDecimal newQuantity, final BigDecimal newPrice, final BigDecimal newConversion, final Entity newStorageLocation, final Entity newBatch, final String newTypeOfPallet, final Date newExpirationDate, final Entity newPalletNumber, final String qualityRating) {
        Entity resourceCorrection = getResourceCorrectionDD().create();

        resourceCorrection.setField(ResourceCorrectionFields.OLD_BATCH, getBatch(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_BATCH, newBatch);
        resourceCorrection.setField(ResourceCorrectionFields.LOCATION, getLocation(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.OLD_QUANTITY, getQuantity(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_QUANTITY, newQuantity);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_PRICE, getPrice(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_PRICE, newPrice);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_STORAGE_LOCATION, getStorageLocation(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_STORAGE_LOCATION, newStorageLocation);
        resourceCorrection.setField(ResourceCorrectionFields.PRODUCT, getProduct(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.TIME, getTime(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_TYPE_OF_PALLET, newTypeOfPallet);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_TYPE_OF_PALLET, getTypeOfPallet(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.OLD_EXPIRATION_DATE, getExpirationDate(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_EXPIRATION_DATE, newExpirationDate);
        resourceCorrection.setField(ResourceCorrectionFields.NEW_PALLET_NUMBER, newPalletNumber);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_PALLET_NUMBER, getPalletNumber(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.PRODUCTION_DATE, getProductionDate(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.OLD_CONVERSION, getConversion(oldResource));
        resourceCorrection.setField(ResourceCorrectionFields.NEW_CONVERSION, newConversion);
        resourceCorrection.setField(ResourceCorrectionFields.NUMBER, numberGeneratorService.generateNumber(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION));

        resourceCorrection.setField(ResourceCorrectionFields.RESOURCE, oldResource);
        resourceCorrection.setField(ResourceCorrectionFields.RESOURCE_NUMBER, oldResource.getStringField(ResourceFields.NUMBER));
        resourceCorrection.setField(ResourceCorrectionFields.DELIVERY_NUMBER, oldResource.getStringField(ResourceFields.DELIVERY_NUMBER));

        resourceCorrection.setField(ResourceCorrectionFields.NEW_QUALITY_RATING, qualityRating);
        resourceCorrection.setField(ResourceCorrectionFields.OLD_QUALITY_RATING, oldResource.getStringField(ResourceFields.QUALITY_RATING));

        return resourceCorrection;
    }

    private boolean isCorrectionNeeded(final Entity resource, final BigDecimal newQuantity, final Entity newStorageLocation, final BigDecimal newPrice, final Entity newBatch, final String newTypeOfPallet, final Entity newPalletNumber, final Date newExpirationDate, final BigDecimal newConversion, final String qualityRating) {
        boolean quantityChanged = newQuantity.compareTo(getQuantity(resource)) != 0;
        boolean priceChanged = isPriceChanged(getPrice(resource), newPrice);
        boolean batchChanged = isBatchChanged(getBatch(resource), newBatch);
        boolean typeOfPalletChanged = isStringChanged(getTypeOfPallet(resource), newTypeOfPallet);
        boolean palletNumberChanged = isPalletNumberChanged(getPalletNumber(resource), newPalletNumber);
        boolean expirationDateChanged = isExpirationDateChanged(getExpirationDate(resource), newExpirationDate);
        boolean conversionChanged = newConversion.compareTo(getConversion(resource)) != 0;
        boolean storageLocationChanged = isStorageLocationChanged(resource, newStorageLocation);
        boolean qualityRatingChanged = isStringChanged(resource.getStringField(ResourceFields.QUALITY_RATING), qualityRating);

        return quantityChanged || storageLocationChanged || priceChanged || batchChanged || typeOfPalletChanged || palletNumberChanged || expirationDateChanged || conversionChanged || qualityRatingChanged;
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

    private boolean isBatchChanged(final Entity oldBatchNumber, final Entity newBatchNumber) {
        if (Objects.isNull(oldBatchNumber) && Objects.isNull(newBatchNumber)) {
            return false;
        }

        if (Objects.isNull(oldBatchNumber) || Objects.isNull(newBatchNumber)) {
            return true;
        }

        return oldBatchNumber.getId().compareTo(newBatchNumber.getId()) != 0;
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

    private boolean isStorageLocationChanged(final Entity oldStorageLocation, final Entity newStorageLocation) {
        if (Objects.isNull(oldStorageLocation) && Objects.isNull(newStorageLocation)) {
            return false;
        }

        if (Objects.isNull(oldStorageLocation) || Objects.isNull(newStorageLocation)) {
            return true;
        }

        return oldStorageLocation.getId().compareTo(newStorageLocation.getId()) != 0;
    }

    private boolean isPalletNumberChanged(final Entity oldPalletNumber, final Entity newPalletNumber) {
        if (Objects.isNull(oldPalletNumber) && Objects.isNull(newPalletNumber)) {
            return false;
        }

        if (Objects.isNull(oldPalletNumber) || Objects.isNull(newPalletNumber)) {
            return true;
        }

        return oldPalletNumber.getId().compareTo(newPalletNumber.getId()) != 0;
    }

    private Entity getProduct(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.PRODUCT);
    }

    private BigDecimal getQuantity(final Entity resource) {
        return resource.getDecimalField(ResourceFields.QUANTITY);
    }

    private BigDecimal getPrice(final Entity resource) {
        return resource.getDecimalField(ResourceFields.PRICE);
    }

    private BigDecimal getConversion(final Entity resource) {
        return resource.getDecimalField(ResourceFields.CONVERSION);
    }

    private Entity getLocation(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.LOCATION);
    }

    private Date getTime(final Entity resource) {
        return resource.getDateField(ResourceFields.TIME);
    }

    private Entity getBatch(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.BATCH);
    }

    private String getTypeOfPallet(final Entity resource) {
        return resource.getStringField(ResourceFields.TYPE_OF_PALLET);
    }

    private Entity getPalletNumber(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
    }

    private Date getExpirationDate(final Entity resource) {
        return resource.getDateField(ResourceFields.EXPIRATION_DATE);
    }

    private Date getProductionDate(final Entity resource) {
        return resource.getDateField(ResourceFields.PRODUCTION_DATE);
    }

    private Entity getStorageLocation(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
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
