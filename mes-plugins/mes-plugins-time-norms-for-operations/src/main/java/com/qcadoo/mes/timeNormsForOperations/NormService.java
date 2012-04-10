/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.timeNormsForOperations;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.timeNormsForOperations.TimeNormsConstants.FIELDS_OPERATION;
import static com.qcadoo.mes.timeNormsForOperations.TimeNormsConstants.FIELDS_TECHNOLOGY;
import static com.qcadoo.view.api.ComponentState.MessageType.INFO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class NormService {

    private static final String OPERATION = "operation";

    private static final String TIME_NEXT_OPERATION = "timeNextOperation";

    private static final String TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final String PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String COUNT_MACHINE_UNIT = "countMachineUNIT";

    private static final String COUNT_MACHINE = "countMachine";

    private static final String COUNT_REALIZED = "countRealized";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference(PRODUCTION_IN_ONE_CYCLE);
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference(TIME_NEXT_OPERATION);
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
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE);
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE_UNIT);

        Boolean visibilityValue = "02specified".equals(countRealized.getFieldValue());
        countMachine.setVisible(visibilityValue);
        countMachine.setEnabled(visibilityValue);
        countMachineUNIT.setVisible(visibilityValue);

    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FieldComponent component = null;
        Entity formEntity = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // we can pass units only to technology level operations
        if (formEntity.getId() == null || !TECHNOLOGY_OPERATION_COMPONENT.equals(formEntity.getDataDefinition().getName())) {
            return;
        }

        // be sure that entity isn't in detached state before you wander through the relationship
        formEntity = formEntity.getDataDefinition().get(formEntity.getId());
        // you can use someEntity.getSTH().getSTH() only when you are 100% sure that all the passers-relations
        // will not return null (i.e. all relations using below are mandatory on the model definition level)
        String unit = formEntity.getBelongsToField("technology").getBelongsToField("product").getField("unit").toString();
        for (String referenceName : Sets.newHashSet(COUNT_MACHINE_UNIT, "productionInOneCycleUNIT")) {
            component = (FieldComponent) view.getComponentByReference(referenceName);
            if (component == null) {
                continue;
            }
            component.setFieldValue(unit);
            component.requestComponentUpdateState();
        }
    }

    public void copyTimeNormsFromOperation(final ViewDefinitionState view, final ComponentState operationLookupState,
            final String[] args) {

        ComponentState operationLookup = view.getComponentByReference(OPERATION);
        if (operationLookup.getFieldValue() == null) {
            if (!OPERATION.equals(operationLookupState.getName())) {
                view.getComponentByReference("form").addMessage("productionTimeNorms.messages.info.missingOperationReference",
                        INFO);
            }
            return;
        }

        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get((Long) operationLookup.getFieldValue());

        applyCostNormsFromGivenSource(view, operation, FIELDS_OPERATION);
    }

    public void copyTimeNormsFromTechnology(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity orderOperationComponent = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // be sure that entity isn't in detached state
        orderOperationComponent = orderOperationComponent.getDataDefinition().get(orderOperationComponent.getId());

        applyCostNormsFromGivenSource(view, orderOperationComponent.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT),
                FIELDS_TECHNOLOGY);
    }

    private void applyCostNormsFromGivenSource(final ViewDefinitionState view, final Entity source, final Iterable<String> fields) {
        checkArgument(source != null, "source entity is null");
        FieldComponent component = null;

        for (String fieldName : fields) {
            component = (FieldComponent) view.getComponentByReference(fieldName);
            component.setFieldValue(source.getField(fieldName));
        }

        if (source.getField(COUNT_REALIZED) == null) {
            view.getComponentByReference(COUNT_REALIZED).setFieldValue("01all");
        }

        if (source.getField(PRODUCTION_IN_ONE_CYCLE) == null) {
            view.getComponentByReference(PRODUCTION_IN_ONE_CYCLE).setFieldValue("1");
        }

        if (source.getField(COUNT_MACHINE) == null) {
            view.getComponentByReference(COUNT_MACHINE).setFieldValue("0");
        }

    }

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE);
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE_UNIT);

        if ("02specified".equals(countRealizedOperation.getFieldValue())) {
            countMachineOperation.setVisible(true);
            countMachineOperation.setEnabled(true);
            countMachineUNIT.setVisible(true);
            countMachineUNIT.setEnabled(true);
        } else {
            countMachineOperation.setVisible(false);
            countMachineUNIT.setVisible(false);

        }
    }

    public void inheritOperationNormValues(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        copyTimeNormsFromOperation(viewDefinitionState, componentState, args);
    }

    public void copyTimeNormsToTechnologyOperationComponent(final DataDefinition dd, final Entity technologyOperationComponent) {
        if ("referenceTechnology".equals(technologyOperationComponent.getField("entityType"))) {
            return;
        }
        if (technologyOperationComponent.getBelongsToField(OPERATION) == null) {
            return;
        }
        copyTimeValuesFromGivenOperation(technologyOperationComponent, technologyOperationComponent.getBelongsToField(OPERATION));
    }

    private void copyTimeValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(target != null, "given target is null");
        checkArgument(source != null, "given source is null");

        if (!shouldPropagateValuesFromLowerInstance(target)) {
            return;
        }

        for (String fieldName : FIELDS_OPERATION) {
            if (source.getField(fieldName) == null) {
                continue;
            }
            target.setField(fieldName, source.getField(fieldName));
        }
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity technologyOperationComponent) {
        for (String fieldName : FIELDS_OPERATION) {
            if (technologyOperationComponent.getField(fieldName) != null) {
                return false;
            }
        }
        return true;
    }

}
