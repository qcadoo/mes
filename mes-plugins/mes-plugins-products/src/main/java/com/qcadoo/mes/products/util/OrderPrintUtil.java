/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.products.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.components.grid.GridComponentState;

@Service
public class OrderPrintUtil {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    public Entity printMaterialReqForOrder(final ComponentState state) {

        Map<String, Object> entityFieldsMap = new HashMap<String, Object>();
        entityFieldsMap.put("onlyComponents", false);

        OrderValidator orderValidator = new OrderValidator() {

            @Override
            public String validateOrder(Entity order) {
                if (order.getField("technology") == null) {
                    return order.getField("number")
                            + ": "
                            + translationService.translate("products.validate.global.error.orderMustHaveTechnology",
                                    state.getLocale());
                }
                return null;
            }
        };

        return printForOrder(state, "materialRequirement", "materialRequirementComponent", entityFieldsMap, orderValidator);
    }

    public Entity printWorkPlanForOrder(final ComponentState state) {

        OrderValidator orderValidator = new OrderValidator() {

            @Override
            public String validateOrder(Entity order) {
                if (order.getField("technology") == null) {
                    return order.getField("number")
                            + ": "
                            + translationService.translate("products.validate.global.error.orderMustHaveTechnology",
                                    state.getLocale());
                } else if (order.getBelongsToField("technology").getTreeField("operationComponents").isEmpty()) {
                    return order.getField("number")
                            + ": "
                            + translationService.translate("products.validate.global.error.orderTechnologyMustHaveOperation",
                                    state.getLocale());
                }
                return null;
            }
        };

        return printForOrder(state, "workPlan", "workPlanComponent", null, orderValidator);
    }

    private Entity printForOrder(final ComponentState state, final String entityName, final String detailEntityName,
            final Map<String, Object> entityFieldsMap, final OrderValidator orderValidator) {
        if (!(state instanceof GridComponentState)) {
            throw new IllegalStateException("method avalible only for grid");
        }

        GridComponentState gridState = (GridComponentState) state;
        Set<Entity> ordersEntities = new HashSet<Entity>();
        if (gridState.getSelectedEntitiesId().size() == 0) {
            state.addMessage(translationService.translate("core.message.noRecordSelected", state.getLocale()),
                    MessageType.FAILURE);
            return null;
        }
        List<String> errors = new LinkedList<String>();
        for (Long orderId : gridState.getSelectedEntitiesId()) {
            Entity order = dataDefinitionService.get("products", "order").get(orderId);
            if (order == null) {
                errors.add(translationService.translate("core.message.entityNotFound", state.getLocale()));
                continue;
            }
            String validateMessage = orderValidator.validateOrder(order);
            if (validateMessage == null) {
                ordersEntities.add(order);
            } else {
                errors.add(validateMessage);
            }
        }
        if (errors.size() != 0) {
            StringBuilder errorMessage = new StringBuilder();
            for (String error : errors) {
                errorMessage.append(" - ");
                errorMessage.append(error);
                errorMessage.append("\n");
            }
            state.addMessage(errorMessage.toString(), MessageType.FAILURE, false);
        } else {
            return createNewOrderPrint(ordersEntities, entityName, detailEntityName, entityFieldsMap, state.getLocale());
        }
        return null;
    }

    private Entity createNewOrderPrint(final Set<Entity> orders, final String entityName, final String detailEntityName,
            final Map<String, Object> entityFieldsMap, final Locale locale) {

        Entity materialReq = new DefaultEntity("products", entityName);

        materialReq.setField("name", generateOrderPrintName(orders, locale));
        materialReq.setField("generated", true);
        materialReq.setField("worker", securityService.getCurrentUserName());
        materialReq.setField("date", new Date());
        if (entityFieldsMap != null) {
            for (Map.Entry<String, Object> entityFieldsMapEntry : entityFieldsMap.entrySet()) {
                materialReq.setField(entityFieldsMapEntry.getKey(), entityFieldsMapEntry.getValue());
            }
        }

        DataDefinition data = dataDefinitionService.get("products", entityName);
        Entity saved = data.save(materialReq);

        for (Entity order : orders) {
            Entity materialReqComponent = new DefaultEntity("products", detailEntityName);
            materialReqComponent.setField("order", order);
            materialReqComponent.setField(entityName, saved);
            dataDefinitionService.get("products", detailEntityName).save(materialReqComponent);
        }

        saved = data.get(saved.getId());

        return saved;
    }

    private String generateOrderPrintName(final Set<Entity> orders, final Locale locale) {
        StringBuilder materialReqName = new StringBuilder();
        materialReqName.append(translationService.translate("products.materialReq.forOrder", locale));
        int ordersCounter = 0;
        for (Entity order : orders) {
            if (ordersCounter > 2) {
                materialReqName.deleteCharAt(materialReqName.length() - 1);
                materialReqName.append("... (");
                materialReqName.append(translationService.translate("products.materialReq.summary", locale));
                materialReqName.append(" ");
                materialReqName.append(orders.size());
                materialReqName.append(")R");
                break;
            }
            materialReqName.append(" ");
            materialReqName.append(order.getField("number"));
            materialReqName.append(",");
            ordersCounter++;
        }
        materialReqName.deleteCharAt(materialReqName.length() - 1);
        return materialReqName.toString();
    }

}

interface OrderValidator {

    String validateOrder(Entity order);
}
