/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.10
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderGroupsService {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    /* ****** HOOKS ******* */

    public void generateNumberAndName(final ViewDefinitionState viewDefinitionState) {
        FieldComponent number = (FieldComponent) viewDefinitionState.getComponentByReference("number");

        if (StringUtils.hasText((String) number.getFieldValue())) {
            return;
        }

        numberGeneratorService.generateAndInsertNumber(viewDefinitionState, OrderGroupsConstants.PLUGIN_IDENTIFIER,
                OrderGroupsConstants.MODEL_ORDERGROUP, "form", "number");
        FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference("name");

        if (!StringUtils.hasText((String) name.getFieldValue())) {
            name.setFieldValue(translationService.translate("orderGroups.orderGroup.name.default",
                    viewDefinitionState.getLocale(), (String) number.getFieldValue()));
        }
    }

    /* ****** VALIDATORS ****** */

    public boolean validateDates(final DataDefinition dataDefinition, final Entity orderGroup) {
        // Date dateFrom = (Date) orderGroup.getField("dateFrom");
        // Date dateTo = (Date) orderGroup.getField("dateTo");
        // if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
        // orderGroup.addError(dataDefinition.getField("dateTo"), "orderGroups.validate.error.badDatesOrder");
        // return false;
        // }

        EntityList orders = orderGroup.getHasManyField("orders");
        // if (orders != null && orders.size() > 0) {
        // if (!checkOrderGroupComponentDateRange(orderGroup, orders)) {
        // orderGroup.addError(dataDefinition.getField("dateFrom"), OrderGroupsConstants.DATE_RANGE_ERROR);
        // orderGroup.addError(dataDefinition.getField("dateTo"), OrderGroupsConstants.DATE_RANGE_ERROR);
        // return false;
        // }
        // }
        // return true;
        return checkOrderGroupComponentDateRange(orderGroup, orders);
    }

    public boolean validateOrderDate(final DataDefinition dataDefinition, final Entity order) {
        Entity group = order.getBelongsToField("orderGroup");
        return checkOrderGroupComponentDateRange(group, Lists.newArrayList(order));
    }

    public boolean checkOrderGroupComponentDateRange(final Entity group, final List<Entity> orders) {
        if (group == null || orders.isEmpty()) {
            return true;
        }

        Date groupDateTo = (Date) group.getField("dateTo");
        Date groupDateFrom = (Date) group.getField("dateFrom");
        Long orderDateFrom = null;
        Long orderDateTo = null;

        if (groupDateTo != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) group.getField("dateTo"));
            // important! change end time to 23:59:59.999
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.MILLISECOND, -1);
            groupDateTo = cal.getTime();
        }

        DataDefinition dataDefinition = group.getDataDefinition();

        for (Entity order : orders) {
            orderDateFrom = ((Date) order.getField("dateFrom")).getTime();
            orderDateTo = ((Date) order.getField("dateTo")).getTime();

            if (groupDateFrom != null && groupDateFrom.getTime() > orderDateFrom) {
                order.addError(dataDefinition.getField("dateFrom"), OrderGroupsConstants.ORDER_DATES_RANGE_ERROR);
                return false;
            }

            if (groupDateTo != null && groupDateTo.getTime() < orderDateTo) {
                order.addError(dataDefinition.getField("dateTo"), OrderGroupsConstants.ORDER_DATES_RANGE_ERROR);
                return false;
            }
        }
        return true;
    }

    /* ****** MODEL HOOKS ****** */

    public void setGroupNameWhenLoadOrder(final DataDefinition dataDefinition, final Entity order) {
        Entity orderGroup = order.getBelongsToField("orderGroup");
        if (orderGroup == null) {
            return;
        }
        order.setField("orderGroupName", orderGroup.getField("name"));
        order.getDataDefinition().save(order);
    }

}
