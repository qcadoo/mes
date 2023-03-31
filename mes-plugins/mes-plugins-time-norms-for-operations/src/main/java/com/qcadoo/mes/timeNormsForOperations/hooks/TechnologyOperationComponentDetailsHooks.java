/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.*;

@Service
public class TechnologyOperationComponentDetailsHooks {

    private List<String> references = Lists.newArrayList("tpz", "productionInOneCycle", "productionInOneCycleUNIT", "tj",
            "timeNextOperation", "pieceworkProduction", "areProductQuantitiesDivisible", "isTjDivisible", "minStaff", "optimalStaff",
            "tjDecreasesForEnlargedStaff", "machineUtilization", "laborUtilization", "nextOperationAfterProducedType",
            "nextOperationAfterProducedQuantity", "nextOperationAfterProducedQuantityUNIT");

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void checkOperationOutputQuantities(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        Entity toc = form.getEntity();
        toc = getTechnologyOperationComponentDD().get(toc.getId());

        Entity technology = toc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

        if (!TechnologyState.DRAFT.getStringValue().equals(technology.getStringField(TechnologyFields.STATE))) {
            for (String reference : references) {
                FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
                field.setEnabled(false);
            }
            GridComponent techOperCompWorkstationTimesGrid = (GridComponent) view.getComponentByReference("techOperCompWorkstationTimes");
            techOperCompWorkstationTimesGrid.setEnabled(false);
        }

        Entity operationComponent = form.getEntity();
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());

        BigDecimal timeNormsQuantity = operationComponent.getDecimalField(PRODUCTION_IN_ONE_CYCLE);

        Entity productOutComponent;

        try {
            productOutComponent = technologyService.getMainOutputProductComponent(operationComponent);
        } catch (IllegalStateException e) {
            return;
        }

        BigDecimal currentQuantity = productOutComponent.getDecimalField("quantity");

        if (timeNormsQuantity != null && timeNormsQuantity.compareTo(currentQuantity) != 0) {
            form.addMessage("technologies.technologyOperationComponent.validate.error.invalidQuantity", MessageType.INFO, false,
                    numberService.format(currentQuantity),
                    productOutComponent.getBelongsToField("product").getStringField(ProductFields.UNIT));
        }
    }

    public void updateNextOperationAfterProducedQuantityFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent nextOperationAfterProducedQuantity = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY);
        FieldComponent nextOperationAfterProducedQuantityUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT);

        if (SPECIFIED.equals(nextOperationAfterProducedType.getFieldValue())) {
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
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference(TPZ);
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference(TJ);
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference(PRODUCTION_IN_ONE_CYCLE);
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference(TIME_NEXT_OPERATION);
        FieldComponent areProductQuantitiesDivisible = (FieldComponent) viewDefinitionState
                .getComponentByReference(ARE_PRODUCT_QUANTITIES_DIVISIBLE);
        FieldComponent isTjDivisible = (FieldComponent) viewDefinitionState.getComponentByReference(IS_TJ_DIVISIBLE);

        Object value = nextOperationAfterProducedType.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        productionInOneCycle.setEnabled(true);

        nextOperationAfterProducedType.setEnabled(true);
        if (!SPECIFIED.equals(value)) {
            nextOperationAfterProducedType.setFieldValue(ALL);
        }
        timeNextOperation.setEnabled(true);
        if ("1".equals(areProductQuantitiesDivisible.getFieldValue())) {
            isTjDivisible.setEnabled(true);
        }
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FieldComponent component = null;
        Entity formEntity = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntity();

        // we can pass units only to technology level operations
        if (formEntity.getId() == null
                || !TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(formEntity.getDataDefinition().getName())) {
            return;
        }

        // be sure that entity isn't in detached state before you wander through the relationship
        formEntity = formEntity.getDataDefinition().get(formEntity.getId());
        // you can use someEntity.getSTH().getSTH() only when you are 100% sure that all the passers-relations
        // will not return null (i.e. all relations using below are mandatory on the model definition level)
        String unit = formEntity.getBelongsToField("technology").getBelongsToField(TechnologyFields.PRODUCT).getField(ProductFields.UNIT).toString();
        for (String referenceName : Sets.newHashSet(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT, TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE_UNIT)) {
            component = (FieldComponent) view.getComponentByReference(referenceName);
            if (component == null) {
                continue;
            }
            component.setFieldValue(unit);
            component.requestComponentUpdateState();
        }
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }
}
