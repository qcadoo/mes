/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
import com.qcadoo.localization.api.TranslationService;
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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private CurrencyService currencyService;

    /* ****** VIEW EVENT LISTENERS ******* */

    public void copyCostValuesFromOperation(final ViewDefinitionState view, final ComponentState operationLookupState,
            final String[] args) {
        ComponentState operationLookup = view.getComponentByReference("operation");
        if (operationLookup.getFieldValue() == null) {
            if (!"operation".equals(operationLookupState.getName())) {
                view.getComponentByReference("form").addMessage(
                        translationService.translate("costNormsForOperation.messages.info.missingOperationReference",
                                view.getLocale()), INFO);
            }
            return;
        }
        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get((Long) operationLookup.getFieldValue());
        applyCostNormsFromGivenSource(view, operation);
    }

    public void copyCostValuesFromTechnology(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity orderOperationComponent = ((FormComponent) view.getComponentByReference("form")).getEntity();
        // Be sure that entity isn't in detached state
        orderOperationComponent = orderOperationComponent.getDataDefinition().get(orderOperationComponent.getId());
        applyCostNormsFromGivenSource(view, orderOperationComponent.getBelongsToField("technologyOperationComponent"));

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

        // FIXME MAKU - double notification after change operation lookup value
        // view.getComponentByReference("form").addMessage(translationService.translate("costNormsForOperation.messages.success.copyCostNormsSuccess",
        // view.getLocale()), SUCCESS);
    }

    /* ******* MODEL HOOKS ******* */

    public void copyCostNormsToOrderOperationComponent(final DataDefinition dd, final Entity orderOperationComponent) {
        copyCostValuesFromGivenOperation(orderOperationComponent,
                orderOperationComponent.getBelongsToField("technologyOperationComponent"));
    }

    public void copyCostNormsToTechnologyOperationComponent(final DataDefinition dd, final Entity technologyOperationComponent) {
        if ("referenceTechnology".equals(technologyOperationComponent.getField("entityType"))) {
            return;
        }
        copyCostValuesFromGivenOperation(technologyOperationComponent,
                technologyOperationComponent.getBelongsToField("operation"));
    }

    /* ******* CUSTOM HELPER(S) ******* */

    private void copyCostValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(target != null, "given target is null");
        checkArgument(source != null, "given source is null");

        for (String fieldName : FIELDS) {
            if (source.getField(fieldName) == null) {
                continue;
            }
            target.setField(fieldName, source.getField(fieldName));
        }
    }
}
