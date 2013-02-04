/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.materialFlowMultitransfers.listeners;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCTS;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowMultitransfers.constants.MaterialFlowMultitransfersConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class MultitransferListeners {

    private static final String L_ERROR_FILL_AT_LEAST_ONE_LOCATION = "materialFlow.validate.global.error.fillAtLeastOneLocation";

    private static final String L_FORM = "form";

    private static final String L_UNIT = "unit";

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public void createMultitransfer(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (!isMultitransferFormValid(view)) {
            return;
        }

        FormComponent multitransferForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent typeField = (FieldComponent) view.getComponentByReference(TYPE);
        FieldComponent timeField = (FieldComponent) view.getComponentByReference(TIME);
        FieldComponent locationFromField = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
        FieldComponent locationToField = (FieldComponent) view.getComponentByReference(LOCATION_TO);
        FieldComponent staffField = (FieldComponent) view.getComponentByReference(STAFF);

        typeField.requestComponentUpdateState();
        timeField.requestComponentUpdateState();
        locationToField.requestComponentUpdateState();
        locationFromField.requestComponentUpdateState();
        staffField.requestComponentUpdateState();

        String type = typeField.getFieldValue().toString();
        Date time = DateUtils.parseDate(timeField.getFieldValue());
        Entity locationFrom = materialFlowService.getLocationById((Long) locationFromField.getFieldValue());
        Entity locationTo = materialFlowService.getLocationById((Long) locationToField.getFieldValue());
        Entity staff = materialFlowService.getStaffById((Long) staffField.getFieldValue());

        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            BigDecimal quantity = productQuantity.getDecimalField(QUANTITY);
            Entity product = productQuantity.getBelongsToField(PRODUCT);

            if ((product != null) && (quantity != null)) {
                createTransfer(type, time, locationFrom, locationTo, staff, product, quantity);
            }
        }

        adlc.setFieldValue(null);

        multitransferForm.setEntity(multitransferForm.getEntity());

        state.performEvent(view, "refresh", new String[0]);

        view.getComponentByReference(L_FORM).addMessage("materialFlowMultitransfers.multitransfer.generate.success",
                MessageType.SUCCESS);
    }

    public boolean isMultitransferFormValid(final ViewDefinitionState view) {
        boolean isValid = true;

        FieldComponent typeField = (FieldComponent) view.getComponentByReference(TYPE);
        FieldComponent timeField = (FieldComponent) view.getComponentByReference(TIME);
        FieldComponent locationFromField = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
        FieldComponent locationToField = (FieldComponent) view.getComponentByReference(LOCATION_TO);

        Entity locationFrom = materialFlowService.getLocationById((Long) locationFromField.getFieldValue());
        Entity locationTo = materialFlowService.getLocationById((Long) locationToField.getFieldValue());

        if (materialFlowService.checkIfLocationHasExternalNumber(locationFrom)) {
            locationFromField.addMessage("materialFlow.validate.global.error.externalNumber", MessageType.FAILURE);

            isValid = false;
        }

        if (materialFlowService.checkIfLocationHasExternalNumber(locationTo)) {
            locationToField.addMessage("materialFlow.validate.global.error.externalNumber", MessageType.FAILURE);

            isValid = false;
        }

        if (typeField.getFieldValue() == null || typeField.getFieldValue().toString().isEmpty()) {
            typeField.addMessage("materialFlow.validate.global.error.fillType", MessageType.FAILURE);

            isValid = false;
        }

        if (timeField.getFieldValue() == null || timeField.getFieldValue().toString().isEmpty()) {
            timeField.addMessage("materialFlow.validate.global.error.fillDate", MessageType.FAILURE);

            isValid = false;
        } else {
            Date time = DateUtils.parseDate(timeField.getFieldValue());

            if (materialFlowResourcesService.shouldValidateDateWhenTransferToWarehouse()
                    && materialFlowResourcesService.areLocationsWarehouses(locationFrom, locationTo)
                    && !materialFlowResourcesService.isDateGraterThanResourcesDate(time)) {
                timeField.addMessage("materialFlowResources.validate.global.error.dateLowerThanResourcesDate",
                        MessageType.FAILURE);

                isValid = false;
            }
        }

        if ((locationFromField.getFieldValue() == null) && (locationToField.getFieldValue() == null)) {
            locationFromField.addMessage(L_ERROR_FILL_AT_LEAST_ONE_LOCATION, MessageType.FAILURE);
            locationToField.addMessage(L_ERROR_FILL_AT_LEAST_ONE_LOCATION, MessageType.FAILURE);

            isValid = false;
        }

        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        List<FormComponent> formComponents = adlc.getFormComponents();

        if (formComponents.isEmpty()) {
            view.getComponentByReference(L_FORM).addMessage(
                    "materialFlowMultitransfers.multitransfer.validation.productsAreRequired", MessageType.FAILURE);

            isValid = false;
        } else {
            String type = (String) typeField.getFieldValue();

            for (FormComponent formComponent : formComponents) {
                Entity productQuantity = formComponent.getEntity();

                BigDecimal quantity = productQuantity.getDecimalField(QUANTITY);
                Entity product = productQuantity.getBelongsToField(PRODUCT);

                if (product == null) {
                    formComponent.findFieldComponentByName(PRODUCT).addMessage("materialFlow.validate.global.error.fillRequired",
                            MessageType.FAILURE);

                    isValid = false;
                } else {
                    if (isProductAlreadyAdded(formComponents, product)) {
                        formComponent.findFieldComponentByName(PRODUCT).addMessage(
                                "materialFlow.validate.global.error.productAlreadyAdded", MessageType.FAILURE);

                        isValid = false;
                    }
                }

                if ((type != null) && !PRODUCTION.getStringValue().equals(type) && (locationFrom != null) && (product != null)
                        && (quantity != null)
                        && !materialFlowResourcesService.areResourcesSufficient(locationFrom, product, quantity)) {
                    formComponent.findFieldComponentByName(QUANTITY).addMessage(
                            "materialFlowResources.validate.global.error.resourcesArentSufficient", MessageType.FAILURE);

                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean isProductAlreadyAdded(final List<FormComponent> formComponents, final Entity product) {
        if ((formComponents != null) && (product != null)) {
            int count = 0;

            for (FormComponent formComponent : formComponents) {
                Entity productQuantity = formComponent.getEntity();
                Entity productAlreadyAdded = productQuantity.getBelongsToField(PRODUCT);

                if (product.equals(productAlreadyAdded)) {
                    count++;
                    if (count > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void createTransfer(final String type, final Date time, final Entity locationFrom, final Entity locationTo,
            final Entity staff, final Entity product, final BigDecimal quantity) {
        DataDefinition transferDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);

        Entity transfer = transferDD.create();
        String number = materialFlowService.generateNumberFromProduct(product, MaterialFlowConstants.MODEL_TRANSFER);

        transfer.setField(NUMBER, number);
        transfer.setField(TYPE, type);
        transfer.setField(TIME, time);
        transfer.setField(LOCATION_FROM, locationFrom);
        transfer.setField(LOCATION_TO, locationTo);
        transfer.setField(STAFF, staff);
        transfer.setField(PRODUCT, product);
        transfer.setField(QUANTITY, quantity);

        checkArgument(transferDD.save(transfer).isValid(), "invalid transfer id =" + transfer.getId());
    }

    public void fillUnitsInADL(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        fillUnitsInADL(view, PRODUCTS);
    }

    private void fillUnitsInADL(final ViewDefinitionState view, final String adlName) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(adlName);
        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            Entity product = productQuantity.getBelongsToField(PRODUCT);

            if (product == null) {
                formComponent.findFieldComponentByName(L_UNIT).setFieldValue(null);
            } else {
                formComponent.findFieldComponentByName(L_UNIT).setFieldValue(product.getStringField(UNIT));
            }

            formComponent.setEntity(productQuantity);
        }
    }

    public void getFromTemplates(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        getFromTemplates(view);
    }

    public void getFromTemplates(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        FieldComponent locationFromField = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
        FieldComponent locationToField = (FieldComponent) view.getComponentByReference(LOCATION_TO);

        Entity locationFrom = materialFlowService.getLocationById((Long) locationFromField.getFieldValue());
        Entity locationTo = materialFlowService.getLocationById((Long) locationToField.getFieldValue());

        List<Entity> templates = getTransferTemplates(locationFrom, locationTo);

        if (templates.isEmpty()) {
            view.getComponentByReference(L_FORM).addMessage("materialFlowMultitransfers.multitransfer.template.failure",
                    MessageType.INFO);
            return;
        }

        List<Entity> productQuantities = Lists.newArrayList();

        DataDefinition productQuantityDD = dataDefinitionService.get(MaterialFlowMultitransfersConstants.PLUGIN_IDENTIFIER,
                MaterialFlowMultitransfersConstants.MODEL_PRODUCT_QUANTITY);

        for (Entity template : templates) {
            Entity product = template.getBelongsToField(PRODUCT);

            Entity productQuantity = productQuantityDD.create();

            productQuantity.setField(PRODUCT, product);

            productQuantities.add(productQuantity);
        }

        adlc.setFieldValue(productQuantities);

        fillUnitsInADL(view, PRODUCTS);

        view.getComponentByReference(L_FORM).addMessage("materialFlowMultitransfers.multitransfer.template.success",
                MessageType.SUCCESS);
    }

    private List<Entity> getTransferTemplates(final Entity locationFrom, final Entity locationTo) {
        return dataDefinitionService
                .get(MaterialFlowMultitransfersConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowMultitransfersConstants.MODEL_TRANSFER_TEMPLATE).find().createAlias(PRODUCT, PRODUCT)
                .add(SearchRestrictions.belongsTo(LOCATION_FROM, locationFrom))
                .add(SearchRestrictions.belongsTo(LOCATION_TO, locationTo)).addOrder(SearchOrders.asc(PRODUCT + "." + NAME))
                .list().getEntities();
    }

    public void disableDateField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        materialFlowResourcesService.disableDateField(view);
    }

    public void checkIfLocationFromHasExternalNumber(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        checkIfLocationFromHasExternalNumber(view);
    }

    public void checkIfLocationToHasExternalNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        checkIfLocationToHasExternalNumber(view);
    }

    public void checkIfLocationToHasExternalNumber(final ViewDefinitionState view) {
        materialFlowService.checkIfLocationHasExternalNumber(view, LOCATION_TO);
    }

    public void checkIfLocationFromHasExternalNumber(final ViewDefinitionState view) {
        materialFlowService.checkIfLocationHasExternalNumber(view, LOCATION_FROM);
    }

}
