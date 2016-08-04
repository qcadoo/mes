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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
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

@Service
public class DeliveredProductAddMultiHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    public void beforeRender(final ViewDefinitionState view) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference("deliveredProductMultiPositions");
        List<FormComponent> formComponents = deliveredProductMultiPositions.getFormComponents();
        BigInteger ordinal = BigInteger.ONE;
        for (FormComponent formComponent : formComponents) {
            FieldComponent conversion = formComponent.findFieldComponentByName("conversion");
            LookupComponent productComponent = (LookupComponent) formComponent.findFieldComponentByName("product");
            FieldComponent unitComponent = formComponent.findFieldComponentByName("unit");
            FieldComponent additionalUnitComponent = formComponent.findFieldComponentByName("additionalUnit");
            LookupComponent additionalCodeComponent = (LookupComponent) formComponent.findFieldComponentByName("additionalCode");
            FieldComponent ordinalComponent = formComponent.findFieldComponentByName("ordinal");
            ordinalComponent.setFieldValue(ordinal);
            ordinalComponent.requestComponentUpdateState();
            ordinal = ordinal.add(BigInteger.ONE);
            filterAdditionalCode(productComponent.getEntity(), additionalCodeComponent);
            String unit = (String) unitComponent.getFieldValue();
            String additionalUnit = (String) additionalUnitComponent.getFieldValue();
            if (unit.equals(additionalUnit)) {
                conversion.setEnabled(false);
                conversion.requestComponentUpdateState();
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
                .forEach(f -> {
            FieldComponent component = formComponent.findFieldComponentByName(f);
            component.setRequired(true);
            component.requestComponentUpdateState();
        });
    }

    private void setStorageLocationFilter(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity deliveredProductMultiEntity = form.getPersistedEntityWithIncludedFormValues();
        Entity delivery = deliveredProductMultiEntity.getBelongsToField(DeliveredProductMultiFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);
        if (location != null) {
            LookupComponent storageLocationComponent = (LookupComponent) view.getComponentByReference("storageLocation");
            FilterValueHolder filterValueHolder = storageLocationComponent.getFilterValue();
            filterValueHolder.put("location", location.getId());
            storageLocationComponent.setFilterValue(filterValueHolder);
            storageLocationComponent.requestComponentUpdateState();
        }
    }

    public void filterAdditionalCode(Entity product, LookupComponent additionalCodeComponent) {
        if (product != null) {
            additionalCodeComponent.setEnabled(true);
            FilterValueHolder filterValueHolder = additionalCodeComponent.getFilterValue();
            filterValueHolder.put("product", product.getId());
            additionalCodeComponent.setFilterValue(filterValueHolder);
            additionalCodeComponent.requestComponentUpdateState();
        } else {
            additionalCodeComponent.setFieldValue(null);
            additionalCodeComponent.setEnabled(false);
            additionalCodeComponent.requestComponentUpdateState();
        }
    }
}
