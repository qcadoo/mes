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

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ResourceDetailsListeners {

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    private static final String L_FORM = "form";

    public void createResourceCorrection(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent resourceForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent quantityInput = (FieldComponent) view.getComponentByReference(ResourceFields.QUANTITY);
        String newQuantity = (String) quantityInput.getFieldValue();

        LookupComponent storageLocation = (LookupComponent) view.getComponentByReference(ResourceFields.STORAGE_LOCATION);
        Entity newStorageLocation = storageLocation.getEntity();
        Either<Exception, Optional<BigDecimal>> quantity = BigDecimalUtils.tryParseAndIgnoreSeparator(newQuantity, view.getLocale());

        if (quantity.isRight() && quantity.getRight().isPresent()) {
            Entity resource = resourceForm.getPersistedEntityWithIncludedFormValues();
            BigDecimal correctQuantity = quantity.getRight().get();
            if (correctQuantity.compareTo(BigDecimal.ZERO) > 0) {
                boolean corrected = resourceCorrectionService.createCorrectionForResource(resource, correctQuantity, newStorageLocation);
                if (!corrected) {
                    resourceForm.addMessage("materialFlow.info.correction.resourceNotChanged", MessageType.INFO);
                    
                } else {                    
                    resourceForm.performEvent(view, "reset");
                    quantityInput.requestComponentUpdateState();
                    resourceForm.addMessage("materialFlow.success.correction.correctionCreated", MessageType.SUCCESS);
                }
            } else {
                quantityInput.addMessage("materialFlow.error.correction.invalidQuantity", MessageType.FAILURE);
            }
        } else {
            quantityInput.addMessage("materialFlow.error.correction.invalidQuantity", MessageType.FAILURE);
        }

    }
}
