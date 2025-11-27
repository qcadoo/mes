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
package com.qcadoo.mes.deliveries.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.deliveries.roles.DeliveryRole;
import com.qcadoo.mes.deliveries.states.constants.DeliveryState;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import com.qcadoo.view.constants.RowStyle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeliveryDetailsHooks {

    private static final String L_LOGGINGS_GRID = "loggingsGrid";

    private static final String L_RELATED_DELIVERY = "relatedDelivery";

    private static final String L_CREATE_RELATED_DELIVERY = "createRelatedDelivery";

    private static final String L_SHOW_RELATED_DELIVERIES = "showRelatedDeliveries";

    private static final String L_COPY_ORDERED_PRODUCTS_TO_DELIVERY = "copyOrderedProductsToDelivered";

    private static final String L_COPY_PRODUCTS_WITHOUT_QUANTITY = "copyProductsWithoutQuantityAndPrice";

    private static final String L_COPY_PRODUCTS_WITH_QUANTITY = "copyProductsWithQuantityAndPrice";

    private static final String L_DELIVERY_POSITIONS = "deliveryPositions";

    private static final String L_CHANGE_STORAGE_LOCATIONS = "changeStorageLocations";

    private static final String L_ASSIGN_STORAGE_LOCATIONS = "assignStorageLocations";

    private static final String L_DELIVERED_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY = "deliveredProductsCumulatedTotalPriceCurrency";

    private static final String L_ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY = "orderedProductsCumulatedTotalPriceCurrency";

    private static final String L_PRODUCT_NUMBER = "productNumber";

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    public void onBeforeRender(final ViewDefinitionState view) {
        orderGridByProductNumber(view);
        updateChangeStorageLocationButton(view);
        updateAssignStorageLocationsButton(view);
        generateDeliveryNumber(view);
        fillCompanyFieldsForSupplier(view);
        fillDeliveryAddressDefaultValue(view);
        fillDescriptionDefaultValue(view);
        changeFieldsEnabledDependOnState(view);
        updateRelatedDeliveryButtonsState(view);
        filterStateChangeHistory(view);
        fillCurrencyFields(view);
        togglePriceFields(view);
        disableShowProductButton(view);
        fillLocationDefaultValue(view);
        changeLocationEnabledDependOnState(view);
        updateCopyOrderedProductButtonsState(view);
        processRoles(view);
        setDeliveryIdForMultiUploadField(view);
        togglePaymentTab(view);
    }

    private void togglePaymentTab(ViewDefinitionState view) {
        boolean hasCurrentUserRole = securityService.hasCurrentUserRole("ROLE_RELEASE_FOR_PAYMENT");
        ComponentState paymentTab = view.getComponentByReference("paymentTab");
        if (hasCurrentUserRole) {
            paymentTab.setVisible(true);
            CheckBoxComponent releasedForPayment = (CheckBoxComponent) view
                    .getComponentByReference(DeliveryFields.RELEASED_FOR_PAYMENT);
            CheckBoxComponent paid = (CheckBoxComponent) view
                    .getComponentByReference(DeliveryFields.PAID);
            FieldComponent paymentDate = (FieldComponent) view.getComponentByReference(DeliveryFields.PAYMENT_DATE);
            FieldComponent paymentID = (FieldComponent) view.getComponentByReference(DeliveryFields.PAYMENT_ID);
            if (releasedForPayment.isChecked()) {
                paymentID.setEnabled(true);
                paid.setEnabled(true);
                paymentDate.setEnabled(true);
            } else {
                paymentID.setFieldValue(null);
                paymentID.setEnabled(false);
                paid.setEnabled(false);
                paymentDate.setEnabled(false);
            }
            FieldComponent stateField = (FieldComponent) view.getComponentByReference(DeliveryFields.STATE);

            String state = stateField.getFieldValue().toString();
            releasedForPayment.setEnabled(DeliveryState.RECEIVED.getStringValue().equals(state) || DeliveryState.ACCEPTED.getStringValue().equals(state));
        } else {
            paymentTab.setVisible(false);
        }
    }

    private void generateDeliveryNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY, QcadooViewConstants.L_FORM, DeliveryFields.NUMBER);
    }

    private void fillCompanyFieldsForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view.getComponentByReference(DeliveryFields.DELIVERY_DATE_BUFFER);
        FieldComponent contractorCategoryField = (FieldComponent) view.getComponentByReference(DeliveryFields.CONTRACTOR_CATEGORY);

        Entity supplier = supplierLookup.getEntity();

        if (Objects.isNull(supplier)) {
            deliveryDateBufferField.setFieldValue(null);
            contractorCategoryField.setFieldValue(null);
        } else {
            deliveryDateBufferField.setFieldValue(supplier.getIntegerField(CompanyFieldsD.BUFFER));
            contractorCategoryField.setFieldValue(supplier.getStringField(CompanyFields.CONTRACTOR_CATEGORY));
        }

        contractorCategoryField.requestComponentUpdateState();
        deliveryDateBufferField.requestComponentUpdateState();
    }

    public void changeFieldsEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(DeliveryFields.STATE);

        String state = stateField.getFieldValue().toString();

        if (Objects.isNull(deliveryForm.getEntityId())) {
            changeFieldsEnabled(view, true, false, false, false);
        } else {
            if (DeliveryState.PREPARED.getStringValue().equals(state) || DeliveryState.APPROVED.getStringValue().equals(state)
                    || DeliveryState.ACCEPTED.getStringValue().equals(state)) {
                changeFieldsEnabled(view, false, false, true, true);
            } else if (DeliveryState.DECLINED.getStringValue().equals(state) || DeliveryState.RECEIVED.getStringValue().equals(state)) {
                changeFieldsEnabled(view, false, false, false, false);
            } else {
                changeFieldsEnabled(view, true, true, true, true);
            }
        }
    }

    private void changeFieldsEnabled(final ViewDefinitionState view, final boolean enabledForm, final boolean enabledOrderedGrid, final boolean enabledDeliveredGrid, final boolean enablePackagesGrid) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);
        GridComponent deliveredPackagesGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PACKAGES);

        deliveryForm.setFormEnabled(enabledForm);
        orderedProductsGrid.setEnabled(enabledOrderedGrid);
        deliveredProductsGrid.setEnabled(enabledDeliveredGrid);
        deliveredPackagesGrid.setEnabled(enablePackagesGrid);
    }

    private void fillDeliveryAddressDefaultValue(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent deliveryAddressField = (FieldComponent) view.getComponentByReference(DeliveryFields.DELIVERY_ADDRESS);

        if (Objects.nonNull(deliveryForm.getEntityId())) {
            return;
        }

        String deliveryAddress = (String) deliveryAddressField.getFieldValue();

        if (StringUtils.isEmpty(deliveryAddress)) {
            deliveryAddressField.setFieldValue(deliveriesService.getDeliveryAddressDefaultValue());
        }
    }

    private void fillDescriptionDefaultValue(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.nonNull(deliveryForm.getEntityId())) {
            return;
        }

        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(DeliveryFields.DESCRIPTION);
        String description = (String) descriptionField.getFieldValue();

        if (StringUtils.isEmpty(description)) {
            descriptionField.setFieldValue(deliveriesService.getDescriptionDefaultValue());
        }
    }

    private void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference(L_LOGGINGS_GRID);

        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(DeliveryStateChangeFields.STATUS, Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    private void updateRelatedDeliveryButtonsState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup reportsRibbonGroup = window.getRibbon().getGroupByName(L_RELATED_DELIVERY);
        RibbonActionItem createRelatedDeliveryRibbonActionItem = reportsRibbonGroup.getItemByName(L_CREATE_RELATED_DELIVERY);
        RibbonActionItem showRelatedDeliveryRibbonActionItem = reportsRibbonGroup.getItemByName(L_SHOW_RELATED_DELIVERIES);

        Long deliveryId = deliveryForm.getEntityId();

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);
        List<Entity> relatedDeliveries = delivery.getHasManyField(DeliveryFields.RELATED_DELIVERIES);

        boolean received = DeliveryState.RECEIVED.getStringValue().equals(delivery.getStringField(DeliveryFields.STATE));
        boolean created = (Objects.nonNull(relatedDeliveries) && !relatedDeliveries.isEmpty());

        updateButtonState(createRelatedDeliveryRibbonActionItem, received && !created);
        updateButtonState(showRelatedDeliveryRibbonActionItem, received && created);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private void fillCurrencyFields(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        List<String> referenceNames = Lists.newArrayList(L_DELIVERED_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY, L_ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY);

        Entity delivery = deliveryForm.getEntity();

        deliveriesService.fillCurrencyFieldsForDelivery(view, referenceNames, delivery);

        LookupComponent currencyLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.CURRENCY);

        if (Objects.isNull(currencyLookup.getFieldValue()) && Objects.isNull(deliveryForm.getEntityId())) {
            Entity currencyEntity = currencyService.getCurrentCurrency();

            currencyLookup.setFieldValue(currencyEntity.getId());
            currencyLookup.requestComponentUpdateState();
        }
    }

    private void disableShowProductButton(final ViewDefinitionState view) {
        deliveriesService.disableShowProductButton(view);
    }

    private void fillLocationDefaultValue(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.nonNull(deliveryForm.getEntityId())) {
            return;
        }

        LookupComponent locationField = (LookupComponent) view.getComponentByReference(DeliveryFields.LOCATION);
        Entity location = locationField.getEntity();

        if (Objects.isNull(location) && !view.isViewAfterReload()) {
            Entity defaultLocation = parameterService.getParameter().getBelongsToField(DeliveryFields.LOCATION);

            if (Objects.isNull(defaultLocation)) {
                locationField.setFieldValue(null);
            } else {
                locationField.setFieldValue(defaultLocation.getId());
            }

            locationField.requestComponentUpdateState();
        }
    }

    private void changeLocationEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        LookupComponent locationField = (LookupComponent) view.getComponentByReference(DeliveryFields.LOCATION);

        if (Objects.isNull(deliveryForm.getEntityId())) {
            locationField.setEnabled(true);
        } else {
            FieldComponent stateField = (FieldComponent) view.getComponentByReference(DeliveryFields.STATE);
            String state = stateField.getFieldValue().toString();

            locationField.setEnabled(!DeliveryState.DECLINED.getStringValue().equals(state) && !DeliveryState.RECEIVED.getStringValue().equals(state));
        }
    }

    private void updateCopyOrderedProductButtonsState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup reportsRibbonGroup = window.getRibbon().getGroupByName(L_COPY_ORDERED_PRODUCTS_TO_DELIVERY);
        RibbonActionItem copyWithoutRibbonActionItem = reportsRibbonGroup.getItemByName(L_COPY_PRODUCTS_WITHOUT_QUANTITY);
        RibbonActionItem copyWithRibbonActionItem = reportsRibbonGroup.getItemByName(L_COPY_PRODUCTS_WITH_QUANTITY);

        Long deliveryId = deliveryForm.getEntityId();

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        boolean hasOrderedProducts = !delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS).isEmpty();
        String state = delivery.getStringField(DeliveryFields.STATE);
        boolean isFinished = DeliveryState.RECEIVED.getStringValue().equals(state) || DeliveryState.DECLINED.getStringValue().equals(state);

        copyWithRibbonActionItem.setEnabled(hasOrderedProducts && !isFinished);
        copyWithoutRibbonActionItem.setEnabled(hasOrderedProducts && !isFinished);
        copyWithRibbonActionItem.requestUpdate(true);
        copyWithoutRibbonActionItem.requestUpdate(true);
    }

    public void processRoles(final ViewDefinitionState view) {
        Entity currentUser = userService.getCurrentUserEntity();
        String state = getState(view);
        for (DeliveryRole role : DeliveryRole.values()) {
            if (role.equals(DeliveryRole.ROLE_DELIVERIES_STATES_APPROVE)) {
                if (!securityService.hasRole(currentUser, role.toString())
                        && (DeliveryStateStringValues.DRAFT.equals(state)
                        || DeliveryStateStringValues.PREPARED.equals(state)
                        || DeliveryStateStringValues.DURING_CORRECTION.equals(state))) {
                    role.processRole(view);
                }
            } else if (role.equals(DeliveryRole.ROLE_DELIVERIES_STATES_ACCEPT)) {
                if (!securityService.hasRole(currentUser, role.toString())
                        && DeliveryStateStringValues.APPROVED.equals(state)) {
                    role.processRole(view);
                }
            } else if (!securityService.hasRole(currentUser, role.toString())) {
                role.processRole(view);
            }
        }
    }

    private String getState(ViewDefinitionState view) {
        String state;

        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (deliveryForm != null) {
            Long deliveryId = deliveryForm.getEntityId();

            if (Objects.isNull(deliveryId)) {
                state = DeliveryStateStringValues.DRAFT;
            } else {
                Entity delivery = deliveriesService.getDelivery(deliveryId);
                state = delivery.getStringField(DeliveryFields.STATE);
            }
        } else {
            GridComponent deliveriesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
            Set<String> states = deliveriesGrid.getSelectedEntities().stream().map(e -> e.getStringField(DeliveryFields.STATE)).collect(Collectors.toSet());
            if (states.size() == 1) {
                state = states.stream().findFirst().get();
            } else {
                state = DeliveryStateStringValues.DRAFT;
            }
        }
        return state;
    }

    private void togglePriceFields(final ViewDefinitionState view) {
        FieldComponent deliveredTotalPriceField = (FieldComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS_CUMULATED_TOTAL_PRICE);
        FieldComponent orderedTotalPriceField = (FieldComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE);
        FieldComponent deliveredTotalPriceCurrencyField = (FieldComponent) view.getComponentByReference(L_DELIVERED_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY);
        FieldComponent orderedTotalPriceCurrencyField = (FieldComponent) view.getComponentByReference(L_ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY);

        boolean hasCurrentUserRole = securityService.hasCurrentUserRole("ROLE_DELIVERIES_PRICE");

        deliveredTotalPriceField.setVisible(hasCurrentUserRole);
        orderedTotalPriceField.setVisible(hasCurrentUserRole);
        deliveredTotalPriceCurrencyField.setVisible(hasCurrentUserRole);
        orderedTotalPriceCurrencyField.setVisible(hasCurrentUserRole);
    }

    private void updateChangeStorageLocationButton(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup deliveryPositionsRibbonGroup = window.getRibbon().getGroupByName(L_DELIVERY_POSITIONS);
        RibbonActionItem changeStorageLocationsRibbonActionItem = deliveryPositionsRibbonGroup.getItemByName(L_CHANGE_STORAGE_LOCATIONS);

        List<Entity> selectedProducts = deliveredProductsGrid.getSelectedEntities();

        Long deliveryId = deliveryForm.getEntityId();

        boolean isEnabled = false;

        if (Objects.nonNull(deliveryId)) {
            Entity delivery = deliveriesService.getDelivery(deliveryId);

            String state = delivery.getStringField(DeliveryFields.STATE);
            boolean isFinished = DeliveryState.RECEIVED.getStringValue().equals(state) || DeliveryState.DECLINED.getStringValue().equals(state);

            isEnabled = !selectedProducts.isEmpty() && !isFinished;

            if (isEnabled) {
                String baseStorageLocation = Optional.ofNullable(selectedProducts.get(0).getStringField(DeliveredProductDtoFields.STORAGE_LOCATION_NUMBER)).orElse(StringUtils.EMPTY);

                for (Entity deliveredProduct : selectedProducts) {
                    String storageLocation = Optional.ofNullable(deliveredProduct.getStringField(DeliveredProductDtoFields.STORAGE_LOCATION_NUMBER)).orElse(StringUtils.EMPTY);

                    if (!baseStorageLocation.equals(storageLocation)) {
                        isEnabled = false;
                    }
                }
            }
        }

        updateButtonState(changeStorageLocationsRibbonActionItem, isEnabled);
    }

    private void updateAssignStorageLocationsButton(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup deliveryPositionsRibbonGroup = window.getRibbon().getGroupByName(L_DELIVERY_POSITIONS);
        RibbonActionItem assignStorageLocationsRibbonActionItem = deliveryPositionsRibbonGroup.getItemByName(L_ASSIGN_STORAGE_LOCATIONS);

        Long deliveryId = deliveryForm.getEntityId();

        boolean isEnabled = false;

        if (Objects.nonNull(deliveryId)) {
            Entity delivery = deliveriesService.getDelivery(deliveryId);

            String state = delivery.getStringField(DeliveryFields.STATE);

            isEnabled = !DeliveryState.RECEIVED.getStringValue().equals(state) && !DeliveryState.DECLINED.getStringValue().equals(state);
        }

        updateButtonState(assignStorageLocationsRibbonActionItem, isEnabled);
    }

    private void orderGridByProductNumber(final ViewDefinitionState view) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);

        String productNumberFilter = gridComponent.getFilters().get(L_PRODUCT_NUMBER);

        if (!Strings.isNullOrEmpty(productNumberFilter) && productNumberFilter.startsWith("[") && productNumberFilter.endsWith("]")) {

            List<Entity> orderedProductsEntities = gridComponent.getEntities();
            List<Entity> sortedEntities = Lists.newArrayList();

            for (String filter : getSortedItemsFromFilter(productNumberFilter)) {
                for (Iterator<Entity> orderedProduct = orderedProductsEntities.listIterator(); orderedProduct.hasNext(); ) {
                    Entity entity = orderedProduct.next();

                    if (filter.equals(entity.getStringField(L_PRODUCT_NUMBER))) {
                        sortedEntities.add(entity);

                        orderedProduct.remove();

                        break;
                    }
                }
            }

            sortedEntities.addAll(orderedProductsEntities);
            gridComponent.setEntities(sortedEntities);
        }
    }

    private String[] getSortedItemsFromFilter(String productNumberFilter) {
        productNumberFilter = productNumberFilter.substring(1, productNumberFilter.length() - 1);

        return productNumberFilter.split(",");
    }

    private void setDeliveryIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent deliveryIdForMultiUpload = (FieldComponent) view.getComponentByReference("deliveryIdForMultiUpload");
        FieldComponent deliveryMultiUploadLocale = (FieldComponent) view.getComponentByReference("deliveryMultiUploadLocale");

        if (Objects.nonNull(deliveryForm.getEntityId())) {
            deliveryIdForMultiUpload.setFieldValue(deliveryForm.getEntityId());
        } else {
            deliveryIdForMultiUpload.setFieldValue("");
        }

        deliveryIdForMultiUpload.requestComponentUpdateState();
        deliveryMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        deliveryMultiUploadLocale.requestComponentUpdateState();
    }

    public Set<String> fillRowStyles(final Entity orderedProduct) {
        final Set<String> rowStyles = Sets.newHashSet();

        BigDecimal orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);

        if (Objects.nonNull(orderedQuantity) && BigDecimal.ZERO.compareTo(orderedQuantity) == 0) {
            rowStyles.add(RowStyle.RED_BACKGROUND);
        }

        return rowStyles;
    }

}
