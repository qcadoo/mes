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
package com.qcadoo.mes.deliveries.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.roles.DeliveryRole;
import com.qcadoo.mes.deliveries.states.constants.DeliveryState;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.ribbon.Ribbon;
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

    private static final String ASSIGN_STORAGE_LOCATIONS = "assignStorageLocations";

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

    public void generateDeliveryNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERY, QcadooViewConstants.L_FORM, DeliveryFields.NUMBER);
    }

    public void fillCompanyFieldsForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view
                .getComponentByReference(DeliveryFields.DELIVERY_DATE_BUFFER);
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
            if (DeliveryState.PREPARED.getStringValue().equals(state) || DeliveryState.APPROVED.getStringValue().equals(state)) {
                changeFieldsEnabled(view, false, false, true, true);
            } else if (DeliveryState.DECLINED.getStringValue().equals(state)
                    || DeliveryState.RECEIVED.getStringValue().equals(state)
                    || DeliveryState.RECEIVE_CONFIRM_WAITING.getStringValue().equals(state)) {
                changeFieldsEnabled(view, false, false, false, false);
            } else {
                changeFieldsEnabled(view, true, true, true, true);
            }
        }
    }

    private void changeFieldsEnabled(final ViewDefinitionState view, final boolean enabledForm, final boolean enabledOrderedGrid,
                                     final boolean enabledDeliveredGrid, final boolean enablePackagesGrid) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);
        GridComponent deliveredPackagesGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PACKAGES);

        deliveryForm.setFormEnabled(enabledForm);
        orderedProductsGrid.setEnabled(enabledOrderedGrid);
        deliveredProductsGrid.setEnabled(enabledDeliveredGrid);
        deliveredPackagesGrid.setEnabled(enabledDeliveredGrid);
    }

    private void fillDeliveryAddressDefaultValue(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.nonNull(deliveryForm.getEntityId())) {
            return;
        }

        FieldComponent deliveryAddressField = (FieldComponent) view.getComponentByReference(DeliveryFields.DELIVERY_ADDRESS);
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
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                DeliveryStateChangeFields.STATUS, Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    private void updateRelatedDeliveryButtonsState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long deliveryId = deliveryForm.getEntityId();

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup reports = window.getRibbon().getGroupByName(L_RELATED_DELIVERY);

        RibbonActionItem createRelatedDelivery = reports.getItemByName(L_CREATE_RELATED_DELIVERY);
        RibbonActionItem showRelatedDelivery = reports.getItemByName(L_SHOW_RELATED_DELIVERIES);

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);
        List<Entity> relatedDeliveries = delivery.getHasManyField(DeliveryFields.RELATED_DELIVERIES);

        boolean received = DeliveryState.RECEIVED.getStringValue().equals(delivery.getStringField(DeliveryFields.STATE));
        boolean receiveConfirmWaiting = DeliveryState.RECEIVE_CONFIRM_WAITING.getStringValue()
                .equals(delivery.getStringField(DeliveryFields.STATE));
        boolean created = (Objects.nonNull(relatedDeliveries) && !relatedDeliveries.isEmpty());

        updateButtonState(createRelatedDelivery, (received || receiveConfirmWaiting) && !created);
        updateButtonState(showRelatedDelivery, (received || receiveConfirmWaiting) && created);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private void fillCurrencyFields(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        List<String> referenceNames = Lists.newArrayList("deliveredProductsCumulatedTotalPriceCurrency",
                "orderedProductsCumulatedTotalPriceCurrency");

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

            locationField.setEnabled(!DeliveryState.DECLINED.getStringValue().equals(state) && !DeliveryState.RECEIVED.getStringValue().equals(state)
                    && !DeliveryState.RECEIVE_CONFIRM_WAITING.getStringValue().equals(state));
        }
    }

    private void updateCopyOrderedProductButtonsState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long deliveryId = deliveryForm.getEntityId();

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup reports = window.getRibbon().getGroupByName(L_COPY_ORDERED_PRODUCTS_TO_DELIVERY);

        RibbonActionItem copyWithout = reports.getItemByName(L_COPY_PRODUCTS_WITHOUT_QUANTITY);
        RibbonActionItem copyWith = reports.getItemByName(L_COPY_PRODUCTS_WITH_QUANTITY);

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        boolean hasOrderedProducts = !delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS).isEmpty();
        String state = delivery.getStringField(DeliveryFields.STATE);
        boolean isFinished = DeliveryState.RECEIVED.getStringValue().equals(state)
                || DeliveryState.DECLINED.getStringValue().equals(state);

        copyWith.setEnabled(hasOrderedProducts && !isFinished);
        copyWithout.setEnabled(hasOrderedProducts && !isFinished);
        copyWith.requestUpdate(true);
        copyWithout.requestUpdate(true);
    }

    public void processRoles(final ViewDefinitionState view) {
        Entity currentUser = userService.getCurrentUserEntity();

        for (DeliveryRole role : DeliveryRole.values()) {
            if (!securityService.hasRole(currentUser, role.toString())) {
                role.processRole(view);
            }
        }
    }

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
        disableShowProductButton(view);
        fillLocationDefaultValue(view);
        changeLocationEnabledDependOnState(view);
        updateCopyOrderedProductButtonsState(view);
        processRoles(view);
        setDeliveryIdForMultiUploadField(view);
    }

    private void updateChangeStorageLocationButton(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup group = ribbon.getGroupByName(L_DELIVERY_POSITIONS);
        RibbonActionItem changeStorageLocations = group.getItemByName(L_CHANGE_STORAGE_LOCATIONS);

        List<Entity> selectedProducts = deliveredProductsGrid.getSelectedEntities();

        Long deliveryId = deliveryForm.getEntityId();

        boolean enabled = false;

        if (Objects.nonNull(deliveryId)) {
            Entity delivery = deliveriesService.getDelivery(deliveryId);

            String state = delivery.getStringField(DeliveryFields.STATE);
            boolean isFinished = DeliveryState.RECEIVED.getStringValue().equals(state)
                    || DeliveryState.DECLINED.getStringValue().equals(state);

            enabled = !selectedProducts.isEmpty() && !isFinished;

            if (enabled) {
                String baseStorageLocation = Optional.ofNullable(selectedProducts.get(0).getStringField("storageLocationNumber"))
                        .orElse(StringUtils.EMPTY);

                for (Entity deliveredProduct : selectedProducts) {
                    String storageLocation = Optional.ofNullable(deliveredProduct.getStringField("storageLocationNumber"))
                            .orElse(StringUtils.EMPTY);

                    if (!baseStorageLocation.equals(storageLocation)) {
                        enabled = false;
                    }
                }
            }
        }

        updateButtonState(changeStorageLocations, enabled);
    }

    private void updateAssignStorageLocationsButton(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup group = ribbon.getGroupByName(L_DELIVERY_POSITIONS);
        RibbonActionItem assignStorageLocations = group.getItemByName(ASSIGN_STORAGE_LOCATIONS);

        Long deliveryId = deliveryForm.getEntityId();

        boolean enabled = false;

        if (Objects.nonNull(deliveryId)) {
            Entity delivery = deliveriesService.getDelivery(deliveryId);
            String state = delivery.getStringField(DeliveryFields.STATE);
            enabled = !DeliveryState.RECEIVED.getStringValue().equals(state)
                    && !DeliveryState.RECEIVE_CONFIRM_WAITING.getStringValue().equals(state)
                    && !DeliveryState.DECLINED.getStringValue().equals(state);
        }
        updateButtonState(assignStorageLocations, enabled);
    }

    private void orderGridByProductNumber(ViewDefinitionState view) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);

        String productNumberFilter = gridComponent.getFilters().get("productNumber");

        if (!Strings.isNullOrEmpty(productNumberFilter) && productNumberFilter.startsWith("[")
                && productNumberFilter.endsWith("]")) {

            List<Entity> orderedProductsEntities = gridComponent.getEntities();
            List<Entity> sortedEntities = Lists.newArrayList();

            for (String filter : getSortedItemsFromFilter(productNumberFilter)) {
                for (Iterator<Entity> orderedProduct = orderedProductsEntities.listIterator(); orderedProduct.hasNext(); ) {
                    Entity entity = orderedProduct.next();

                    if (filter.equals(entity.getStringField("productNumber"))) {
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
