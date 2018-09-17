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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import static com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState.DRAFT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordType;
import com.qcadoo.mes.advancedGenealogyForOrders.AdvancedGenealogyForOrdersService;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordFieldsAGFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TrackingForOrdersViewHooks {

    private static final String L_NUMBER = "number";

    @Autowired
    private AdvancedGenealogyForOrdersService advancedGenealogyForOrdersService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_PRODUCED_BATCH_LOOKUP = "producedBatchLookup";

    private static final String L_STATE = "state";

    private static final String L_ORDER = "order";

    private static final String L_ORDER_LOOKUP = "orderLookup";

    private static final String L_PRODUCT_BATCH_REQUIRED = "productBatchRequired";

    private static final String L_PRODUCT_IN_COMPONENT = "productInComponent";

    private static final String L_GENEALOGY_PRODUCT_IN_COMPONENTS_LIST = "genealogyProductInComponentsList";

    private static final String L_PRODUCT_INFO_LOOKUP = "productInfo";

    private static final String L_TECHNOLOGY_BATCH_REQUIRED = "technologyBatchRequired";

    private static final String L_TECHNOLOGY = "technology";

    public final void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        grid.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
                searchBuilder.add(SearchRestrictions.eq("entityType", TrackingRecordType.FOR_ORDER));
            }

        });
    }

    public final void changeProductInComponents(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        FormComponent form = getForm(view);
        FieldComponent orderLookup = getFieldComponent(view, L_ORDER_LOOKUP);
        Long trackingRecordId = form.getEntityId();
        Long newOrderId = (Long) orderLookup.getFieldValue();
        if (trackingRecordId == null || newOrderId == null) {
            return;
        }
        Entity newOrder = getOrder(newOrderId);

        if (newOrder == null) {
            return;
        }

        AwesomeDynamicListComponent genealogyProductInComponentsList = getMainAwesomeDynamicList(view);
        List<Entity> productInComponents = advancedGenealogyForOrdersService.buildProductInComponentList(newOrder);
        genealogyProductInComponentsList.setFieldValue(productInComponents);
        markRequiredProductInComponents(genealogyProductInComponentsList);

        setFilterForProducedBatchLookup(view, newOrder);

    }

    public final void markRequiredProductInComponents(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = getMainAwesomeDynamicList(view);
        markRequiredProductInComponents(adl);

    }

    private void markRequiredProductInComponents(final AwesomeDynamicListComponent adl) {
        for (FormComponent form : adl.getFormComponents()) {
            if (genealogyProductInComponentRequireBatches(form.getEntity())) {
                form.findFieldComponentByName(L_PRODUCT_INFO_LOOKUP).setRequired(true);
            }
        }
    }

    private boolean genealogyProductInComponentRequireBatches(final Entity genealogyProductInComponent) {
        Entity technologyProductInComponent = genealogyProductInComponent.getBelongsToField(L_PRODUCT_IN_COMPONENT);
        return technologyProductInComponent.getBooleanField(L_PRODUCT_BATCH_REQUIRED);
    }

    private boolean isProducedBatchRequiredForOrder(final Entity order) {
        if (order == null) {
            return false;
        }
        Entity technology = order.getBelongsToField(L_TECHNOLOGY);
        return technology.getBooleanField(L_TECHNOLOGY_BATCH_REQUIRED);
    }

    public final void assignProductInComponentValidationErrors(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = getMainAwesomeDynamicList(view);
        for (FormComponent form : adl.getFormComponents()) {
            Entity productInComponent = form.getEntity();
            if (!productInComponent.isFieldValid(L_PRODUCT_IN_COMPONENT)) {
                ErrorMessage errorMessage = productInComponent.getError(L_PRODUCT_IN_COMPONENT);
                form.findFieldComponentByName(L_PRODUCT_INFO_LOOKUP).addMessage(errorMessage.getMessage(), MessageType.FAILURE);
            }
        }
    }

    public final void setEntityTypeToForOrder(final ViewDefinitionState view) {
        FieldComponent entityType = getFieldComponent(view, "entityType");
        entityType.setFieldValue(TrackingRecordType.FOR_ORDER);
        entityType.requestComponentUpdateState();
    }

    public final void toggleComponentsVisibility(final ViewDefinitionState view) {
        List<FieldComponent> components = Lists.newArrayList();

        components.add(getFieldComponent(view, L_NUMBER));
        components.add(getFieldComponent(view, L_ORDER_LOOKUP));
        components.add(getFieldComponent(view, L_PRODUCED_BATCH_LOOKUP));
        toggleComponents(view, components);
        toggleProducedBatchRequirement(view);
    }

    public final void toggleComponentsVisibilityForOrder(final ViewDefinitionState view) {
        List<FieldComponent> components = Lists.newArrayList();

        components.add(getFieldComponent(view, L_NUMBER));
        components.add(getFieldComponent(view, L_PRODUCED_BATCH_LOOKUP));
        toggleComponents(view, components);
        toggleProducedBatchRequirement(view);
    }

    private void toggleProducedBatchRequirement(final ViewDefinitionState view) {
        FormComponent form = getForm(view);

        Entity trackingRecord = form.getEntity();

        Entity order = trackingRecord.getBelongsToField(TrackingRecordFieldsAGFO.ORDER);
        if (order != null) {
            getFieldComponent(view, L_PRODUCED_BATCH_LOOKUP).setRequired(isProducedBatchRequiredForOrder(order));
        }
    }

    private void toggleComponents(final ViewDefinitionState view, final List<FieldComponent> components) {
        FormComponent form = getForm(view);
        ComponentState usedBatchesLayout = view.getComponentByReference("usedBatchesForOrdersListBorderLayout");
        if (!form.isValid()) {
            return;
        }

        ComponentState description = view.getComponentByReference("description");

        if (form.getEntityId() == null) {
            usedBatchesLayout.setVisible(false);
            description.setVisible(true);
        } else {
            boolean isDraft = isInDraftState(form.getEntity());
            setComponents(components, isDraft);

            description.setVisible(false);
            usedBatchesLayout.setVisible(true);
        }
    }

    private void setComponents(final List<FieldComponent> components, final boolean enabledAndRequired) {
        for (FieldComponent component : components) {
            component.setEnabled(enabledAndRequired);
            component.setRequired(enabledAndRequired);
        }
    }

    private boolean isInDraftState(final Entity trackingRecord) {
        return DRAFT.getStringValue().equals(trackingRecord.getStringField(L_STATE));
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }

    private AwesomeDynamicListComponent getMainAwesomeDynamicList(final ViewDefinitionState view) {
        return (AwesomeDynamicListComponent) view.getComponentByReference(L_GENEALOGY_PRODUCT_IN_COMPONENTS_LIST);
    }

    public final void setOrderState(final ViewDefinitionState view) {
        FieldComponent orderLookup = (FieldComponent) view.getComponentByReference(L_ORDER_LOOKUP);
        if (orderLookup.getFieldValue() == null) {
            return;
        }

        Entity order = getOrder((Long) orderLookup.getFieldValue());

        if (order == null) {
            return;
        }
        String orderState = order.getStringField(L_STATE);
        FieldComponent stateField = getFieldComponent(view, "orderState");
        stateField.setFieldValue(orderState);
    }

    public final void setOrderTreatment(final ViewDefinitionState view) {
        FieldComponent orderLookup = (FieldComponent) view.getComponentByReference(L_ORDER_LOOKUP);
        if (orderLookup.getFieldValue() == null) {
            return;
        }
        Entity order = getOrder((Long) orderLookup.getFieldValue());

        if (order == null) {
            return;
        }

        String orderTreatment = order.getStringField("trackingRecordTreatment");

        FieldComponent stateField = (FieldComponent) view.getComponentByReference("orderTreatment");
        stateField.setFieldValue(orderTreatment);
    }

    private Entity getOrder(final Long id) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
    }

    private void setFilterForProducedBatchLookup(final ViewDefinitionState view, final Entity order) {

        if (order != null) {
            LookupComponent producedBatchLookup = (LookupComponent) view.getComponentByReference(L_PRODUCED_BATCH_LOOKUP);
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);
            FilterValueHolder filter = producedBatchLookup.getFilterValue();
            filter.put("productForBatch", product.getId());
            producedBatchLookup.setFilterValue(filter);
        }
    }

    private void setFilterForInputProductBatchLookups(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view
                .getComponentByReference(L_GENEALOGY_PRODUCT_IN_COMPONENTS_LIST);

        for (FormComponent innerForm : adl.getFormComponents()) {
            AwesomeDynamicListComponent innerAdl = (AwesomeDynamicListComponent) innerForm
                    .findFieldComponentByName("productInBatches");
            Entity productInComponent = innerForm.getPersistedEntityWithIncludedFormValues().getBelongsToField(
                    "productInComponent");
            Entity innerProduct = productInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            for (FormComponent batchForm : innerAdl.getFormComponents()) {
                LookupComponent batchLookup = (LookupComponent) batchForm.findFieldComponentByName("inputProductBatchLookup");
                FilterValueHolder innerFilter = batchLookup.getFilterValue();
                innerFilter.put("productForBatch", innerProduct.getId());
                batchLookup.setFilterValue(innerFilter);

            }

        }
    }

    public final void setCriteriaModifiersParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity trackingRecord = form.getPersistedEntityWithIncludedFormValues();
        Entity order = trackingRecord.getBelongsToField(L_ORDER);
        setFilterForProducedBatchLookup(view, order);
        setFilterForInputProductBatchLookups(view);
    }

    public final void setOrderFieldRequired(final ViewDefinitionState view) {

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(L_ORDER_LOOKUP);
        orderLookup.setRequired(true);
    }
}
