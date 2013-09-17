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
package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_FORM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.criteriaModifires.TechnologyCriteriaModifires;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyType;
import com.qcadoo.mes.technologies.hooks.TechnologyDetailsViewHooks;
import com.qcadoo.mes.technologies.listeners.TechnologyDetailsListeners;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class CopyOfTechnologyHooks {

    public static final String LOCK_TECHNOLOGY_TREE = "lockTechnologyTree";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyDetailsViewHooks technologyDetailsViewHooks;

    @Autowired
    private TechnologyDetailsListeners technologyDetailsListeners;

    @Autowired
    private ParameterService parameterService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(form.getEntityId());

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).uniqueResult();

        String orderType = order.getStringField(OrderFields.ORDER_TYPE);
        fillFileds(view, technology);
        enableFileds(view, orderType);
        disableRibbonItem(view, orderType, order);
        setVisibleFileds(view, orderType);
        setCriteriaModifierParameters(view, order);
        technologyDetailsViewHooks.filterStateChangeHistory(view);
        technologyDetailsViewHooks.setTreeTabEditable(view);
        technologyDetailsListeners.setGridEditable(view);
        disableForm(view, order, technology);
    }

    private void disableForm(final ViewDefinitionState view, final Entity order, final Entity technologyEntity) {

        FormComponent technology = (FormComponent) view.getComponentByReference(FIELD_FORM);
        boolean disabled = true;

        if (technology.getEntityId() != null) {
            if (order == null) {
                return;
            }
            String state = order.getStringField(STATE);
            String technolgyState = technologyEntity.getStringField(STATE);
            if (OrderState.PENDING.getStringValue().equals(state)) {
                disabled = false;
            }
            if (TechnologyState.CHECKED.getStringValue().equals(technolgyState)) {
                disabled = true;
            }
        }

        technology.setFormEnabled(!disabled);
        if (lockTechnologyTree()) {
            technologyDetailsViewHooks.setTreeTabEditable(view, false);
        } else {
            technologyDetailsViewHooks.setTreeTabEditable(view, !disabled);
        }
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity order) {
        LookupComponent patternTechnologyLookup = (LookupComponent) view.getComponentByReference("technologyPrototype");
        FilterValueHolder holder = patternTechnologyLookup.getFilterValue();
        holder.put(TechnologyCriteriaModifires.PRODUCT_PARAMETER, order.getBelongsToField(OrderFields.PRODUCT).getId());
        patternTechnologyLookup.setFilterValue(holder);

    }

    private void disableRibbonItem(final ViewDefinitionState view, final String orderType, final Entity order) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonActionItem clearAndLoadPatternTechnology = ribbon.getGroupByName("technology").getItemByName(
                "clearAndLoadPatternTechnology");
        RibbonActionItem clearTechnology = ribbon.getGroupByName("technology").getItemByName("clearTechnology");
        RibbonActionItem checkTechnology = ribbon.getGroupByName("status").getItemByName("checkTechnology");

        if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(orderType)) {
            clearAndLoadPatternTechnology.setEnabled(false);
            clearAndLoadPatternTechnology.requestUpdate(true);
        }
        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            clearTechnology.setEnabled(false);
            clearTechnology.requestUpdate(true);
        }
        String state = order.getStringField(STATE);
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
        LookupComponent patternTechnologyLookup = (LookupComponent) view.getComponentByReference("technologyPrototype");
        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            patternTechnologyLookup.setEnabled(true);

        }
        FieldComponent field = (FieldComponent) view.getComponentByReference("typeOfTechnology");
        field.setEnabled(false);
        field.requestComponentUpdateState();

    }

    private void setVisibleFileds(final ViewDefinitionState view, final String orderType) {
        LookupComponent patternTechnologyLookup = (LookupComponent) view.getComponentByReference("technologyPrototype");
        if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(orderType)) {
            patternTechnologyLookup.setVisible(false);

        }

    }

    private void fillFileds(final ViewDefinitionState view, final Entity technology) {

        FieldComponent field = (FieldComponent) view.getComponentByReference("typeOfTechnology");
        if (TechnologyType.WITH_OWN_TECHNOLOGY.getStringValue().equals(technology.getField(TechnologyFields.TECHNOLOGY_TYPE))) {
            field.setFieldValue(translationService.translate("orders.technology.own", view.getLocale()));
        } else {
            field.setFieldValue(translationService.translate("orders.technology.pattern", view.getLocale()));
        }

        field.requestComponentUpdateState();
    }

    public boolean lockTechnologyTree() {
        return parameterService.getParameter().getBooleanField(LOCK_TECHNOLOGY_TREE);
    }
}
