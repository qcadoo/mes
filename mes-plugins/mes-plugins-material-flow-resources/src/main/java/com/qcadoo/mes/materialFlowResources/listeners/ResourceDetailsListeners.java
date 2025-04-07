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
package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.hooks.ResourceDetailsHooks;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class ResourceDetailsListeners {

    private static final String L_PRODUCT_ID = "product_id";

    private static final String L_LOCATION_ID = "location_id";

    private static final String L_QUANTITY = "availableQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void createResourceCorrection(final ViewDefinitionState view, final ComponentState state,
                                         final String[] args) {
        FormComponent resourceForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity resource = resourceForm.getPersistedEntityWithIncludedFormValues();

        if (validateQuantity(view, resource) && validatePallet(view, resource)) {
            boolean corrected = resourceCorrectionService.createCorrectionForResource(resource, false).isPresent();

            if (!resource.isValid()) {
                copyErrors(resourceForm, resource);
            } else if (!corrected) {
                resourceForm.addMessage("materialFlow.info.correction.resourceNotChanged", MessageType.INFO);
            } else {
                resourceForm.performEvent(view, "reset");

                resourceForm.addMessage("materialFlow.success.correction.correctionCreated", MessageType.SUCCESS);
            }
        }
    }

    private void copyErrors(final FormComponent resourceForm, final Entity resource) {
        resource.getGlobalErrors().forEach(resourceForm::addMessage);

        resource.getErrors().values().forEach(resourceForm::addMessage);
    }

    private boolean validateQuantity(final ViewDefinitionState view, final Entity resource) {
        boolean isValid = true;

        FieldComponent quantityInput = (FieldComponent) view.getComponentByReference(ResourceFields.QUANTITY);
        FieldComponent priceInput = (FieldComponent) view.getComponentByReference(ResourceFields.PRICE);
        FieldComponent conversionInput = (FieldComponent) view.getComponentByReference(ResourceFields.CONVERSION);

        String newQuantity = (String) quantityInput.getFieldValue();
        String newPrice = (String) priceInput.getFieldValue();
        String newConversion = (String) conversionInput.getFieldValue();

        Either<Exception, Optional<BigDecimal>> quantity = BigDecimalUtils.tryParseAndIgnoreSeparator(newQuantity,
                view.getLocale());
        Either<Exception, Optional<BigDecimal>> price = BigDecimalUtils.tryParseAndIgnoreSeparator(newPrice, view.getLocale());
        Either<Exception, Optional<BigDecimal>> conversion = BigDecimalUtils.tryParseAndIgnoreSeparator(newConversion,
                view.getLocale());

        if (quantity.isRight() && quantity.getRight().isPresent()) {
            Entity resourceFromDb = resource.getDataDefinition().get(resource.getId());

            BigDecimal correctQuantity = quantity.getRight().get();

            BigDecimal beforeQuantity = resourceFromDb.getDecimalField(ResourceFields.QUANTITY);
            BigDecimal difference = correctQuantity.subtract(beforeQuantity, numberService.getMathContext());

            Entity resourceStockDto = getResourceStockDto(resourceFromDb);

            BigDecimal afterCorrectQuantity = resourceStockDto.getDecimalField(L_QUANTITY).add(difference,
                    numberService.getMathContext());

            if (afterCorrectQuantity.compareTo(BigDecimal.ZERO) < 0) {
                quantityInput.addMessage("materialFlow.error.correction.quantityLesserThanAvailable", MessageType.FAILURE);

                isValid = false;
            } else if (price.isRight()) {
                if (conversion.isRight() && conversion.getRight().isPresent()) {
                    BigDecimal resourceReservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY);

                    if (correctQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        if (correctQuantity.compareTo(resourceReservedQuantity) < 0) {
                            quantityInput.addMessage("materialFlow.error.correction.quantityLesserThanReserved",
                                    MessageType.FAILURE);

                            isValid = false;
                        }
                    } else {
                        quantityInput.addMessage("materialFlow.error.correction.invalidQuantity", MessageType.FAILURE);

                        isValid = false;
                    }
                } else {
                    conversionInput.addMessage("materialFlow.error.correction.invalidConversion", MessageType.FAILURE);

                    isValid = false;
                }
            } else {
                priceInput.addMessage("materialFlow.error.correction.invalidPrice", MessageType.FAILURE);

                isValid = false;
            }
        } else {
            quantityInput.addMessage("materialFlow.error.correction.invalidQuantity", MessageType.FAILURE);

            isValid = false;
        }

        return isValid;
    }

    private boolean validatePallet(final ViewDefinitionState view, final Entity resource) {
        boolean isValid = true;

        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(ResourceFields.STORAGE_LOCATION);
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(ResourceFields.PALLET_NUMBER);

        Entity storageLocation = storageLocationLookup.getEntity();
        Entity palletNumber = palletNumberLookup.getEntity();

        if (Objects.isNull(storageLocation) && Objects.nonNull(palletNumber)) {
            storageLocationLookup.addMessage("qcadooView.validate.field.error.missing", MessageType.FAILURE);

            isValid = false;
        } else {
            if (Objects.nonNull(storageLocation)) {
                boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

                if (placeStorageLocation) {
                    if (Objects.isNull(palletNumber)) {
                        palletNumberLookup.addMessage("qcadooView.validate.field.error.missing", MessageType.FAILURE);

                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    public void fillTypeOfLoadUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(ResourceFields.LOCATION);
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(ResourceFields.PALLET_NUMBER);
        LookupComponent typeOfLoadUnitLookup = (LookupComponent) view.getComponentByReference(ResourceFields.TYPE_OF_LOAD_UNIT);

        Entity location = locationLookup.getEntity();
        Entity palletNumber = palletNumberLookup.getEntity();
        Long typeOfLoadUnit = null;

        if (Objects.nonNull(palletNumber)) {
            typeOfLoadUnit = materialFlowResourcesService.getTypeOfLoadUnitByPalletNumber(location.getId(), palletNumber.getStringField(PalletNumberFields.NUMBER));
        }

        typeOfLoadUnitLookup.setFieldValue(typeOfLoadUnit);
        typeOfLoadUnitLookup.requestComponentUpdateState();
    }

    private Entity getResourceStockDto(final Entity resourceFromDb) {
        return getResourceStockDto().find()
                .add(SearchRestrictions.eq(L_PRODUCT_ID, resourceFromDb.getBelongsToField(ResourceFields.PRODUCT).getId().intValue()))
                .add(SearchRestrictions.eq(L_LOCATION_ID, resourceFromDb.getBelongsToField(ResourceFields.LOCATION).getId().intValue()))
                .setMaxResults(1).uniqueResult();
    }

    private DataDefinition getResourceStockDto() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO);
    }

}
