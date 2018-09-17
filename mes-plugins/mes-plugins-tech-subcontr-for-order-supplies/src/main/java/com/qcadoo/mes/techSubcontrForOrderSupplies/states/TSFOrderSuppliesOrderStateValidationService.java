/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.techSubcontrForOrderSupplies.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class TSFOrderSuppliesOrderStateValidationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void validationOnAccepted(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (!TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)
                && areSubcontracedOperations(order)) {
            stateChangeContext.addFieldValidationError(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
                    "orders.order.typeOfProductionRecording.error.typeIsNotForEachAndAreSubcontractedOperations");
        }
    }

    private boolean areSubcontracedOperations(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        SearchResult searchResult = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                .add(SearchRestrictions.eq(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING, true)).list();

        return !searchResult.getEntities().isEmpty();
    }

}
