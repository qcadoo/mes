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
package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.NEXT_OPERATION_AFTER_PRODUCED_TYPE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.TIME_NEXT_OPERATION;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyOperationComponentDetailsHooks {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private NumberService numberService;

    public void checkOperationOutputQuantities(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }

        Entity operationComponent = form.getEntity();
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());

        BigDecimal timeNormsQuantity = operationComponent.getDecimalField("productionInOneCycle");

        Entity productOutComponent = null;

        try {
            productOutComponent = technologyService.getMainOutputProductComponent(operationComponent);
        } catch (IllegalStateException e) {
            return;
        }

        BigDecimal currentQuantity = productOutComponent.getDecimalField("quantity");

        if (timeNormsQuantity.compareTo(currentQuantity) != 0) {
            form.addMessage("technologies.technologyOperationComponent.validate.error.invalidQuantity", MessageType.INFO, false,
                    numberService.format(currentQuantity), productOutComponent.getBelongsToField("product")
                            .getStringField("unit"));
        }
    }

    public void updateNextOperationAfterProducedQuantityFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent nextOperationAfterProducedQuantity = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY);
        FieldComponent nextOperationAfterProducedQuantityUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT);

        if ("02specified".equals(nextOperationAfterProducedType.getFieldValue())) {
            nextOperationAfterProducedQuantity.setVisible(true);
            nextOperationAfterProducedQuantity.setEnabled(true);
            nextOperationAfterProducedQuantityUNIT.setVisible(true);
            nextOperationAfterProducedQuantityUNIT.setEnabled(true);
        } else {
            nextOperationAfterProducedQuantity.setVisible(false);
            nextOperationAfterProducedQuantityUNIT.setVisible(false);

        }
    }

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference(PRODUCTION_IN_ONE_CYCLE);
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference(TIME_NEXT_OPERATION);
        FieldComponent areProductQuantitiesDivisible = (FieldComponent) viewDefinitionState
                .getComponentByReference("areProductQuantitiesDivisible");
        FieldComponent isTjDivisible = (FieldComponent) viewDefinitionState.getComponentByReference("isTjDivisible");

        Object value = nextOperationAfterProducedType.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        productionInOneCycle.setEnabled(true);

        nextOperationAfterProducedType.setEnabled(true);
        if (!"02specified".equals(value)) {
            nextOperationAfterProducedType.setFieldValue("01all");
        }
        timeNextOperation.setEnabled(true);
        if ("1".equals(areProductQuantitiesDivisible.getFieldValue())) {
            isTjDivisible.setEnabled(true);
        }
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
        for (String referenceName : Sets.newHashSet(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT, "productionInOneCycleUNIT")) {
            component = (FieldComponent) view.getComponentByReference(referenceName);
            if (component == null) {
                continue;
            }
            component.setFieldValue(unit);
            component.requestComponentUpdateState();
        }
    }
}
