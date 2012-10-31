/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.FIELDS;
import static com.qcadoo.view.api.ComponentState.MessageType.INFO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostNormsForOperationService {

    private static final String OPERATION_FIELD = "operation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    /* ****** VIEW EVENT LISTENERS ******* */

    public void copyCostValuesFromOperation(final ViewDefinitionState view, final ComponentState operationLookupState,
            final String[] args) {
        ComponentState operationLookup = view.getComponentByReference(OPERATION_FIELD);
        if (operationLookup.getFieldValue() == null) {
            if (!OPERATION_FIELD.equals(operationLookupState.getName())) {
                view.getComponentByReference("form").addMessage("costNormsForOperation.messages.info.missingOperationReference",
                        INFO);
            }
            return;
        }
        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get((Long) operationLookup.getFieldValue());
        applyCostNormsFromGivenSource(view, operation);
    }

    public void copyCostValuesFromTechnology(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity technologyInstanceOperationComponent = ((FormComponent) view.getComponentByReference("form")).getEntity();
        // Be sure that entity isn't in detached state
        technologyInstanceOperationComponent = technologyInstanceOperationComponent.getDataDefinition().get(
                technologyInstanceOperationComponent.getId());
        applyCostNormsFromGivenSource(view,
                technologyInstanceOperationComponent.getBelongsToField("technologyOperationComponent"));

    }

    public void inheritOperationNormValues(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        copyCostValuesFromOperation(view, componentState, args);
    }

    public void fillCurrencyFields(final ViewDefinitionState view) {
        String currencyStringCode = currencyService.getCurrencyAlphabeticCode();
        FieldComponent component = null;
        for (String componentReference : Sets.newHashSet("pieceworkCostCURRENCY", "laborHourlyCostCURRENCY",
                "machineHourlyCostCURRENCY")) {
            component = (FieldComponent) view.getComponentByReference(componentReference);
            if (component == null) {
                continue;
            }
            component.setFieldValue(currencyStringCode);
            component.requestComponentUpdateState();
        }
    }

    private void applyCostNormsFromGivenSource(final ViewDefinitionState view, final Entity source) {
        checkArgument(source != null, "source entity is null");
        FieldComponent component = null;

        for (String fieldName : FIELDS) {
            component = (FieldComponent) view.getComponentByReference(fieldName);
            component.setFieldValue(source.getField(fieldName));
        }
    }

    /* ******* MODEL HOOKS ******* */

    public void copyCostNormsToTechnologyInstanceOperationComponent(final DataDefinition dd,
            final Entity technologyInstanceOperationComponent) {
        copyCostValuesFromGivenOperation(technologyInstanceOperationComponent,
                technologyInstanceOperationComponent.getBelongsToField("technologyOperationComponent"));
    }

    public void copyCostNormsToTechnologyOperationComponent(final DataDefinition dd, final Entity technologyOperationComponent) {
        if ("referenceTechnology".equals(technologyOperationComponent.getField("entityType"))) {
            return;
        }
        if (technologyOperationComponent.getBelongsToField(OPERATION_FIELD) == null) {
            return;
        }
        copyCostValuesFromGivenOperation(technologyOperationComponent,
                technologyOperationComponent.getBelongsToField(OPERATION_FIELD));
    }

    /* ******* CUSTOM HELPER(S) ******* */

    private void copyCostValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(target != null, "given target is null");
        checkArgument(source != null, "given source is null");

        if (!shouldPropagateValuesFromLowerInstance(target)) {
            return;
        }

        for (String fieldName : FIELDS) {
            if (source.getField(fieldName) == null) {
                continue;
            }
            target.setField(fieldName, source.getField(fieldName));
        }
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity technologyOperationComponent) {
        for (String fieldName : FIELDS) {
            if (technologyOperationComponent.getField(fieldName) != null) {
                return false;
            }
        }
        return true;
    }

}
