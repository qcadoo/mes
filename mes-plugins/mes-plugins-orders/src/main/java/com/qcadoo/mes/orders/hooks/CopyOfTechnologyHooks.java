/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.criteriaModifires.TechnologyCriteriaModifires;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.hooks.TechnologyDetailsViewHooks;
import com.qcadoo.mes.technologies.listeners.TechnologyDetailsListeners;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class CopyOfTechnologyHooks {

    private static final String L_WINDOW = "window";

    private static final String L_FORM = "form";

    private static final String L_CLEAR_AND_LOAD_PATTERN_TECHNOLOGY = "clearAndLoadPatternTechnology";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_STATUS = "status";

    private static final String L_CLEAR_TECHNOLOGY = "clearTechnology";

    private static final String L_CHECK_TECHNOLOGY = "checkTechnology";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyDetailsViewHooks technologyDetailsViewHooks;

    @Autowired
    private TechnologyDetailsListeners technologyDetailsListeners;

    @Autowired
    private ParameterService parameterService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        final FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (technologyId == null) {
            return;
        }

        Entity technology = technologyForm.getEntity().getDataDefinition().get(technologyId);

        Entity order = getOrderForTechnology(technology);

        String orderType = order.getStringField(OrderFields.ORDER_TYPE);

        enableFileds(view, orderType);
        disableRibbonItem(view, orderType, order);
        setVisibleFileds(view, orderType);
        setCriteriaModifierParameters(view, order);
        technologyDetailsViewHooks.filterStateChangeHistory(view);
        technologyDetailsViewHooks.setTreeTabEditable(view);
        technologyDetailsListeners.setGridEditable(view);
        disableForm(view, order, technology);
    }

    private Entity getOrderForTechnology(Entity technology) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).setMaxResults(1).uniqueResult();
    }

    private void disableForm(final ViewDefinitionState view, final Entity order, final Entity technology) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);
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

            if (TechnologyState.CHECKED.getStringValue().equals(technolgyState)) {
                isDisabled = true;
            }
        }

        technologyForm.setFormEnabled(!isDisabled);

        if (lockTechnologyTree()) {
            technologyDetailsViewHooks.setTreeTabEditable(view, false);
        } else {
            technologyDetailsViewHooks.setTreeTabEditable(view, !isDisabled);
        }
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity order) {
        LookupComponent patternTechnologyLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFields.TECHNOLOGY_PROTOTYPE);

        FilterValueHolder holder = patternTechnologyLookup.getFilterValue();

        holder.put(TechnologyCriteriaModifires.PRODUCT_PARAMETER, order.getBelongsToField(OrderFields.PRODUCT).getId());

        patternTechnologyLookup.setFilterValue(holder);
    }

    private void disableRibbonItem(final ViewDefinitionState view, final String orderType, final Entity order) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup technology = ribbon.getGroupByName(L_TECHNOLOGY);
        RibbonGroup status = ribbon.getGroupByName(L_STATUS);

        RibbonActionItem clearAndLoadPatternTechnology = technology.getItemByName(L_CLEAR_AND_LOAD_PATTERN_TECHNOLOGY);
        RibbonActionItem clearTechnology = technology.getItemByName(L_CLEAR_TECHNOLOGY);
        RibbonActionItem checkTechnology = status.getItemByName(L_CHECK_TECHNOLOGY);

        if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(orderType)) {
            clearAndLoadPatternTechnology.setEnabled(false);
            clearAndLoadPatternTechnology.requestUpdate(true);
        }
        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            clearTechnology.setEnabled(false);
            clearTechnology.requestUpdate(true);
        }

        String state = order.getStringField(OrderFields.STATE);

        if (!OrderState.PENDING.getStringValue().equals(state)) {
            clearAndLoadPatternTechnology.setEnabled(false);
            clearAndLoadPatternTechnology.requestUpdate(true);

            clearTechnology.setEnabled(false);
            clearTechnology.requestUpdate(true);

            checkTechnology.setEnabled(false);
            checkTechnology.requestUpdate(true);
        }
    }

    private void enableFileds(final ViewDefinitionState view, final String orderType) {
        LookupComponent technologyPrototypeLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFields.TECHNOLOGY_PROTOTYPE);

        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            technologyPrototypeLookup.setEnabled(true);
        }
    }

    private void setVisibleFileds(final ViewDefinitionState view, final String orderType) {
        LookupComponent patternTechnologyLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFields.TECHNOLOGY_PROTOTYPE);

        if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(orderType)) {
            patternTechnologyLookup.setVisible(false);
        }
    }

    public boolean lockTechnologyTree() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsO.LOCK_TECHNOLOGY_TREE);
    }

}
