/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basicProductionCounting.hooks;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductionCountingQuantityAdvancedDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_USED_QUANTITY_GRID_LAYOUT = "usedQuantityGridLayout";

    private static final String L_PRODUCED_QUANTITY_GRID_LAYOUT = "producedQuantityGridLayout";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void disableFieldsDependsOfState(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductionCountingQuantityFields.PRODUCT);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
        FieldComponent typeOfMaterialField = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);

        Long productionCountingQuantityId = productionCountingQuantityForm.getEntityId();

        boolean isEnabled = (productionCountingQuantityId == null);

        updateFieldComponentState(productLookup, isEnabled);
        updateFieldComponentState(technologyOperationComponentLookup, isEnabled);
        updateFieldComponentState(typeOfMaterialField, isEnabled);
        updateFieldComponentState(roleField, isEnabled);
    }

    private void updateFieldComponentState(final FieldComponent filedComponent, final boolean isEnabled) {
        filedComponent.setEnabled(isEnabled);
        filedComponent.requestComponentUpdateState();
    }

    public void hideFieldsDependsOfState(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(L_FORM);
        ComponentState usedQuantityGridLayout = view.getComponentByReference(L_USED_QUANTITY_GRID_LAYOUT);
        ComponentState producedQuantityGridLayout = view.getComponentByReference(L_PRODUCED_QUANTITY_GRID_LAYOUT);
        FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);

        Long productionCountingQuantityId = productionCountingQuantityForm.getEntityId();

        boolean isEnabled = (productionCountingQuantityId != null);
        boolean isUsed = checkIfIsUsed((String) roleField.getFieldValue());
        boolean isProduced = checkIfIsProduced((String) roleField.getFieldValue());

        updateComponentState(usedQuantityGridLayout, isEnabled && isUsed);
        updateComponentState(producedQuantityGridLayout, isEnabled && isProduced);
    }

    private boolean checkIfIsUsed(final String role) {
        return (StringUtils.isNotEmpty(role) && ProductionCountingQuantityRole.USED.getStringValue().equals(role));
    }

    private boolean checkIfIsProduced(final String role) {
        return (StringUtils.isNotEmpty(role) && ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role));
    }

    private void updateComponentState(final ComponentState componentState, final boolean isVisible) {
        componentState.setVisible(isVisible);
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity productionCountingQuantity = productionCountingQuantityForm.getEntity();

        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);

        if (order != null) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (technology != null) {
                FilterValueHolder filterValueHolder = technologyOperationComponentLookup.getFilterValue();
                filterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                technologyOperationComponentLookup.setFilterValue(filterValueHolder);
            }
        }
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList("plannedQuantityUnit", "usedQuantityUnit", "producedQuantityUnit");

        basicProductionCountingService.fillUnitFields(view, ProductionCountingQuantityFields.PRODUCT, referenceNames);
    }

}
