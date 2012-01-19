/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.orderGroups;

import static com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants.DATE_RANGE_ERROR;
import static com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants.ORDER_DATES_RANGE_ERROR;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderGroupsService {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    private static final String DATE_FROM_FIELD = "dateFrom";

    private static final String DATE_TO_FIELD = "dateTo";

    private static final String NUMBER_FIELD = "number";

    private static final String NAME_FIELD = "name";

    private static final String ORDERS_FIELD = "orders";

    private static final String ORDER_GROUP_FIELD = "orderGroup";

    private static final String ORDER_GROUP_NAME_FIELD = "orderGroupName";

    /* ****** HOOKS ******* */

    public final void generateNumberAndName(final ViewDefinitionState view) {
        ComponentState number = view.getComponentByReference(NUMBER_FIELD);
        if (StringUtils.hasText((String) number.getFieldValue())) {
            return;
        }

        numberGeneratorService.generateAndInsertNumber(view, OrderGroupsConstants.PLUGIN_IDENTIFIER,
                OrderGroupsConstants.MODEL_ORDERGROUP, "form", NUMBER_FIELD);

        ComponentState name = view.getComponentByReference(NAME_FIELD);
        if (StringUtils.hasText((String) name.getFieldValue())) {
            return;
        }
        name.setFieldValue(translationService.translate("orderGroups.orderGroup.name.default", view.getLocale(),
                (String) number.getFieldValue()));
    }

    /* ****** VALIDATORS ****** */

    public final boolean validateDates(final DataDefinition dataDefinition, final Entity orderGroup) {
        Date dateFrom = (Date) orderGroup.getField(DATE_FROM_FIELD);
        Date dateTo = (Date) orderGroup.getField(DATE_TO_FIELD);
        if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
            orderGroup.addError(dataDefinition.getField(DATE_TO_FIELD), "orderGroups.validate.error.badDatesOrder");
            return false;
        }

        EntityList orders = orderGroup.getHasManyField(ORDERS_FIELD);
        return checkOrderGroupDateBoundary(orderGroup, orders, DATE_RANGE_ERROR, orderGroup);
    }

    public final boolean validateOrderDate(final DataDefinition dataDefinition, final Entity order) {
        Entity group = order.getBelongsToField(ORDER_GROUP_FIELD);
        return checkOrderGroupDateBoundary(group, Lists.newArrayList(order), ORDER_DATES_RANGE_ERROR, order);
    }

    public final boolean checkOrderGroupDateBoundary(final Entity group, final List<Entity> orders, final String errorMessage,
            final Entity errorsHolder) {
        if (group == null || orders == null || orders.isEmpty()) {
            return true;
        }
        long groupDateTo = getDateFieldFromEntity(group, DATE_TO_FIELD);
        long groupDateFrom = getDateFieldFromEntity(group, DATE_FROM_FIELD);
        if (groupDateTo > 0) {
            groupDateTo = getTimeOfDayEnd(groupDateTo);
        }
        boolean isValid = true;
        for (Entity order : orders) {
            long orderDateFrom = getDateFieldFromEntity(order, DATE_FROM_FIELD);
            long orderDateTo = getDateFieldFromEntity(order, DATE_TO_FIELD);
            if (groupDateFrom * orderDateFrom != 0 && groupDateFrom > orderDateFrom) {
                errorsHolder.addError(errorsHolder.getDataDefinition().getField(DATE_FROM_FIELD), errorMessage,
                        order.getStringField(NAME_FIELD));
                isValid = false;
            }
            if (groupDateTo * orderDateTo != 0 && groupDateTo < orderDateTo) {
                errorsHolder.addError(errorsHolder.getDataDefinition().getField(DATE_TO_FIELD), errorMessage,
                        order.getStringField(NAME_FIELD));
                isValid = false;
            }
            if (!isValid) {
                break;
            }
        }
        return isValid;
    }

    private long getDateFieldFromEntity(final Entity entity, final String fieldName) {
        if (entity.getField(fieldName) == null) {
            return 0;
        }
        return ((Date) entity.getField(fieldName)).getTime();
    }

    private long getTimeOfDayEnd(final long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        // important! change end time to 23:59:59.999
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        return cal.getTime().getTime();
    }

    public final void updateBelongingOrdersOrderGroupName(final DataDefinition groupDataDefinition, final Entity orderGroup) {
        List<Entity> orders = orderGroup.getHasManyField(ORDERS_FIELD);
        if (orders == null) {
            return;
        }
        for (Entity order : orders) {
            order.getDataDefinition().save(order);
        }
    }

    public final void updateOrderGroupName(final DataDefinition orderDataDefinition, final Entity order) {
        Entity orderGroup = order.getBelongsToField(ORDER_GROUP_FIELD);
        if (orderGroup == null) {
            order.setField(ORDER_GROUP_NAME_FIELD, null);
            return;
        }
        order.setField(ORDER_GROUP_NAME_FIELD, orderGroup.getStringField(NAME_FIELD));
    }

    public final void showInOrdersList(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) componentState;
        Entity orderGroup = form.getEntity();
        if (orderGroup == null) {
            return;
        }
        String orderGroupName = orderGroup.getStringField(NAME_FIELD);
        if (orderGroupName == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put(ORDER_GROUP_FIELD, orderGroupName);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put("filters", filters);

        Map<String, Object> componentsOptions = Maps.newHashMap();
        componentsOptions.put("grid.options", gridOptions);

        componentsOptions.put("window.activeMenu", "orders.productionOrders");

        view.redirectTo("/page/orders/ordersList.html", false, true, componentsOptions);
    }
}
