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
package com.qcadoo.mes.warehouseMinimalState.hooks;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WarehouseMinimumStateDetailsHooks {

    private static final Set<String> UNIT_COMPONENT_REFERENCES = Sets.newHashSet("minimumStateUNIT", "optimalOrderQuantityNIT");

    private static final String L_FORM = "form";

    private static final String L_PRODUCT = "product";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity componentEntity = form.getPersistedEntityWithIncludedFormValues();
        Entity productEntity = componentEntity.getBelongsToField(L_PRODUCT);

        fillUnits(view, productEntity);
    }

    private void fillUnits(final ViewDefinitionState view, final Entity productEntity) {
        if (productEntity == null) {
            return;
        }
        String unit = productEntity.getStringField(ProductFields.UNIT);
        for (String componentReferenceName : UNIT_COMPONENT_REFERENCES) {
            FieldComponent unitComponent = (FieldComponent) view.getComponentByReference(componentReferenceName);
            if (unitComponent != null && StringUtils.isEmpty((String) unitComponent.getFieldValue())) {
                unitComponent.setFieldValue(unit);
                unitComponent.requestComponentUpdateState();
            }

        }
    }
}
