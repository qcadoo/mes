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
package com.qcadoo.mes.basic.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class UnitService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void fillProductUnit(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        fillProductUnitBeforeRender(viewDefinitionState);
    }

    public void fillProductUnitBeforeRender(final ViewDefinitionState state) {
        FieldComponent productState = (FieldComponent) state.getComponentByReference("product");
        FieldComponent unitState = (FieldComponent) state.getComponentByReference("unit");
        unitState.requestComponentUpdateState();
        if (productState.getFieldValue() == null) {
            unitState.setFieldValue("");
        } else {
            Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                    (Long) productState.getFieldValue());
            unitState.setFieldValue(product.getStringField("unit"));
        }
    }

    public void fillProductUnitBeforeRenderIfEmpty(final ViewDefinitionState state, final String unitField) {
        FieldComponent productState = (FieldComponent) state.getComponentByReference("product");
        FieldComponent unitState = (FieldComponent) state.getComponentByReference(unitField);
        unitState.requestComponentUpdateState();
        if (StringUtils.isEmpty((String) unitState.getFieldValue())) {
            if (productState.getFieldValue() == null) {
                unitState.setFieldValue("");
            } else {
                Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                        (Long) productState.getFieldValue());
                unitState.setFieldValue(product.getStringField(ProductFields.UNIT));
            }
        }
    }

    public String getDefaultUnitFromSystemParameters() {
        return parameterService.getParameter().getStringField("unit");
    }
}
