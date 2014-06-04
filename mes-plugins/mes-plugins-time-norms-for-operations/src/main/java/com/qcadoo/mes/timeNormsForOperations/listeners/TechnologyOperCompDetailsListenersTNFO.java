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
package com.qcadoo.mes.timeNormsForOperations.listeners;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.*;
import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_OPERATION;
import static com.qcadoo.view.api.ComponentState.MessageType.INFO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class TechnologyOperCompDetailsListenersTNFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

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

        applyTimeNormsFromGivenSource(view, operation, FIELDS_OPERATION);
    }

    void applyTimeNormsFromGivenSource(final ViewDefinitionState view, final Entity source, final Iterable<String> fields) {
        checkArgument(source != null, "source entity is null");
        FieldComponent component = null;

        for (String fieldName : fields) {
            component = (FieldComponent) view.getComponentByReference(fieldName);
            component.setFieldValue(source.getField(fieldName));
        }

        if (source.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE) == null) {
            view.getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE).setFieldValue("01all");
        }

        if (source.getField(PRODUCTION_IN_ONE_CYCLE) == null) {
            view.getComponentByReference(PRODUCTION_IN_ONE_CYCLE).setFieldValue("1");
        }

        if (source.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY) == null) {
            view.getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY).setFieldValue("0");
        }

    }

    public void inheritOperationNormValues(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        copyTimeNormsFromOperation(viewDefinitionState, componentState, args);
    }

    public void changeNextOperationAfterProducedTypeNorm(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent nextOperationAfterProducedQuantity = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY);
        FieldComponent nextOperationAfterProducedQuantityUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT);

        Boolean visibilityValue = "02specified".equals(nextOperationAfterProducedType.getFieldValue());
        nextOperationAfterProducedQuantity.setVisible(visibilityValue);
        nextOperationAfterProducedQuantity.setEnabled(visibilityValue);
        nextOperationAfterProducedQuantityUNIT.setVisible(visibilityValue);

    }

    public void changeNextOperationAfterProducedTypeNormOperation(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent nextOperationAfterProducedQuantity = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY);
        FieldComponent nextOperationAfterProducedQuantityUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT);

        Boolean visibilityValue = "02specified".equals(nextOperationAfterProducedType.getFieldValue());
        nextOperationAfterProducedQuantity.setVisible(visibilityValue);
        nextOperationAfterProducedQuantity.setEnabled(visibilityValue);
        nextOperationAfterProducedQuantityUNIT.setVisible(visibilityValue);

    }

    public void onProductionInOneCycleCheckboxChange(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent areProductQuantitiesDivisible = (FieldComponent) viewDefinitionState
                .getComponentByReference(ARE_PRODUCT_QUANTITIES_DIVISIBLE);
        FieldComponent isTjDivisible = (FieldComponent) viewDefinitionState.getComponentByReference(IS_TJ_DIVISIBLE);
        if ("1".equals(areProductQuantitiesDivisible.getFieldValue())) {
            isTjDivisible.setEnabled(true);
        } else {
            isTjDivisible.setEnabled(false);
            isTjDivisible.setFieldValue("0");
        }

    }

}
