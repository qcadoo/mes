/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.NEXT_OPERATION_AFTER_PRODUCED_TYPE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.TIME_NEXT_OPERATION;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyInstanceOperCompDetailsHooksTNFO {

    public void disableComponents(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpz = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tj = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference(PRODUCTION_IN_ONE_CYCLE);
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent nextOperationAfterProducedQuantity = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY);
        FieldComponent nextOperationAfterProducedQuantityUnit = (FieldComponent) viewDefinitionState
                .getComponentByReference("nextOperationAfterProducedQuantityUNIT");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference(TIME_NEXT_OPERATION);
        FieldComponent areProductQuantitiesDivisible = (FieldComponent) viewDefinitionState
                .getComponentByReference("areProductQuantitiesDivisible");
        FieldComponent isTjDivisible = (FieldComponent) viewDefinitionState.getComponentByReference("isTjDivisible");

        tpz.setEnabled(true);
        tpz.setRequired(true);
        tj.setEnabled(true);
        tj.setRequired(true);
        productionInOneCycle.setEnabled(true);
        productionInOneCycle.setRequired(true);
        nextOperationAfterProducedType.setEnabled(true);
        nextOperationAfterProducedType.setRequired(true);

        if ("02specified".equals(nextOperationAfterProducedType.getFieldValue())) {
            nextOperationAfterProducedQuantity.setVisible(true);
            nextOperationAfterProducedQuantity.setEnabled(true);
            nextOperationAfterProducedQuantity.setRequired(true);
            nextOperationAfterProducedQuantityUnit.setVisible(true);
            if (nextOperationAfterProducedQuantity.getFieldValue() == null
                    || !StringUtils.hasText(String.valueOf(nextOperationAfterProducedQuantity.getFieldValue()))) {
                nextOperationAfterProducedQuantity.setFieldValue("1");
            }
        } else {
            nextOperationAfterProducedQuantity.setVisible(false);
            nextOperationAfterProducedQuantity.setRequired(false);
            nextOperationAfterProducedQuantityUnit.setVisible(false);
        }

        timeNextOperation.setEnabled(true);
        timeNextOperation.setRequired(true);

        if ("1".equals(areProductQuantitiesDivisible.getFieldValue())) {
            isTjDivisible.setEnabled(true);
        }

    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FieldComponent component = null;
        Entity formEntity = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // we can pass units only to technology level operations
        if (formEntity.getId() == null
                || !"technologyInstanceOperationComponent".equals(formEntity.getDataDefinition().getName())) {
            return;
        }

        // be sure that entity isn't in detached state before you wander through the relationship
        formEntity = formEntity.getDataDefinition().get(formEntity.getId());
        // you can use someEntity.getSTH().getSTH() only when you are 100% sure that all the passers-relations
        // will not return null (i.e. all relations using below are mandatory on the model definition level)
        String unit = formEntity.getBelongsToField("technology").getBelongsToField("product").getField("unit").toString();
        for (String referenceName : Sets.newHashSet("nextOperationAfterProducedQuantityUNIT", "productionInOneCycleUNIT")) {
            component = (FieldComponent) view.getComponentByReference(referenceName);
            if (component == null) {
                continue;
            }
            component.setFieldValue(unit);
            component.requestComponentUpdateState();
        }
    }
}
