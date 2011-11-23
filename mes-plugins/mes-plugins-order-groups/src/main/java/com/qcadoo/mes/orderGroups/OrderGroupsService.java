/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants.DATE_RANGE_ERROR;
import static com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants.MODEL_ORDERGROUP;
import static com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.view.api.ComponentState.MessageType.FAILURE;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchDisjunction;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderGroupsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    private static final Logger LOG = LoggerFactory.getLogger(OrderGroupsService.class);

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

    public void toggleRibbonAddOrderButtons(final ViewDefinitionState viewDefinitionState) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        window.getRibbon().getGroupByName("ordersActions").getItemByName("addManyOrders")
                .setEnabled((form.getEntityId() != null));
    }

    /* ****** CUSTOM EVENT LISTENER ****** */

    @Transactional
    public void removeOrderFromGroup(final ViewDefinitionState viewDefinitionState, ComponentState componentState, String[] args) {
        if ("orderGroupOrders".equals(componentState.getName())) {
            DataDefinition orderDataDefinition = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER);
            GridComponent grid = (GridComponent) componentState;

            for (Long orderId : grid.getSelectedEntitiesIds()) {
                Entity order = orderDataDefinition.get(orderId);
                order.setField("orderGroup", null);
                order.setField("orderGroupName", null);
                orderDataDefinition.save(order);
            }
        }
    }

    public void redirectToGroupsList(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        // FIXME MAKU
        state.redirectTo("../page/orderGroups/orderGroupList.html", false, true);
    }

    public void showOrderGroupOrdersTable(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        JSONObject context = new JSONObject();
        StringBuilder header = new StringBuilder();
        String name = state.getComponentByReference("name").getFieldValue().toString();
        String groupDateFrom = state.getComponentByReference("groupDateFrom").getFieldValue().toString();
        String groupDateTo = state.getComponentByReference("groupDateTo").getFieldValue().toString();

        header.append(name);
        header.append("</span> ( ");
        header.append(groupDateFrom.isEmpty() ? "..." : groupDateFrom);
        header.append(" : ");
        header.append(groupDateTo.isEmpty() ? "..." : groupDateTo);
        header.append(" )<span>");

        try {
            context.put("orderGroup.id", componentState.getFieldValue().toString());
            context.put("form.name", header.toString());
        } catch (JSONException e) {
            LOG.debug(e.getMessage());
        }

        StringBuilder url = new StringBuilder();
        url.append("../page/orderGroups/orderGroupOrdersTable.html?context=");
        url.append(context.toString());

        state.redirectTo(url.toString(), false, true);
    }

    @Transactional
    public void addOrdersToGroup(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        Long groupId = ((FormComponent) viewDefinitionState.getComponentByReference("orderGroup")).getEntityId();
        if (groupId == null) {
            return;
        }

        Set<Entity> orderEntities = new HashSet<Entity>();
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        GridComponent orders = (GridComponent) viewDefinitionState.getComponentByReference("orders");
        DataDefinition orderDataDef = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER);
        Entity group = dataDefinitionService.get(OrderGroupsConstants.PLUGIN_IDENTIFIER, MODEL_ORDERGROUP).get(groupId);

        // get orders entities
        for (Long orderId : orders.getSelectedEntitiesIds()) {
            orderEntities.add(orderDataDef.get(orderId));
        }

        // validate data ranges
        if (!checkOrderGroupComponentDateRange(group, orderEntities)) {
            // disable infinity calling component onChange listener (bug with infinity error messages loop)
            viewDefinitionState.getComponentByReference("confirmBoxTextHolder").setFieldValue(null);
            form.addMessage(translationService.translate(DATE_RANGE_ERROR, viewDefinitionState.getLocale()), FAILURE);
            return;
        }

        // Save if validation pass
        for (Entity entity : orderEntities) {
            entity.setField("orderGroup", groupId);
            entity.setField("orderGroupName", group.getField("name"));
            orderDataDef.save(entity);
        }

        // Redirect to 'successful' view (perform action like window.goBack)
        viewDefinitionState.redirectTo("/page/orderGroups/orderGroupDetails.html?context={\"form.id\":\"" + groupId + "\"}",
                false, true);
    }

    public void prepareConfirmText(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        final WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        final FormComponent groupForm = (FormComponent) viewDefinitionState.getComponentByReference("orderGroup");
        FieldComponent textarea = (FieldComponent) viewDefinitionState.getComponentByReference("confirmBoxTextHolder");
        final GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("orders");

        if (grid == null || groupForm == null || textarea == null || window == null) {
            return;
        }

        Long groupId = groupForm.getEntityId();
        Entity group = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_ORDERGROUP).get(groupId);

        checkArgument(group != null, "unable to load orderGroup entity with id = " + groupId);

        RibbonActionItem ribbonItemSelect = window.getRibbon().getGroupByName("navigation").getItemByName("select");
        textarea.setFieldValue("NULL");

        if (grid.getSelectedEntitiesIds().size() == 0) {
            ribbonItemSelect.setEnabled(false);
            return;
        }
        ribbonItemSelect.setEnabled(true);

        DataDefinition dd = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER);
        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        for (Long id : grid.getSelectedEntitiesIds()) {
            disjunction.add(SearchRestrictions.idEq(id));
        }

        SearchResult res = dd.find().createAlias("orderGroup", "orderGroup")
                .add(SearchRestrictions.and(disjunction, SearchRestrictions.isNotNull("orderGroup"))).list();
        if (res.getTotalNumberOfEntities() == 0) {
            return;
        }

        String groupName = group.getField("name").toString();
        StringBuilder confirmTextBuilder = new StringBuilder();
        for (Entity order : res.getEntities()) {
            if (group.getId().equals(order.getBelongsToField("orderGroup").getId())) {
                continue;
            }
            confirmTextBuilder.append('"');
            confirmTextBuilder.append(order.getField("name"));
            confirmTextBuilder.append("\":\n\t\"");
            confirmTextBuilder.append(order.getField("orderGroupName"));
            confirmTextBuilder.append("\" --> \"");
            confirmTextBuilder.append(groupName);
            confirmTextBuilder.append("\"\n");
        }

        if (confirmTextBuilder.length() == 0) {
            return;
        }

        String confirmText = translationService.translate("orderGroups.popup.confirm.confirmOrderTransferText",
                viewDefinitionState.getLocale());
        textarea.setFieldValue(confirmText + "\n\n" + confirmTextBuilder.toString());
    }

    /* ****** VALIDATORS ****** */

    public boolean validateDates(final DataDefinition dataDefinition, final Entity orderGroup) {
        Date dateFrom = (Date) orderGroup.getField("dateFrom");
        Date dateTo = (Date) orderGroup.getField("dateTo");
        if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
            orderGroup.addError(dataDefinition.getField("dateTo"), "orderGroups.validate.error.badDatesOrder");
            return false;
        }

        EntityList orders = orderGroup.getHasManyField("orders");
        if (orders != null && orders.size() > 0) {
            if (!checkOrderGroupComponentDateRange(orderGroup, orders)) {
                orderGroup.addError(dataDefinition.getField("dateFrom"), OrderGroupsConstants.DATE_RANGE_ERROR);
                orderGroup.addError(dataDefinition.getField("dateTo"), OrderGroupsConstants.DATE_RANGE_ERROR);
                return false;
            }
        }
        return true;
    }

    public boolean validateOrderDate(final DataDefinition dataDefinition, final Entity order) {
        Entity group = order.getBelongsToField("orderGroup");
        if (group != null && !checkOrderGroupComponentDateRange(group, Arrays.asList(order))) {
            order.addError(dataDefinition.getField("dateFrom"), OrderGroupsConstants.ORDER_DATES_RANGE_ERROR);
            order.addError(dataDefinition.getField("dateTo"), OrderGroupsConstants.ORDER_DATES_RANGE_ERROR);
        }
        return true;
    }

    public boolean checkOrderGroupComponentDateRange(final Entity group, final Iterable<Entity> orders) {
        Date groupDateTo = (Date) group.getField("dateTo");
        Date groupDateFrom = (Date) group.getField("dateFrom");
        Long orderDateFrom = null;
        Long orderDateTo = null;

        if (groupDateTo != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) group.getField("dateTo"));
            // important! change time to 23:59:59.99
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.MILLISECOND, -1);
            groupDateTo = cal.getTime();
        }

        for (Entity order : orders) {
            orderDateFrom = ((Date) order.getField("dateFrom")).getTime();
            orderDateTo = ((Date) order.getField("dateTo")).getTime();

            if (groupDateFrom != null && groupDateFrom.getTime() > orderDateFrom) {
                return false;
            }

            if (groupDateTo != null && groupDateTo.getTime() < orderDateTo) {
                return false;
            }
        }
        return true;
    }

    /* ****** MODEL HOOKS ****** */

    public void setGroupNameWhenSaveOrder(final DataDefinition dataDefinition, final Entity order) {
        Entity orderGroup = order.getBelongsToField("orderGroup");
        if (orderGroup == null) {
            return;
        }
        order.setField("orderGroupName", orderGroup.getField("name"));
    }

    public void fillOrdersGroupNameField(final DataDefinition dataDefinition, final Entity orderGroup) {
        DataDefinition ordersDataDef = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        Object groupName = orderGroup.getField("name");
        for (Entity order : orderGroup.getHasManyField("orders")) {
            order.setField("orderGroupName", groupName);
            ordersDataDef.save(order);
        }
    }

}
