/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionCounting.listeners;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class RecordOperationProductComponentListeners {

    private static final Set<String> UNIT_COMPONENT_REFERENCES = Sets.newHashSet("plannedQuantityUNIT", "usedQuantityUNIT");

    private static final String L_FORM = "form";

    private static final String L_PRODUCT = "product";

    private static final String L_NAME = "name";

    private static final String L_NUMBER = "number";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity componentEntity = form.getPersistedEntityWithIncludedFormValues();
        Entity productEntity = componentEntity.getBelongsToField(L_PRODUCT);

        fillUnits(view, productEntity);
        fillFieldFromProduct(view, productEntity);
    }

    private void fillUnits(final ViewDefinitionState view, final Entity productEntity) {
        String unit = productEntity.getStringField(ProductFields.UNIT);
        for (String componentReferenceName : UNIT_COMPONENT_REFERENCES) {
            FieldComponent unitComponent = (FieldComponent) view.getComponentByReference(componentReferenceName);
            if (unitComponent != null) {
                unitComponent.setFieldValue(unit);
                unitComponent.requestComponentUpdateState();
            }
        }
    }

    public void fillFieldFromProduct(final ViewDefinitionState view, final Entity productEntity) {
        view.getComponentByReference(L_NUMBER).setFieldValue(productEntity.getField(L_NUMBER));
        view.getComponentByReference(L_NAME).setFieldValue(productEntity.getField(L_NAME));
    }

}
