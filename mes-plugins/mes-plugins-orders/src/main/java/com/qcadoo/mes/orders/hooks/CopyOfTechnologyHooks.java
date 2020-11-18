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
package com.qcadoo.mes.orders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.hooks.TechnologyDetailsHooks;
import com.qcadoo.mes.technologies.listeners.TechnologyDetailsListeners;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopyOfTechnologyHooks {

    private static final String L_CLEAR_AND_LOAD_PATTERN_TECHNOLOGY = "clearAndLoadPatternTechnology";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_STATUS = "status";

    private static final String L_CHECK_TECHNOLOGY = "checkTechnology";

    private static final List<String> STATES_TO_EDIT_GROUP = Lists.newArrayList(OrderState.IN_PROGRESS.getStringValue(),
            OrderState.ACCEPTED.getStringValue(), OrderState.PENDING.getStringValue(), OrderState.INTERRUPTED.getStringValue());

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyDetailsHooks technologyDetailsHooks;

    @Autowired
    private TechnologyDetailsListeners technologyDetailsListeners;

    @Autowired
    private ParameterService parameterService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        final FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (technologyId == null) {
            return;
        }

        Entity technology = technologyForm.getEntity().getDataDefinition().get(technologyId);
        Entity order = getOrderForTechnology(view);

        disableRibbonItem(view, order);
        setCriteriaModifierParameters(view, order);
        technologyDetailsHooks.filterStateChangeHistory(view);
        technologyDetailsHooks.setTreeTabEditable(view);
        technologyDetailsListeners.setGridEditable(view);
        disableForm(view, order, technology);
        enableGroupField(view, order);
    }

    public Entity getOrderForTechnology(final ViewDefinitionState state) {
        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        String orderId = null;
       try {
           orderId = state.getJsonContext().getString("window.mainTab.technology.orderId");
        } catch (JSONException ex) {
           EntityList entities = ((FormComponent) state.getComponentByReference(QcadooViewConstants.L_FORM)).getPersistedEntityWithIncludedFormValues().getHasManyField("orders");
           if(!entities.isEmpty()){
               orderId = String.valueOf(entities.get(0).getId());
           }
        }
        return orderDD.get(Long.valueOf(orderId));
    }

    private void disableForm(final ViewDefinitionState view, final Entity order, final Entity technology) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        boolean isDisabled = true;

        if (technologyForm.getEntityId() != null) {
            if (order == null) {
                return;
            }
            String technolgyState = technology.getStringField(TechnologyFields.STATE);
            String orderState = order.getStringField(OrderFields.STATE);

            if (OrderState.PENDING.getStringValue().equals(orderState)) {
                isDisabled = false;
            }

            if (TechnologyState.CHECKED.getStringValue().equals(technolgyState) || TechnologyState.ACCEPTED.getStringValue().equals(technolgyState)) {
                isDisabled = true;
            }
        }

        technologyForm.setFormEnabled(!isDisabled);

        if (allowTechnologyTreeChangeInPendingOrder()) {
            technologyDetailsHooks.setTreeTabEditable(view, !isDisabled);
        } else {
            technologyDetailsHooks.setTreeTabEditable(view, false);
        }
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity order) {
        LookupComponent patternTechnologyLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFields.TECHNOLOGY_PROTOTYPE);

        FilterValueHolder holder = patternTechnologyLookup.getFilterValue();

        holder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, order.getBelongsToField(OrderFields.PRODUCT).getId());

        patternTechnologyLookup.setFilterValue(holder);
    }

    private void disableRibbonItem(final ViewDefinitionState view, final Entity order) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup technology = ribbon.getGroupByName(L_TECHNOLOGY);
        RibbonGroup status = ribbon.getGroupByName(L_STATUS);

        RibbonActionItem clearAndLoadPatternTechnology = technology.getItemByName(L_CLEAR_AND_LOAD_PATTERN_TECHNOLOGY);
        RibbonActionItem checkTechnology = status.getItemByName(L_CHECK_TECHNOLOGY);

        String state = order.getStringField(OrderFields.STATE);

        if (!OrderState.PENDING.getStringValue().equals(state)) {
            clearAndLoadPatternTechnology.setEnabled(false);
            clearAndLoadPatternTechnology.requestUpdate(true);

            checkTechnology.setEnabled(false);
            checkTechnology.requestUpdate(true);
        }
    }

    private boolean allowTechnologyTreeChangeInPendingOrder() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsO.ALLOW_TECHNOLOGY_TREE_CHANGE_INPENDING_ORDER);
    }

    private void enableGroupField(final ViewDefinitionState view, final Entity order) {
        String orderState = order.getStringField(OrderFields.STATE);

        if (STATES_TO_EDIT_GROUP.contains(orderState)) {
            LookupComponent technologyGroup = (LookupComponent) view.getComponentByReference(TechnologyFields.TECHNOLOGY_GROUP);
            technologyGroup.setEnabled(true);
            technologyGroup.requestComponentUpdateState();
        }
    }

}
