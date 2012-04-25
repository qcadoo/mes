/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForProduct.constants.CostNormsForProductConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostNormsForProductService {

    private static final String L_VIEW_DEFINITION_STATE_IS_NULL = "viewDefinitionState is null";

    private static final String L_FORM = "form";

    private static final String L_NOMINAL_COST = "nominalCost";

    private static final String L_COST_FOR_NUMBER_UNIT = "costForNumberUnit";

    private static final String L_EMPTY = "";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    public void fillUnitFieldInProduct(final ViewDefinitionState viewDefinitionState) {
        fillUnitField(viewDefinitionState, L_COST_FOR_NUMBER_UNIT, true);
    }

    public void fillUnitField(final ViewDefinitionState viewDefinitionState, final String fieldName, final boolean inProduct) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        FieldComponent unitField = (FieldComponent) viewDefinitionState.getComponentByReference(fieldName);
        unitField.setFieldValue(L_EMPTY);
        unitField.setEnabled(false);
        if (form == null || form.getEntityId() == null) {
            return;
        }

        Long productId = null;

        if (inProduct) {
            productId = (Long) form.getFieldValue();
        } else {
            FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference("product");

            if (productField == null) {
                return;
            }

            productId = (Long) productField.getFieldValue();
        }

        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);

        if (product == null) {
            return;
        }

        String unit = product.getStringField(UNIT);

        fillField(unitField, unit);
    }

    public void fillCurrencyFieldsInProduct(final ViewDefinitionState viewDefinitionState) {
        fillCurrencyFields(viewDefinitionState, CostNormsForProductConstants.CURRENCY_FIELDS_PRODUCT);
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState, final Set<String> fieldNames) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        clearAndDisabledFields(viewDefinitionState, fieldNames);
        if (form == null || form.getEntityId() == null) {
            return;
        }
        String currency = currencyService.getCurrencyAlphabeticCode();
        if (currency == null) {
            return;
        }
        for (String fieldName : fieldNames) {
            FieldComponent currencyField = (FieldComponent) viewDefinitionState.getComponentByReference(fieldName);
            fillField(currencyField, currency);
        }
    }

    private void clearAndDisabledFields(final ViewDefinitionState view, final Set<String> fieldNames) {
        for (String fieldName : fieldNames) {
            FieldComponent currencyField = (FieldComponent) view.getComponentByReference(fieldName);
            currencyField.setFieldValue(L_EMPTY);
            currencyField.setEnabled(false);
        }
    }

    public void fillField(final FieldComponent fieldComponent, final String fieldValue) {
        checkArgument(fieldComponent != null, "fieldComponent is null");
        fieldComponent.setFieldValue(fieldValue);
        fieldComponent.requestComponentUpdateState();
    }

    public void enabledFieldForExternalID(final ViewDefinitionState view) {
        FieldComponent nominalCost = (FieldComponent) view.getComponentByReference(L_NOMINAL_COST);
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        Entity entity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                form.getEntityId());

        if (entity == null) {
            return;
        }
        String externalNumber = entity.getStringField("externalNumber");

        if (externalNumber != null) {
            nominalCost.setEnabled(true);
        }
    }

}
