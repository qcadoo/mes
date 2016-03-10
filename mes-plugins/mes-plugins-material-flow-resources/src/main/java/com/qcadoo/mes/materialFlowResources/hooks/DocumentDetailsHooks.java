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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.base.Strings;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.listeners.DocumentDetailsListeners;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class DocumentDetailsHooks {

    private static final String RIBBON_GROUP = "actions";

    private static final List<String> RIBBON_ACTION_ITEM = Arrays.asList("saveBack", "saveNew", "save", "delete", "copy");

    private static final String STATE_GROUP = "state";

    private static final String ACCEPT_ITEM = "accept";

    private static final String PRINT_GROUP = "print";

    private static final String PRINT_PDF_ITEM = "printPdf";

    private static final List<String> INBOUND_FIELDS = Arrays.asList("price", "batch", "productionDate", "expirationDate");

    public static final String FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentDetailsListeners documentDetailsListeners;

    public void onBeforeRender(final ViewDefinitionState view) {
        initializeDocument(view);
        documentDetailsListeners.calculateQuantity(view, null, new String[0]);
        setCriteriaModifiersParameters(view);
        lockNumberAndTypeChange(view);
        fetchNameAndNumberFromDatabase(view);
    }

    // fixme: refactor
    public void showFieldsByDocumentType(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();

        String documentType = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.RECEIPT.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_INBOUND.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, true);
            enableStorageLocation(view, true);
            showWarehouse(view, false, true);
            showCompany(view, true);
        } else if (DocumentType.TRANSFER.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            enableStorageLocation(view, true);
            showWarehouse(view, true, true);
            showCompany(view, false);
        } else if (DocumentType.RELEASE.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_OUTBOUND.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            enableStorageLocation(view, false);
            showWarehouse(view, true, false);
            showCompany(view, true);
        } else {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            enableStorageLocation(view, true);
            showWarehouse(view, false, false);
            showCompany(view, false);
        }
    }

    private void showWarehouse(final ViewDefinitionState view, boolean from, boolean to) {
        FieldComponent locationFrom = (FieldComponent) view.getComponentByReference("locationFrom");
        locationFrom.setEnabled(from);

        FieldComponent locationTo = (FieldComponent) view.getComponentByReference("locationTo");
        locationTo.setEnabled(to);
    }

    private void showCompany(final ViewDefinitionState view, boolean visible) {
        FieldComponent company = (FieldComponent) view.getComponentByReference("company");
        company.setEnabled(visible);
    }

    private void enableStorageLocation(final ViewDefinitionState view, boolean enabled) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(DocumentFields.POSITIONS);
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            FieldComponent storageLocation = positionForm.findFieldComponentByName(PositionFields.STORAGE_LOCATION);

            storageLocation.setEnabled(enabled);
        }
    }

    private void enableInboundDocumentPositionsAttributesAndFillInUnit(final ViewDefinitionState view, final boolean enabled) {

        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(DocumentFields.POSITIONS);
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            for (String fieldName : INBOUND_FIELDS) {
                FieldComponent field = positionForm.findFieldComponentByName(fieldName);
                field.setEnabled(enabled);

            }
            FieldComponent givenQuantity = positionForm.findFieldComponentByName(PositionFields.GIVEN_QUANTITY);
            FieldComponent givenUnit = positionForm.findFieldComponentByName(PositionFields.GIVEN_UNIT);
            givenQuantity.setRequired(true);
            givenUnit.setRequired(true);
            fillInUnit(positionForm);

        }
    }

    private void fillInUnit(FormComponent positionForm) {
        Entity position = positionForm.getPersistedEntityWithIncludedFormValues();
        if (!position.isValid()) {
            return;
        }
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        if (product == null) {
            return;
        }

        String unit = product.getStringField(UNIT);
        String givenUnit = position.getStringField(PositionFields.GIVEN_UNIT);
        if (StringUtils.isEmpty(givenUnit)) {
            position.setField(PositionFields.GIVEN_UNIT, unit);
        }

        position.setField(PositionFields.UNIT, unit);
        positionForm.setEntity(position);
    }

    public void initializeDocument(final ViewDefinitionState view) {
        showFieldsByDocumentType(view);
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Long documentId = formComponent.getEntityId();

        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();
        DocumentState state = DocumentState.of(document);

        documentDetailsListeners.showAndSetRequiredForResourceLookup(view);

        if (documentId == null) {
            changeAcceptButtonState(window, false);
            changePrintButtonState(window, false);
            FieldComponent date = (FieldComponent) view.getComponentByReference(DocumentFields.TIME);
            FieldComponent user = (FieldComponent) view.getComponentByReference(DocumentFields.USER);
            if (date.getFieldValue() == null) {
                date.setFieldValue(setDateToField(new Date()));
            }
            user.setFieldValue(userService.getCurrentUserEntity().getId());
        } else if (DocumentState.DRAFT.equals(state)) {
            changeAcceptButtonState(window, true);
            changePrintButtonState(window, true);
        } else if (DocumentState.ACCEPTED.equals(state)) {
            formComponent.setFormEnabled(false);
            disableADL(view);
            disableRibbon(window);
            changePrintButtonState(window, true);
        }

    }

    private void disableADL(ViewDefinitionState view) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(DocumentFields.POSITIONS);
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            positionForm.setFormEnabled(false);
        }
        
        positionsADL.setEnabled(false);
        positionsADL.requestComponentUpdateState();
    }

    private void disableRibbon(final WindowComponent window) {
        for (String actionItem : RIBBON_ACTION_ITEM) {
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).setEnabled(false);
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).requestUpdate(true);
        }
        changeAcceptButtonState(window, false);
    }

    private void changeAcceptButtonState(WindowComponent window, final boolean enable) {
        RibbonActionItem actionItem = (RibbonActionItem) window.getRibbon().getGroupByName(STATE_GROUP)
                .getItemByName(ACCEPT_ITEM);
        actionItem.setEnabled(enable);
        actionItem.requestUpdate(true);
    }

    private void changePrintButtonState(WindowComponent window, final boolean enable) {
        RibbonActionItem actionItem = (RibbonActionItem) window.getRibbon().getGroupByName(PRINT_GROUP)
                .getItemByName(PRINT_PDF_ITEM);
        actionItem.setEnabled(enable);
        actionItem.requestUpdate(true);
    }

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    public void setCriteriaModifiersParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(FORM);
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        if (!DocumentState.of(document).equals(DocumentState.ACCEPTED)) {
            Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            Entity warehouseTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
            AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view
                    .getComponentByReference(DocumentFields.POSITIONS);
            for (FormComponent positionForm : positionsADL.getFormComponents()) {
                Entity position = positionForm.getPersistedEntityWithIncludedFormValues();
                Entity product = position.getBelongsToField(PositionFields.PRODUCT);
                LookupComponent resourcesLookup = (LookupComponent) positionForm
                        .findFieldComponentByName(PositionFields.RESOURCE);
                LookupComponent storageLocationLookup = (LookupComponent) positionForm
                        .findFieldComponentByName(PositionFields.STORAGE_LOCATION);
                FilterValueHolder filter = resourcesLookup.getFilterValue();
                FilterValueHolder storageLocationFilter = storageLocationLookup.getFilterValue();
                if (warehouseFrom != null) {
                    filter.put("locationFrom", warehouseFrom.getId());
                }
                if (product != null) {
                    filter.put("product", product.getId());
                    storageLocationFilter.put("product", product.getId());
                }
                if (warehouseTo != null) {
                    storageLocationFilter.put("location", warehouseTo.getId());
                }
                resourcesLookup.setFilterValue(filter);
                storageLocationLookup.setFilterValue(storageLocationFilter);
            }
        }
    }

    private void lockNumberAndTypeChange(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);

        ComponentState typeComponent = view.getComponentByReference(DocumentFields.TYPE);
        if (formComponent.getEntityId() != null) {
            typeComponent.setEnabled(false);

        } else {
            typeComponent.setEnabled(true);
        }

        FieldComponent numberFieldComponent = (FieldComponent) view.getComponentByReference(DocumentFields.NUMBER);
        numberFieldComponent.setEnabled(false);
    }

    private void fetchNameAndNumberFromDatabase(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        if (formComponent.getEntityId() != null) {
            ComponentState numberField = view.getComponentByReference(DocumentFields.NUMBER);
            ComponentState nameField = view.getComponentByReference(DocumentFields.NAME);

            String nameFieldValue = (String) nameField.getFieldValue();
            String numberFieldValue = (String) numberField.getFieldValue();

            if (!numberFieldValue.contains("/")) {
                Entity document = getDocumentDD().get(formComponent.getEntityId());

                if (!numberFieldValue.contains("/")) {
                    numberField.setFieldValue(document.getField(DocumentFields.NUMBER));
                }

                if (Strings.isNullOrEmpty(nameFieldValue)) {
                    nameField.setFieldValue(document.getField(DocumentFields.NAME));
                }
            }
        }
    }

    private DataDefinition getDocumentDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT);
    }
}
