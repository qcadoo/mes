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
package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.mes.advancedGenealogy.criteriaModifier.BatchCriteriaModifier;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiPositionFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class DeliveredProductAddMultiHooks {

    private static final String L_LOCATION = "location";

    @Autowired
    private BatchCriteriaModifier batchCriteriaModifier;

    public void beforeRender(final ViewDefinitionState view) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);

        List<FormComponent> formComponents = deliveredProductMultiPositions.getFormComponents();

        BigInteger ordinal = BigInteger.ONE;

        for (FormComponent formComponent : formComponents) {
            FieldComponent conversionField = formComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.CONVERSION);
            LookupComponent productLookup = (LookupComponent) formComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.PRODUCT);
            FieldComponent unitField = formComponent.findFieldComponentByName(DeliveredProductMultiPositionFields.UNIT);
            FieldComponent additionalUnitField = formComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT);
            FieldComponent ordinalField = formComponent.findFieldComponentByName(DeliveredProductMultiPositionFields.ORDINAL);
            LookupComponent batchLookup = (LookupComponent) formComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.BATCH);

            ordinalField.setFieldValue(ordinal);
            ordinalField.requestComponentUpdateState();

            ordinal = ordinal.add(BigInteger.ONE);

            Entity product = productLookup.getEntity();

            filterBatch(batchLookup, product);

            String unit = (String) unitField.getFieldValue();
            String additionalUnit = (String) additionalUnitField.getFieldValue();

            if (unit.equals(additionalUnit)) {
                conversionField.setEnabled(false);
                conversionField.requestComponentUpdateState();
            }

            boldRequired(formComponent);
        }

        FieldComponent palletNumber = (FieldComponent) view.getComponentByReference(DeliveredProductMultiFields.PALLET_NUMBER);

        palletNumber.setRequired(true);

        FieldComponent storageLocation = (FieldComponent) view
                .getComponentByReference(DeliveredProductMultiFields.STORAGE_LOCATION);
        storageLocation.setRequired(true);

        setStorageLocationFilter(view);
    }

    public void boldRequired(final FormComponent formComponent) {
        Arrays.asList(DeliveredProductMultiPositionFields.PRODUCT, DeliveredProductMultiPositionFields.QUANTITY,
                DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY, DeliveredProductMultiPositionFields.CONVERSION).stream()
                .forEach(fieldName -> {
                    FieldComponent fieldComponent = formComponent.findFieldComponentByName(fieldName);

                    fieldComponent.setRequired(true);
                    fieldComponent.requestComponentUpdateState();
                });
    }

    private void setStorageLocationFilter(final ViewDefinitionState view) {
        FormComponent deliveredProductMultiForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity deliveredProductMultiEntity = deliveredProductMultiForm.getPersistedEntityWithIncludedFormValues();

        Entity delivery = deliveredProductMultiEntity.getBelongsToField(DeliveredProductMultiFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.nonNull(location)) {
            LookupComponent storageLocationLookup = (LookupComponent) view
                    .getComponentByReference(DeliveredProductMultiFields.STORAGE_LOCATION);

            FilterValueHolder filterValueHolder = storageLocationLookup.getFilterValue();
            filterValueHolder.put(L_LOCATION, location.getId());

            storageLocationLookup.setFilterValue(filterValueHolder);
            storageLocationLookup.requestComponentUpdateState();
        }
    }

    public void filterBatch(final LookupComponent batchLookup, final Entity product) {
        batchCriteriaModifier.putProductFilterValue(batchLookup, product);
    }

}
