/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.6
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
package com.qcadoo.mes.productionTimeNorms;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.productionTimeNorms.TimeNormsConstants.FIELDS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class NormService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionInOneCycle");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");
        Object value = countRealized.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        productionInOneCycle.setEnabled(true);

        countRealized.setEnabled(true);
        if (!"02specified".equals(value)) {
            countRealized.setFieldValue("01all");
        }
        timeNextOperation.setEnabled(true);
    }

    public void updateFieldsStateWhenDefaultValueCheckboxChanged(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        /* uruchamia sie hook before render */
    }

    public void changeCountRealizedNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");

        if (countRealized.getFieldValue().equals("02specified")) {
            countMachine.setVisible(true);
            countMachine.setEnabled(true);
        } else {
            countMachine.setVisible(false);
        }
    }

    public void copyNormFromOperation(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionInOneCycle");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");

        Long operationId = (Long) componentState.getFieldValue();

        Entity operation = operationId != null ? dataDefinitionService.get("technologies", "operation").get(operationId) : null;

        if (operation != null) {
            tpzNorm.setFieldValue(operation.getField("tpz"));
            tjNorm.setFieldValue(operation.getField("tj"));
            productionInOneCycle.setFieldValue(operation.getField("productionInOneCycle"));
            countRealized.setFieldValue(operation.getField("countRealizedOperation") != null ? operation
                    .getField("countRealizedOperation") : "01all");
            countMachine.setFieldValue(operation.getField("countMachineOperation"));
            timeNextOperation.setFieldValue(operation.getField("timeNextOperation"));
        } else {
            tpzNorm.setFieldValue(null);
            tjNorm.setFieldValue(null);
            productionInOneCycle.setFieldValue("1");
            countRealized.setFieldValue("01all");
            countMachine.setFieldValue(null);
            timeNextOperation.setFieldValue(null);
        }
    }

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");

        if ("02specified".equals(countRealizedOperation.getFieldValue())) {
            countMachineOperation.setVisible(true);
            countMachineOperation.setEnabled(true);
        } else {
            countMachineOperation.setVisible(false);
        }
    }

    public void inheritOperationNormValues(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        String formName = form.getName();
        Entity target, source;

        if ("form".equals(formName)) { // technology
            target = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(form.getEntityId());
            source = target.getBelongsToField("operation");
            copyCostValuesFromGivenOperation(target, source);
        } else if ("orderOperationComponent".equals(formName)) { // technology instance (technology inside order)
            target = dataDefinitionService.get(ProductionSchedulingConstants.PLUGIN_IDENTIFIER,
                    ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT).get(form.getEntityId());
            source = target.getBelongsToField("technologyOperationComponent");
            if (copyCostValuesFromGivenOperation(target, source) != null) {
                return;
            }
            copyCostValuesFromGivenOperation(target, source.getBelongsToField("operation"));
            source = copyCostValuesFromGivenOperation(source, source.getBelongsToField("operation")); // Fill missing technology
                                                                                                      // costs
        } else {
            return;
        }
        fillCostFormFields(viewDefinitionState, source); // propagate model changes into the view
    }

    private Entity copyCostValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(source != null, "given source is null");
        boolean result = false;
        for (String fieldName : FIELDS) {
            if (target.getField(fieldName) == null && source.getField(fieldName) != null) {
                target.setField(fieldName, source.getField(fieldName));
                result = true;
            }
        }
        if (!result) {
            return null;
        }
        return target.getDataDefinition().save(target);
    }

    private void fillCostFormFields(final ViewDefinitionState viewDefinitionState, final Entity source) {
        checkArgument(source != null, "source is null!");
        for (String componentReference : FIELDS) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (component.getFieldValue() != null && component.getFieldValue().toString().isEmpty()
                    && source.getField(componentReference) != null) {
                component.setFieldValue(source.getField(componentReference).toString());
            }
        }
    }
}
