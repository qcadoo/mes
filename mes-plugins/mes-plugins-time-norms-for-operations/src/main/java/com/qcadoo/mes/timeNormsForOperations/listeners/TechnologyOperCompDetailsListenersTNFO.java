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
package com.qcadoo.mes.timeNormsForOperations.listeners;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.ALL;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.ARE_PRODUCT_QUANTITIES_DIVISIBLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.IS_TJ_DIVISIBLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_TYPE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.SPECIFIED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_OPERATION;
import static com.qcadoo.view.api.ComponentState.MessageType.INFO;
import static com.qcadoo.view.api.ComponentState.MessageType.SUCCESS;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationFieldsTFNO;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologyOperCompDetailsListenersTNFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyTimeNormsFromOperation(final ViewDefinitionState view, final ComponentState operationLookupState,
                                           final String[] args) {

        ComponentState operationLookup = view.getComponentByReference(OPERATION);
        if (operationLookup.getFieldValue() == null) {
            if (!OPERATION.equals(operationLookupState.getName())) {
                view.getComponentByReference(QcadooViewConstants.L_FORM)
                        .addMessage("productionTimeNorms.messages.info.missingOperationReference", INFO);
            }
            return;
        }

        Entity operation = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION)
                .get((Long) operationLookup.getFieldValue());

        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (formComponent.getEntityId() != null) {
            Entity toc = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .get(formComponent.getEntityId());

            copyOperationWorkstationTimes(toc, operation);
        }
        applyTimeNormsFromGivenSource(view, operation, FIELDS_OPERATION);
    }

    public void copyTimeNormsFromOperationForTechnologies(final ViewDefinitionState view, final ComponentState componentState,
                                                          final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> selectedEntities = grid.getSelectedEntitiesIds();
        selectedEntities.forEach(techId -> {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(techId);

            for (Entity toc : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
                Entity operation = toc.getBelongsToField(OPERATION);
                copyOperationWorkstationTimes(toc, operation);
                applyTimeNormsFromGivenSource(toc, operation);
            }
        });
        view.addMessage("qcadooView.notification.success", SUCCESS);
    }

    private void copyOperationWorkstationTimes(Entity toc, Entity operation) {
        for (Entity operationWorkstationTime : operation.getHasManyField(OperationFieldsTFNO.OPERATION_WORKSTATION_TIMES)) {
            for (Entity techOperCompWorkstationTime : toc
                    .getHasManyField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_WORKSTATION_TIMES)) {
                if (techOperCompWorkstationTime.getBelongsToField(TechOperCompWorkstationTimeFields.WORKSTATION).getId()
                        .equals(operationWorkstationTime.getBelongsToField(OperationWorkstationTimeFields.WORKSTATION).getId())) {
                    techOperCompWorkstationTime.setField(TechOperCompWorkstationTimeFields.TPZ,
                            operationWorkstationTime.getField(OperationWorkstationTimeFields.TPZ));
                    techOperCompWorkstationTime.setField(TechOperCompWorkstationTimeFields.TJ,
                            operationWorkstationTime.getField(OperationWorkstationTimeFields.TJ));
                    techOperCompWorkstationTime.setField(TechOperCompWorkstationTimeFields.TIME_NEXT_OPERATION,
                            operationWorkstationTime.getField(OperationWorkstationTimeFields.TIME_NEXT_OPERATION));
                    techOperCompWorkstationTime.getDataDefinition().save(techOperCompWorkstationTime);
                    break;
                }
            }
        }
    }

    void applyTimeNormsFromGivenSource(Entity toc, final Entity source) {
        checkArgument(source != null, "source entity is null");

        for (String fieldName : com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_OPERATION) {
            toc.setField(fieldName, source.getField(fieldName));
        }

        if (source.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE) == null) {
            toc.setField(NEXT_OPERATION_AFTER_PRODUCED_TYPE, ALL);
        }

        if (source.getField(PRODUCTION_IN_ONE_CYCLE) == null) {
            toc.setField(PRODUCTION_IN_ONE_CYCLE, BigDecimal.ONE);
        }

        if (source.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY) == null) {
            toc.setField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY, BigDecimal.ZERO);
        }
        toc.getDataDefinition().save(toc);
    }

    void applyTimeNormsFromGivenSource(final ViewDefinitionState view, final Entity source, final Iterable<String> fields) {
        checkArgument(source != null, "source entity is null");
        FieldComponent component = null;

        for (String fieldName : fields) {
            component = (FieldComponent) view.getComponentByReference(fieldName);
            component.setFieldValue(source.getField(fieldName));
        }

        if (source.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE) == null) {
            view.getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE).setFieldValue(ALL);
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

        boolean visibilityValue = SPECIFIED.equals(nextOperationAfterProducedType.getFieldValue());
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

        boolean visibilityValue = SPECIFIED.equals(nextOperationAfterProducedType.getFieldValue());
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
