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
package com.qcadoo.mes.orders.util;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class OrderReportService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    public Entity printForOrder(final ComponentState state, final String plugin, final String entityName,
            final Map<String, Object> entityFieldsMap, final OrderValidator orderValidator) {
        if (!(state instanceof GridComponent)) {
            throw new IllegalStateException("method avalible only for grid");
        }

        GridComponent gridState = (GridComponent) state;
        Set<Entity> ordersEntities = new HashSet<Entity>();
        if (gridState.getSelectedEntitiesIds().isEmpty()) {
            state.addMessage("qcadooView.message.noRecordSelected", MessageType.FAILURE);
            return null;
        }
        List<ErrorMessage> errors = Lists.newLinkedList();
        for (Long orderId : gridState.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
            if (order == null) {
                errors.add(new ErrorMessage("qcadooView.message.entityNotFound"));
                continue;
            }
            ErrorMessage errorMessage = orderValidator.validateOrder(order);
            if (errorMessage == null) {
                ordersEntities.add(order);
            } else {
                errors.add(errorMessage);
            }
        }
        if (errors.isEmpty()) {
            return createNewOrderPrint(ordersEntities, plugin, entityName, entityFieldsMap, state.getLocale());
        } else {
            StringBuilder errorMessages = new StringBuilder();
            for (ErrorMessage error : errors) {
                errorMessages.append(" - ");
                String translatedError = translationService.translate(error.getMessage(), state.getLocale(), error.getVars());
                errorMessages.append(translatedError);
                errorMessages.append("\n");
            }
            state.addTranslatedMessage(errorMessages.toString(), MessageType.FAILURE, false);
        }
        return null;
    }

    private Entity createNewOrderPrint(final Set<Entity> orders, final String plugin, final String entityName,
            final Map<String, Object> entityFieldsMap, final Locale locale) {

        DataDefinition data = dataDefinitionService.get(plugin, entityName);

        Entity entity = data.create();

        entity.setField("name", generateOrderPrintName(orders, locale));
        entity.setField("generated", true);
        entity.setField("worker", securityService.getCurrentUserName());
        entity.setField("date", new Date());
        if (data.getField("orders") != null) {
            entity.setField("orders", Lists.newArrayList(orders));
        }

        if (entityFieldsMap != null) {
            for (Map.Entry<String, Object> entityFieldsMapEntry : entityFieldsMap.entrySet()) {
                entity.setField(entityFieldsMapEntry.getKey(), entityFieldsMapEntry.getValue());
            }
        }

        Entity saved = data.save(entity);

        if (!saved.isValid()) {
            throw new IllegalStateException("Entity " + saved + " is not valid! - " + saved.getErrors() + " / "
                    + saved.getGlobalErrors());
        }

        return saved;
    }

    private String generateOrderPrintName(final Set<Entity> orders, final Locale locale) {
        StringBuilder materialReqName = new StringBuilder();
        materialReqName.append(translationService.translate("materialRequirements.materialReq.forOrder", locale));
        int ordersCounter = 0;
        for (Entity order : orders) {
            if (ordersCounter > 2) {
                materialReqName.deleteCharAt(materialReqName.length() - 1);
                materialReqName.append("... (");
                materialReqName.append(translationService.translate("materialRequirements.materialReq.summary", locale));
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

    public interface OrderValidator {

        ErrorMessage validateOrder(Entity order);
    }

}