/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.hooks;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.ReleaseOfMaterials;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.productionCounting.listeners.ProductionTrackingDetailsListeners;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionTrackingDetailsHooks {

    private static final String L_STATE = "state";

    private static final String L_IS_DISABLED = "isDisabled";

    private static final String L_ACTIONS = "actions";

    private static final String L_PRODUCTS_QUANTITIES = "productsQuantities";

    private static final String L_PRODUCTION_COUNTING_QUANTITIES = "productionCountingQuantities";

    private static final String L_ANOMALIES = "anomalies";

    private static final String L_COPY = "copy";

    private static final String L_COPY_PLANNED_QUANTITY_TO_USED_QUANTITY = "copyPlannedQuantityToUsedQuantity";

    private static final String L_ADD_TO_ANOMALIES_LIST = "addToAnomaliesList";

    private static final String L_CORRECT = "correct";

    private static final String L_CORRECTION = "correction";

    private static final String L_CORRECTS = "corrects";

    private static final String L_PRODUCTS_TAB = "productsTab";

    private static final String L_ORDER_ID = "orderId";

    private static final String L_ORDER = "order";

    private static final String L_BATCH_ORDERED_PRODUCT_LABEL = "batchOrderedProductLabel";

    private static final String L_STOPPAGES_TAB = "stoppagesTab";

    private static final List<String> L_PRODUCTION_TRACKING_FIELD_NAMES = Lists.newArrayList(ProductionTrackingFields.ORDER,
            ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, ProductionTrackingFields.STAFF,
            ProductionTrackingFields.SHIFT, ProductionTrackingFields.WORKSTATION, ProductionTrackingFields.DIVISION,
            ProductionTrackingFields.LABOR_TIME, ProductionTrackingFields.MACHINE_TIME,
            ProductionTrackingFields.TIME_RANGE_FROM,
            ProductionTrackingFields.TIME_RANGE_TO, ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS,
            ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS, ProductionTrackingFields.SHIFT_START_DAY,
            ProductionTrackingFields.STAFF_WORK_TIMES, ProductionTrackingFields.BATCH, ProductionTrackingFields.EXPIRATION_DATE,
            ProductionTrackingFields.ADD_BATCH, ProductionTrackingFields.STOPPAGES, ProductionTrackingFields.COMMENTS);

    public static final String L_ROLE_STOPPAGES = "ROLE_STOPPAGES";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private ProductionTrackingDetailsListeners productionTrackingDetailsListeners;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        setCriteriaModifierParameters(view);

        productionTrackingService.fillProductionLineLookup(view);

        if ((view.isViewAfterRedirect() || view.isViewAfterReload())
                && !((CheckBoxComponent) view.getComponentByReference(ProductionTrackingFields.ADD_BATCH)).isChecked()) {
            FieldComponent batchNumber = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.BATCH_NUMBER);
            batchNumber.setEnabled(false);
        }

        if (Objects.isNull(productionTrackingForm.getEntityId())) {
            setStateFieldValueToDraft(view);
        } else {
            Entity productionTracking = getProductionTrackingFromDB(productionTrackingForm.getEntityId());

            initializeProductionTrackingDetailsView(view);
            showLastStateChangeFailNotification(productionTrackingForm, productionTracking);
            changeFieldComponentsEnabledAndGridsEditable(view);
            updateRibbonState(view);
            toggleCorrectButton(view, productionTracking);
            toggleCorrectionFields(view, productionTracking);
            fetchNumberFromDatabase(view, productionTracking);
        }

        fillBatchOrderedProductLabel(view);
        changeStoppagesTabVisible(view);
        fillPieceworkProduction(view);
    }

    private void fillPieceworkProduction(ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent pieceworkProduction = (CheckBoxComponent) view.getComponentByReference(ProductionTrackingFields.PIECEWORK_PRODUCTION);
        Entity productionTracking = productionTrackingForm.getPersistedEntityWithIncludedFormValues();

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        if (Objects.nonNull(order)) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (Objects.nonNull(technology)) {
                pieceworkProduction.setFieldValue(technology.getBooleanField(TechnologyFieldsPC.PIECEWORK_PRODUCTION));
            }

            Entity technologyOperationComponent = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
            if (Objects.nonNull(technologyOperationComponent)) {
                pieceworkProduction.setFieldValue(technologyOperationComponent.getBooleanField(TechnologyOperationComponentFieldsTNFO.PIECEWORK_PRODUCTION));
            }
        } else {
            pieceworkProduction.setFieldValue(false);
        }
    }

    private void fillBatchOrderedProductLabel(final ViewDefinitionState view) {
        LookupComponent orderLookupComponent = (LookupComponent) view.getComponentByReference(L_ORDER);

        if (!orderLookupComponent.isEmpty()) {
            Entity order = orderLookupComponent.getEntity();

            if (Objects.nonNull(order)) {
                FieldComponent batchOrderedProductLabel = (FieldComponent) view
                        .getComponentByReference(L_BATCH_ORDERED_PRODUCT_LABEL);

                batchOrderedProductLabel.setFieldValue(translationService.translate(
                        "productionCounting.productionTrackingDetails.window.batchOrderedProduct.batchOrderedProductLabel.label",
                        LocaleContextHolder.getLocale()) + " "
                        + order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.NUMBER));
                batchOrderedProductLabel.requestComponentUpdateState();
            }
        }
    }

    private void changeStoppagesTabVisible(final ViewDefinitionState view) {
        Entity user = userService.getCurrentUserEntity();

        boolean isVisible = securityService.hasRole(user, L_ROLE_STOPPAGES);

        view.getComponentByReference(L_STOPPAGES_TAB).setVisible(isVisible);
    }

    private void fetchNumberFromDatabase(final ViewDefinitionState view, final Entity productionTracking) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.NUMBER);

        if (Strings.isNullOrEmpty((String) numberField.getFieldValue())) {
            numberField.setFieldValue(productionTracking.getStringField(ProductionTrackingFields.NUMBER));
        }
    }

    private void toggleCorrectButton(final ViewDefinitionState view, final Entity entity) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem correctButton = window.getRibbon().getGroupByName(L_CORRECTION).getItemByName(L_CORRECT);

        String state = entity.getStringField(ProductionTrackingFields.STATE);
        Entity order = entity.getBelongsToField(ProductionTrackingFields.ORDER);
        String orderState = order.getStringField(OrderFields.STATE);

        boolean productionTrackingIsAccepted = ProductionTrackingStateStringValues.ACCEPTED.equals(state);
        boolean orderIsNotFinished = !OrderStateStringValues.COMPLETED.equals(orderState)
                && !OrderStateStringValues.ABANDONED.equals(orderState);

        correctButton.setEnabled(productionTrackingIsAccepted && orderIsNotFinished);

        correctButton.requestUpdate(true);
    }

    private void toggleCorrectionFields(final ViewDefinitionState view, final Entity entity) {
        Entity correctedProductionTracking = getCorrectedProductionTracking(entity);

        if (Objects.nonNull(correctedProductionTracking)) {
            view.getComponentByReference(ProductionTrackingFields.ORDER).setEnabled(false);
            view.getComponentByReference(OrderFields.PRODUCTION_LINE).setEnabled(false);
            view.getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT).setEnabled(false);

            view.getComponentByReference(L_CORRECTS).setVisible(true);
            view.getComponentByReference(L_CORRECTS)
                    .setFieldValue(correctedProductionTracking.getStringField(ProductionTrackingFields.NUMBER));
        }
    }

    private Entity getCorrectedProductionTracking(Entity entity) {
        return entity.getDataDefinition().find().add(SearchRestrictions.belongsTo(ProductionTrackingFields.CORRECTION, entity))
                .uniqueResult();
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity productionTracking = productionTrackingForm.getEntity();

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        if (Objects.nonNull(order)) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (Objects.nonNull(technology)) {
                FilterValueHolder filterValueHolder = technologyOperationComponentLookup.getFilterValue();
                filterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                technologyOperationComponentLookup.setFilterValue(filterValueHolder);
            }
        }

        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.BATCH);
        FilterValueHolder batchFilterValueHolder = batchLookup.getFilterValue();

        if (Objects.isNull(order)) {
            if (batchFilterValueHolder.has(L_ORDER_ID)) {
                batchFilterValueHolder.remove(L_ORDER_ID);
            }
        } else {
            batchFilterValueHolder.put(L_ORDER_ID, order.getId());
        }

        batchLookup.setFilterValue(batchFilterValueHolder);
    }

    private void setStateFieldValueToDraft(final ViewDefinitionState view) {
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(L_STATE);

        stateField.setFieldValue(ProductionTrackingState.DRAFT.getStringValue());
        stateField.requestComponentUpdateState();
    }

    public void initializeProductionTrackingDetailsView(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.STATE);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.ORDER);
        FieldComponent isDisabledField = (FieldComponent) view.getComponentByReference(L_IS_DISABLED);

        Entity productionTracking = productionTrackingForm.getEntity();

        stateField.setFieldValue(productionTracking.getField(ProductionTrackingFields.STATE));
        stateField.requestComponentUpdateState();

        Entity order = orderLookup.getEntity();

        isDisabledField.setFieldValue(false);

        if (Objects.nonNull(order)) {
            changeProductsTabVisible(view, productionTracking, order);

            productionTrackingService.setTimeAndPieceworkComponentsVisible(view, order);
        }
    }

    private void changeProductsTabVisible(final ViewDefinitionState view, final Entity productionTracking, final Entity order) {
        view.getComponentByReference(L_PRODUCTS_TAB).setVisible(checkIfShouldProductTabBeVisible(productionTracking, order));
    }

    public boolean checkIfShouldProductTabBeVisible(final Entity productionTracking, final Entity order) {
        if (Objects.isNull(productionTracking)) {
            return false;
        }

        if (Objects.isNull(order)) {
            return false;
        }

        boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        return (registerQuantityInProduct || registerQuantityOutProduct);
    }

    private void showLastStateChangeFailNotification(final FormComponent productionTrackingForm,
                                                     final Entity productionTracking) {
        boolean lastStateChangeFails = productionTracking.getBooleanField(ProductionTrackingFields.LAST_STATE_CHANGE_FAILS);

        if (lastStateChangeFails) {
            String lastStateChangeFailCause = productionTracking
                    .getStringField(ProductionTrackingFields.LAST_STATE_CHANGE_FAIL_CAUSE);

            if (StringUtils.isEmpty(lastStateChangeFailCause)) {
                productionTrackingForm.addMessage("productionCounting.productionTracking.info.lastStateChangeFails",
                        ComponentState.MessageType.INFO, true, lastStateChangeFailCause);
            } else {
                productionTrackingForm.addMessage("productionCounting.productionTracking.info.lastStateChangeFails.withCause",
                        ComponentState.MessageType.INFO, false, lastStateChangeFailCause);
            }
        }
    }

    public void changeFieldComponentsEnabledAndGridsEditable(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(productionTrackingForm.getEntityId())) {
            return;
        }

        Entity productionTracking = productionTrackingForm.getEntity();

        String state = productionTracking.getStringField(ProductionTrackingFields.STATE);

        boolean isDraft = (ProductionTrackingStateStringValues.DRAFT.equals(state));
        boolean isExternalSynchronized = productionTracking.getBooleanField(ProductionTrackingFields.IS_EXTERNAL_SYNCHRONIZED);

        setFieldComponentsEnabledAndGridsEditable(view, isDraft && isExternalSynchronized);

        productionTrackingDetailsListeners.checkJustOne(view, null, null);
    }

    private void setFieldComponentsEnabledAndGridsEditable(final ViewDefinitionState view, final boolean isEnabled) {
        productionCountingService.setComponentsState(view, L_PRODUCTION_TRACKING_FIELD_NAMES, isEnabled, true);

        GridComponent trackingOperationProductInComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        GridComponent trackingOperationProductOutComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        GridComponent stateChangesGrid = (GridComponent) view.getComponentByReference(ProductionTrackingFields.STATE_CHANGES);

        String releaseOfMaterials = parameterService.getParameter().getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS);
        if (ReleaseOfMaterials.MANUALLY_TO_ORDER_OR_GROUP.getStringValue().equals(releaseOfMaterials)) {
            trackingOperationProductInComponentsGrid.setEnabled(false);
        } else {
            trackingOperationProductInComponentsGrid.setEnabled(isEnabled);
        }
        trackingOperationProductOutComponentsGrid.setEnabled(isEnabled);

        stateChangesGrid.setEditable(isEnabled);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup actionsRibbonGroup = window.getRibbon().getGroupByName(L_ACTIONS);
        RibbonGroup productsQuantitiesRibbonGroup = window.getRibbon().getGroupByName(L_PRODUCTS_QUANTITIES);
        RibbonGroup productionCountingQuantitiesRibbonGroup = window.getRibbon().getGroupByName(L_PRODUCTION_COUNTING_QUANTITIES);
        RibbonGroup anomaliesRibbonGroup = window.getRibbon().getGroupByName(L_ANOMALIES);

        RibbonActionItem copyRibbonActionItem = actionsRibbonGroup.getItemByName(L_COPY);

        RibbonActionItem copyPlannedQuantityToUsedQuantityRibbonActionItem = productsQuantitiesRibbonGroup
                .getItemByName(L_COPY_PLANNED_QUANTITY_TO_USED_QUANTITY);
        RibbonActionItem productionCountingQuantitiesRibbonActionItem = productionCountingQuantitiesRibbonGroup
                .getItemByName(L_PRODUCTION_COUNTING_QUANTITIES);
        RibbonActionItem addToAnomaliesListRibbonActionItem = anomaliesRibbonGroup.getItemByName(L_ADD_TO_ANOMALIES_LIST);

        if (Objects.isNull(productionTrackingForm.getEntityId())) {
            return;
        }

        Entity productionTracking = productionTrackingForm.getEntity();
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        if (Objects.isNull(order)) {
            return;
        }

        String state = productionTracking.getStringField(ProductionTrackingFields.STATE);
        String orderState = order.getStringField(OrderFields.STATE);

        boolean isInProgress = OrderStateStringValues.IN_PROGRESS.equals(orderState);
        boolean isDraft = ProductionTrackingStateStringValues.DRAFT.equals(state);
        boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        copyRibbonActionItem.setEnabled(isInProgress);

        String releaseOfMaterials = parameterService.getParameter().getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS);
        if (ReleaseOfMaterials.MANUALLY_TO_ORDER_OR_GROUP.getStringValue().equals(releaseOfMaterials)) {
            copyPlannedQuantityToUsedQuantityRibbonActionItem.setEnabled(false);
        } else {
            copyPlannedQuantityToUsedQuantityRibbonActionItem
                    .setEnabled(isDraft && (registerQuantityInProduct || registerQuantityOutProduct));

        }

        productionCountingQuantitiesRibbonActionItem
                .setEnabled(isDraft && (registerQuantityInProduct || registerQuantityOutProduct));
        addToAnomaliesListRibbonActionItem.setEnabled(isDraft && (registerQuantityInProduct || registerQuantityOutProduct));

        copyRibbonActionItem.requestUpdate(true);
        copyPlannedQuantityToUsedQuantityRibbonActionItem.requestUpdate(true);
        productionCountingQuantitiesRibbonActionItem.requestUpdate(true);
        addToAnomaliesListRibbonActionItem.requestUpdate(true);
    }

    private Entity getProductionTrackingFromDB(final Long productionTrackingId) {
        return dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING)
                .get(productionTrackingId);
    }

}
