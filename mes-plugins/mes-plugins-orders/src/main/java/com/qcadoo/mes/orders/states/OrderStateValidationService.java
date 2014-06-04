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
package com.qcadoo.mes.orders.states;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;
import static com.qcadoo.mes.states.constants.StateChangeStatus.IN_PROGRESS;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.validators.TechnologyTreeValidators;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginUtils;

@Service
public class OrderStateValidationService {

    @Autowired
    private CopyOfTechnologyValidationService copyOfTechnologyValidationService;

    @Autowired
    private TechnologyTreeValidators technologyTreeValidators;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private TechnologyStateChangeAspect technologyStateChangeAspect;

    private static final String ENTITY_IS_NULL = "entity is null";

    public void validationOnAccepted(final StateChangeContext stateChangeContext) {
        final List<String> references = Arrays.asList(DATE_TO, DATE_FROM);
        checkRequired(references, stateChangeContext);

        validateTechnologyState(stateChangeContext);
    }

    public void validationOnInProgress(final StateChangeContext stateChangeContext) {
        final List<String> references = Arrays.asList(DATE_TO, DATE_FROM);
        checkRequired(references, stateChangeContext);

        validateTechnologyState(stateChangeContext);
    }

    public void validationOnCompleted(final StateChangeContext stateChangeContext) {
        final List<String> fieldNames = Arrays.asList(DATE_TO, DATE_FROM, DONE_QUANTITY);
        checkRequired(fieldNames, stateChangeContext);
    }

    private void checkRequired(final List<String> fieldNames, final StateChangeContext stateChangeContext) {
        checkArgument(stateChangeContext != null, ENTITY_IS_NULL);
        final Entity stateChangeEntity = stateChangeContext.getOwner();
        for (String fieldName : fieldNames) {
            if (stateChangeEntity.getField(fieldName) == null) {
                stateChangeContext.addFieldValidationError(fieldName, "orders.order.orderStates.fieldRequired");
            }
        }
    }

    private void validateTechnologyState(final StateChangeContext stateChangeContext) {
        checkArgument(stateChangeContext != null, ENTITY_IS_NULL);

        final Entity order = stateChangeContext.getOwner();
        final Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology == null) {
            return;
        }
        if (!copyOfTechnologyValidationService.checkIfTechnologyTreeIsSet(stateChangeContext, technology)) {
            return;
        }
        copyOfTechnologyValidationService.checkConsumingManyProductsFromOneSubOp(stateChangeContext, technology);
        technologyTreeValidators.checkConsumingTheSameProductFromManySubOperations(technology.getDataDefinition(), technology,
                false);
        copyOfTechnologyValidationService.checkIfTechnologyHasAtLeastOneComponent(stateChangeContext, technology);
        copyOfTechnologyValidationService.checkTopComponentsProducesProductForTechnology(stateChangeContext, technology);
        copyOfTechnologyValidationService.checkIfOperationsUsesSubOperationsProds(stateChangeContext, technology);
        if (PluginUtils.isEnabled("timeNormsForOperations")) {
            copyOfTechnologyValidationService.checkIfTreeOperationIsValid(stateChangeContext, technology);
            copyOfTechnologyValidationService.checkOperationOutputQuantities(stateChangeContext, technology);
        }
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        Entity technologyDB = technologyDD.get(technology.getId());
        if (technologyDB.getStringField(TechnologyFields.STATE).equals(TechnologyState.DRAFT.getStringValue())
                || technologyDB.getStringField(TechnologyFields.STATE).equals(TechnologyState.CHECKED.getStringValue())) {
            final StateChangeStatus status = stateChangeContext.getStatus();

            if (IN_PROGRESS.equals(status)) {
                final StateChangeContext stateChangeContextT = stateChangeContextBuilder.build(
                        technologyStateChangeAspect.getChangeEntityDescriber(), technologyDB, "02accepted");

                stateChangeContextT.setStatus(StateChangeStatus.IN_PROGRESS);
                technologyStateChangeAspect.changeState(stateChangeContextT);

            }
        }

    }

}
