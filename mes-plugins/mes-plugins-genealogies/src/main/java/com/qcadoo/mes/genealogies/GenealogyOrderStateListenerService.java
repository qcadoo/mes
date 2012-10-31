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
package com.qcadoo.mes.genealogies;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class GenealogyOrderStateListenerService {

    private static final String PRODUCT_IN_COMPONENT_FIELD = "productInComponent";

    private static final String PRODUCT_IN_COMPONENTS_FIELD = "productInComponents";

    private static final String OTHER_FEATURES_FIELD = "otherFeatures";

    private static final String POST_FEATURES_FIELD = "postFeatures";

    private static final String SHIFT_FEATURES_FIELD = "shiftFeatures";

    private static final String OPERATION_PRODUCT_IN_COMPONENTS_FIELD = "operationProductInComponents";

    private static final String BATCH_FIELD = "batch";

    private static final String OPERATION_COMPONENTS_FIELD = "operationComponents";

    private static final String TECHNOLOGY_FIELD = "technology";

    private static final String GENEALOGIES_FIELD = "genealogies";

    private static final String OTHER_FEATURE_REQUIRED_FIELD = "otherFeatureRequired";

    private static final String POST_FEATURE_REQUIRED_FIELD = "postFeatureRequired";

    private static final String SHIFT_FEATURE_REQUIRED_FIELD = "shiftFeatureRequired";

    private static final String BATCH_REQUIRED_FIELD = "batchRequired";

    private static final String BATCH_FOR_DONE_ORDER_FIELD = "batchForDoneOrder";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private AutoGenealogyService autoGenealogyService;

    public void onCompleted(final StateChangeContext stateChangeContext) {
        Entity parameter = parameterService.getParameter();

        if ("02active".equals(parameter.getStringField(BATCH_FOR_DONE_ORDER_FIELD))) {
            autoGenealogyService.createGenealogy(stateChangeContext.getOwner(), stateChangeContext, false);
        } else if ("03lastUsed".equals(parameter.getStringField(BATCH_FOR_DONE_ORDER_FIELD))) {
            autoGenealogyService.createGenealogy(stateChangeContext.getOwner(), stateChangeContext, true);
        }
        if (checkAutogenealogyRequired() && !checkRequiredBatch(stateChangeContext.getOwner())) {
            stateChangeContext.addMessage("genealogies.message.batchNotFound", StateMessageType.FAILURE);
        }
    }

    private boolean checkAutogenealogyRequired() {
        Entity parameter = parameterService.getParameter();
        return !("01none".equals(parameter.getStringField(BATCH_FOR_DONE_ORDER_FIELD)));
    }

    private boolean checkRequiredBatch(final Entity entity) {
        checkArgument(entity != null, "order is null");

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());
        Entity technology = order.getBelongsToField(TECHNOLOGY_FIELD);
        if (technology != null) {
            if (order.getHasManyField(GENEALOGIES_FIELD).size() == 0) {
                if ((Boolean) technology.getField(BATCH_REQUIRED_FIELD)) {
                    return false;
                }
                if ((Boolean) technology.getField(SHIFT_FEATURE_REQUIRED_FIELD)) {
                    return false;
                }
                if ((Boolean) technology.getField(POST_FEATURE_REQUIRED_FIELD)) {
                    return false;
                }
                if ((Boolean) technology.getField(OTHER_FEATURE_REQUIRED_FIELD)) {
                    return false;
                }
                for (Entity operationComponent : technology.getTreeField(OPERATION_COMPONENTS_FIELD)) {
                    for (Entity operationProductComponent : operationComponent
                            .getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS_FIELD)) {
                        if ((Boolean) operationProductComponent.getField(BATCH_REQUIRED_FIELD)) {
                            return false;
                        }
                    }
                }
            }
            for (Entity genealogy : order.getHasManyField(GENEALOGIES_FIELD)) {
                if ((Boolean) technology.getField(BATCH_REQUIRED_FIELD) && genealogy.getField(BATCH_FIELD) == null) {
                    return false;
                }
                if ((Boolean) technology.getField(SHIFT_FEATURE_REQUIRED_FIELD)) {
                    List<Entity> entityList = genealogy.getHasManyField(SHIFT_FEATURES_FIELD);
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField(POST_FEATURE_REQUIRED_FIELD)) {
                    List<Entity> entityList = genealogy.getHasManyField(POST_FEATURES_FIELD);
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField(OTHER_FEATURE_REQUIRED_FIELD)) {
                    List<Entity> entityList = genealogy.getHasManyField(OTHER_FEATURES_FIELD);
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                for (Entity genealogyProductIn : genealogy.getHasManyField(PRODUCT_IN_COMPONENTS_FIELD)) {
                    if ((Boolean) (genealogyProductIn.getBelongsToField(PRODUCT_IN_COMPONENT_FIELD)
                            .getField(BATCH_REQUIRED_FIELD))) {
                        List<Entity> entityList = genealogyProductIn.getHasManyField(BATCH_FIELD);
                        if (entityList.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
