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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.criteriaModifiers.AddressCriteriaModifiers;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.service.ReservationsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.qcadoo.mes.materialFlowResources.listeners.DocumentsListListeners.MOBILE_WMS;
import static com.qcadoo.mes.materialFlowResources.listeners.DocumentsListListeners.REALIZED;

@Service
public class DocumentDetailsHooks {

    public static final String L_PRINT_DISPOSITION_ORDER_PDF = "printDispositionOrderPdf";

    private static final String L_ACTIONS = "actions";

    private static final String L_STATE = "state";

    private static final String L_PRINT = "print";

    private static final String L_ACCEPT = "accept";

    private static final String L_PRINT_PDF = "printPdf";

    private static final String L_IMPORT = "import";

    private static final String L_OPEN_POSITIONS_IMPORT_PAGE = "openPositionsImportPage";

    private static final List<String> L_ACTIONS_ITEMS = Arrays.asList("saveBack", "saveNew", "save", "delete", "copy");

    public static final String L_SHOW_PRODUCT_ATTRIBUTES = "showProductAttributes";

    public static final String L_ATTRIBUTES = "attributes";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private PluginManager pluginManager;

    public void onBeforeRender(final ViewDefinitionState view) {
        initializeDocument(view);
        lockNumberAndTypeChange(view);
        fetchNameAndNumberFromDatabase(view);
        setRibbonState(view);
        fillAddressLookupCriteriaModifier(view);
        setAdditionalConditions(view);
    }

    private void setAdditionalConditions(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity document = documentForm.getPersistedEntityWithIncludedFormValues();
        LookupComponent locationFromLookup = (LookupComponent) view
                .getComponentByReference(DocumentFields.LINKED_DOCUMENT_LOCATION);
        CheckBoxComponent createLinkedDocumentCheckBox = (CheckBoxComponent) view
                .getComponentByReference(DocumentFields.CREATE_LINKED_DOCUMENT);

        String state = document.getStringField(DocumentFields.STATE);

        if (DocumentState.ACCEPTED.getStringValue().equals(state)) {
            locationFromLookup.setEnabled(false);
            createLinkedDocumentCheckBox.setEnabled(false);
        } else {
            String type = document.getStringField(DocumentFields.TYPE);

            if (DocumentType.RELEASE.getStringValue().equals(type)) {
                if (createLinkedDocumentCheckBox.isChecked()) {
                    locationFromLookup.setEnabled(true);
                } else {
                    locationFromLookup.setFieldValue(null);
                    locationFromLookup.setEnabled(false);
                }

                createLinkedDocumentCheckBox.setEnabled(true);
            } else {
                locationFromLookup.setFieldValue(null);
                locationFromLookup.setEnabled(false);
                createLinkedDocumentCheckBox.setChecked(false);
                createLinkedDocumentCheckBox.setEnabled(false);
            }
        }

        locationFromLookup.requestComponentUpdateState();
        createLinkedDocumentCheckBox.requestComponentUpdateState();
    }

    public void onCreateLinkedDocumentChange(final ViewDefinitionState view, final ComponentState formState,
                                             final String[] args) {
        setAdditionalConditions(view);
    }

    public void showFieldsByDocumentType(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity document = documentForm.getPersistedEntityWithIncludedFormValues();

        String documentType = document.getStringField(DocumentFields.TYPE);
        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);

        if (DocumentType.RECEIPT.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_INBOUND.getStringValue().equals(documentType)) {
            showWarehouse(view, false, true);
            showCompanyAndAddress(view, true);
        } else if (DocumentType.TRANSFER.getStringValue().equals(documentType)) {
            showWarehouse(view, true, true);
            showCompanyAndAddress(view, false);
        } else if (DocumentType.RELEASE.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_OUTBOUND.getStringValue().equals(documentType)) {
            showWarehouse(view, true, false);
            showCompanyAndAddress(view, true);
        } else {
            showWarehouse(view, false, false);
            showCompanyAndAddress(view, false);
        }

        if (!positions.isEmpty()) {
            showWarehouse(view, false, false);
        }
    }

    private void showWarehouse(final ViewDefinitionState view, final boolean from, final boolean to) {
        LookupComponent locationFromLookup = (LookupComponent) view.getComponentByReference(DocumentFields.LOCATION_FROM);
        locationFromLookup.setEnabled(from);

        LookupComponent locationToLookup = (LookupComponent) view.getComponentByReference(DocumentFields.LOCATION_TO);
        locationToLookup.setEnabled(to);
    }

    private void showCompanyAndAddress(final ViewDefinitionState view, final boolean visible) {
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(DocumentFields.COMPANY);
        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(DocumentFields.ADDRESS);

        companyLookup.setEnabled(visible);
        addressLookup.setEnabled(visible);
    }

    public void initializeDocument(final ViewDefinitionState view) {
        showFieldsByDocumentType(view);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        FormComponent documentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long documentId = documentForm.getEntityId();

        Entity document = documentForm.getPersistedEntityWithIncludedFormValues();
        DocumentState state = DocumentState.of(document);

        if (documentId == null) {
            changeAcceptButtonState(window, false);
            changePrintButtonState(window, false);
            changeFillResourceButtonState(window, false);
            changeCheckResourcesStockButtonState(window, false);

            changeAddMultipleResourcesButtonState(window, false);

            FieldComponent dateField = (FieldComponent) view.getComponentByReference(DocumentFields.TIME);
            FieldComponent userField = (FieldComponent) view.getComponentByReference(DocumentFields.USER);

            if (dateField.getFieldValue() == null) {
                dateField.setFieldValue(setDateToField(new Date()));
            }

            userField.setFieldValue(userService.getCurrentUserEntity().getId());
        } else if (DocumentState.DRAFT.equals(state)) {
            changeAcceptButtonState(window, true);
            changePrintButtonState(window, true);
            changeFillResourceButtonState(window, reservationsService.reservationsEnabledForDocumentPositions(document));
            changeCheckResourcesStockButtonState(window, DocumentType.isOutbound(document.getStringField(DocumentFields.TYPE))
                    && !reservationsService.reservationsEnabledForDocumentPositions(document));
            changeAddMultipleResourcesButtonState(window, DocumentType.isOutbound(document.getStringField(DocumentFields.TYPE)));
            if (pluginManager.isPluginEnabled(MOBILE_WMS)) {
                if (document.getBooleanField(DocumentFields.WMS)
                        && !REALIZED.equals(document.getStringField(DocumentFields.STATE_IN_WMS))) {
                    toggleRibbon(window, false);
                    changeFillResourceButtonState(window, false);
                    changeAddMultipleResourcesButtonState(window, false);
                } else {
                    toggleRibbon(window, true);
                }
            }
        } else if (DocumentState.ACCEPTED.equals(state)) {
            documentForm.setFormEnabled(false);
            toggleRibbon(window, false);
            changePrintButtonState(window, true);
            changeFillResourceButtonState(window, false);
            changeCheckResourcesStockButtonState(window, false);
            changeAddMultipleResourcesButtonState(window, false);
        }
    }

    private void toggleRibbon(final WindowComponent window, final boolean enable) {
        for (String actionItem : L_ACTIONS_ITEMS) {
            window.getRibbon().getGroupByName(L_ACTIONS).getItemByName(actionItem).setEnabled(enable);
            window.getRibbon().getGroupByName(L_ACTIONS).getItemByName(actionItem).requestUpdate(true);
        }

        changeAcceptButtonState(window, enable);
    }

    private void changeAcceptButtonState(final WindowComponent window, final boolean enable) {
        RibbonActionItem acceptRibbonActionItem = window.getRibbon().getGroupByName(L_STATE).getItemByName(L_ACCEPT);

        acceptRibbonActionItem.setEnabled(enable);
        acceptRibbonActionItem.requestUpdate(true);
    }

    private void changeCheckResourcesStockButtonState(final WindowComponent window, final boolean enable) {
        RibbonActionItem checkResourcesStockItem = window.getRibbon().getGroupByName("resourcesStock")
                .getItemByName("checkResourcesStock");

        checkResourcesStockItem.setEnabled(enable);
        checkResourcesStockItem.requestUpdate(true);
    }

    private void changeFillResourceButtonState(final WindowComponent window, final boolean enable) {
        RibbonActionItem fillResourcesItem = window.getRibbon().getGroupByName("resources").getItemByName("fillResources");

        fillResourcesItem.setEnabled(enable);
        fillResourcesItem.requestUpdate(true);
    }

    private void changeAddMultipleResourcesButtonState(final WindowComponent window, final boolean enable) {
        RibbonActionItem addMultipleResources = window.getRibbon().getGroupByName("resources")
                .getItemByName("addMultipleResources");

        addMultipleResources.setEnabled(enable);
        addMultipleResources.requestUpdate(true);
    }

    private void changePrintButtonState(final WindowComponent window, final boolean enable) {
        RibbonActionItem printRibbonActionItem = window.getRibbon().getGroupByName(L_PRINT).getItemByName(L_PRINT_PDF);

        printRibbonActionItem.setEnabled(enable);
        printRibbonActionItem.requestUpdate(true);
    }

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    private void lockNumberAndTypeChange(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        ComponentState typeComponent = view.getComponentByReference(DocumentFields.TYPE);

        if (documentForm.getEntityId() != null) {
            typeComponent.setEnabled(false);
        } else {
            typeComponent.setEnabled(true);
        }

        FieldComponent numberField = (FieldComponent) view.getComponentByReference(DocumentFields.NUMBER);
        numberField.setEnabled(false);
    }

    private void fetchNameAndNumberFromDatabase(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (documentForm.getEntityId() != null) {
            ComponentState numberField = view.getComponentByReference(DocumentFields.NUMBER);
            ComponentState nameField = view.getComponentByReference(DocumentFields.NAME);

            String nameFieldValue = (String) nameField.getFieldValue();
            String numberFieldValue = (String) numberField.getFieldValue();

            if (!numberFieldValue.contains("/")) {
                Entity document = getDocumentDD().get(documentForm.getEntityId());

                if (!numberFieldValue.contains("/")) {
                    numberField.setFieldValue(document.getField(DocumentFields.NUMBER));
                }

                if (Strings.isNullOrEmpty(nameFieldValue)) {
                    nameField.setFieldValue(document.getField(DocumentFields.NAME));
                }
            }
        }
    }

    private void setRibbonState(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonActionItem printDispositionOrderPdfRibbonActionItem = window.getRibbon().getGroupByName(L_PRINT)
                .getItemByName(L_PRINT_DISPOSITION_ORDER_PDF);
        RibbonActionItem openPositionsImportPageRibbonActionItem = window.getRibbon().getGroupByName(L_IMPORT)
                .getItemByName(L_OPEN_POSITIONS_IMPORT_PAGE);
        RibbonActionItem showProductAttributesActionItem = window.getRibbon().getGroupByName(L_ATTRIBUTES)
                .getItemByName(L_SHOW_PRODUCT_ATTRIBUTES);

        Entity document = documentForm.getEntity();
        String state = document.getStringField(DocumentFields.STATE);
        String documentType = document.getStringField(DocumentFields.TYPE);

        List<String> documentTypesWithDispositionOrder = Lists.newArrayList(DocumentType.TRANSFER.getStringValue(),
                DocumentType.INTERNAL_OUTBOUND.getStringValue(), DocumentType.RELEASE.getStringValue());
        List<String> documentTypesWithAdmission = Lists.newArrayList(DocumentType.RECEIPT.getStringValue(),
                DocumentType.INTERNAL_INBOUND.getStringValue());

        String errorMessage = null;
        String descriptionMessage = "materialFlowResources.documentDetails.window.ribbon.import.openPositionsImportPage.description";

        boolean isSaved = Objects.nonNull(documentForm.getEntityId());
        boolean isDraft = DocumentState.DRAFT.getStringValue().equals(state);
        boolean isDispositionOrder = documentTypesWithDispositionOrder.contains(documentType);
        boolean isAdmission = documentTypesWithAdmission.contains(documentType);

        if (isSaved) {
            if (Objects.isNull(documentType) || !isDispositionOrder) {
                errorMessage = "materialFlowResources.printDispositionOrderPdf.error";
            }
        }

        printDispositionOrderPdfRibbonActionItem.setEnabled(isSaved && isDispositionOrder);
        printDispositionOrderPdfRibbonActionItem.setMessage(errorMessage);
        printDispositionOrderPdfRibbonActionItem.requestUpdate(true);

        openPositionsImportPageRibbonActionItem.setEnabled(isSaved && isDraft && isAdmission);
        openPositionsImportPageRibbonActionItem.setMessage(descriptionMessage);
        openPositionsImportPageRibbonActionItem.requestUpdate(true);

        showProductAttributesActionItem.setEnabled(isSaved);
        showProductAttributesActionItem.requestUpdate(true);
    }

    public void fillAddressLookupCriteriaModifier(final ViewDefinitionState view) {
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(DocumentFields.COMPANY);
        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(DocumentFields.ADDRESS);

        Entity company = companyLookup.getEntity();

        FilterValueHolder filterValueHolder = addressLookup.getFilterValue();

        if (company == null) {
            filterValueHolder.remove(AddressCriteriaModifiers.L_COMPANY_ID);

            addressLookup.setFieldValue(null);
        } else {
            Long companyId = company.getId();

            if (filterValueHolder.has(AddressCriteriaModifiers.L_COMPANY_ID)) {
                Long oldCompanyId = filterValueHolder.getLong(AddressCriteriaModifiers.L_COMPANY_ID);

                if (!companyId.equals(oldCompanyId)) {
                    addressLookup.setFieldValue(null);
                }
            }

            filterValueHolder.put(AddressCriteriaModifiers.L_COMPANY_ID, companyId);
        }

        addressLookup.setFilterValue(filterValueHolder);
        addressLookup.requestComponentUpdateState();
    }

    private DataDefinition getDocumentDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);
    }

}
