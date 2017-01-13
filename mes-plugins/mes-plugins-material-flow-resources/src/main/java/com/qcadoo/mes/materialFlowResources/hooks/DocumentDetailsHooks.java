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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
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
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class DocumentDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_ACTIONS = "actions";

    private static final String L_STATE = "state";

    private static final String L_PRINT = "print";

    private static final String L_ACCEPT = "accept";

    private static final String L_PRINT_PDF = "printPdf";

    private static final List<String> L_ACTIONS_ITEMS = Arrays.asList("saveBack", "saveNew", "save", "delete", "copy");

    public static final String L_PRINT_DISPOSITION_ORDER_PDF = "printDispositionOrderPdf";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationsService reservationsService;

    public void onBeforeRender(final ViewDefinitionState view) {
        initializeDocument(view);
        lockNumberAndTypeChange(view);
        fetchNameAndNumberFromDatabase(view);
        lockDispositionOrder(view);
        fillAddressLookupCriteriaModifier(view);
    }

    // fixme: refactor
    public void showFieldsByDocumentType(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

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

    private void showWarehouse(final ViewDefinitionState view, boolean from, boolean to) {
        LookupComponent locationFromLookup = (LookupComponent) view.getComponentByReference(DocumentFields.LOCATION_FROM);
        locationFromLookup.setEnabled(from);

        LookupComponent locationToLookup = (LookupComponent) view.getComponentByReference(DocumentFields.LOCATION_TO);
        locationToLookup.setEnabled(to);
    }

    private void showCompanyAndAddress(final ViewDefinitionState view, boolean visible) {
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(DocumentFields.COMPANY);
        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(DocumentFields.ADDRESS);

        companyLookup.setEnabled(visible);
        addressLookup.setEnabled(visible);
    }

    public void initializeDocument(final ViewDefinitionState view) {
        showFieldsByDocumentType(view);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long documentId = documentForm.getEntityId();

        Entity document = documentForm.getPersistedEntityWithIncludedFormValues();
        DocumentState state = DocumentState.of(document);

        if (documentId == null) {
            changeAcceptButtonState(window, false);
            changePrintButtonState(window, false);
            changeFillResourceButtonState(window, false);

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
        } else if (DocumentState.ACCEPTED.equals(state)) {
            documentForm.setFormEnabled(false);
            disableRibbon(window);
            changePrintButtonState(window, true);
            changeFillResourceButtonState(window, false);
        }
    }

    private void disableRibbon(final WindowComponent window) {
        for (String actionItem : L_ACTIONS_ITEMS) {
            window.getRibbon().getGroupByName(L_ACTIONS).getItemByName(actionItem).setEnabled(false);
            window.getRibbon().getGroupByName(L_ACTIONS).getItemByName(actionItem).requestUpdate(true);
        }

        changeAcceptButtonState(window, false);
    }

    private void changeAcceptButtonState(WindowComponent window, final boolean enable) {
        RibbonActionItem acceptRibbonActionItem = (RibbonActionItem) window.getRibbon().getGroupByName(L_STATE)
                .getItemByName(L_ACCEPT);

        acceptRibbonActionItem.setEnabled(enable);
        acceptRibbonActionItem.requestUpdate(true);
    }

    private void changeFillResourceButtonState(WindowComponent window, final boolean enable) {
        RibbonActionItem fillResourcesItem = (RibbonActionItem) window.getRibbon().getGroupByName("resources")
                .getItemByName("fillResources");

        fillResourcesItem.setEnabled(enable);
        fillResourcesItem.requestUpdate(true);
    }

    private void changePrintButtonState(WindowComponent window, final boolean enable) {
        RibbonActionItem printRibbonActionItem = (RibbonActionItem) window.getRibbon().getGroupByName(L_PRINT)
                .getItemByName(L_PRINT_PDF);

        printRibbonActionItem.setEnabled(enable);
        printRibbonActionItem.requestUpdate(true);
    }

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    private void lockNumberAndTypeChange(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

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
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

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

    private DataDefinition getDocumentDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);
    }

    private void lockDispositionOrder(ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonActionItem printDispositionOrderPdfRibbonActionItem = (RibbonActionItem) window.getRibbon().getGroupByName(L_PRINT)
                .getItemByName(L_PRINT_DISPOSITION_ORDER_PDF);

        String errorMessage = null;

        if (documentForm.getEntityId() != null) {
            Entity document = documentForm.getEntity();
            String documentType = document.getStringField(DocumentFields.TYPE);

            List<String> documentTypesWithDispositionOrder = Arrays.asList(DocumentType.TRANSFER.getStringValue(),
                    DocumentType.INTERNAL_OUTBOUND.getStringValue(), DocumentType.RELEASE.getStringValue());

            if (documentType == null || !documentTypesWithDispositionOrder.contains(documentType)) {
                errorMessage = "materialFlowResources.printDispositionOrderPdf.error";
            }
            if (document.getBooleanField(DocumentFields.IN_BUFFER)) {
                errorMessage = "materialFlowResources.printDispositionOrderPdf.errorInBuffer";
            }
        }

        printDispositionOrderPdfRibbonActionItem.setEnabled(errorMessage == null && documentForm.getEntityId() != null);
        printDispositionOrderPdfRibbonActionItem.setMessage(errorMessage);
        printDispositionOrderPdfRibbonActionItem.requestUpdate(true);
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

}
