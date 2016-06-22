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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiPositionFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class DeliveredProductAddMultiHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    public void beforeRender(final ViewDefinitionState view) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference("deliveredProductMultiPositions");
        List<FormComponent> formComponenets = deliveredProductMultiPositions.getFormComponents();
        for (FormComponent formComponent : formComponenets) {
            FieldComponent conversion = formComponent.findFieldComponentByName("conversion");
            FieldComponent unitComponent = formComponent.findFieldComponentByName("unit");
            FieldComponent additionalUnitComponent = formComponent.findFieldComponentByName("additionalUnit");
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
    }

    private void boldRequired(final FormComponent formComponent) {
        Arrays.asList(DeliveredProductMultiPositionFields.PRODUCT, DeliveredProductMultiPositionFields.EXPIRATION_DATE,
                DeliveredProductMultiPositionFields.QUANTITY, DeliveredProductMultiPositionFields.CONVERSION).stream()
                .forEach(f -> {
                    formComponent.findFieldComponentByName(f).setRequired(true);
                });
    }

}
