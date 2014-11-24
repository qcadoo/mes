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
package com.qcadoo.mes.materialFlowResources.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.listeners.DocumentDetailsListeners;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class DocumentDetailsHooks {

    private static final String RIBBON_GROUP = "actions";

    private static final List<String> RIBBON_ACTION_ITEM = Arrays.asList("saveBack", "saveNew", "save", "delete", "copy");

    private static final String STATE_GROUP = "state";

    private static final String ACCEPT_ITEM = "accept";

    private static final List<String> INBOUND_FIELDS = Arrays.asList("price", "batch", "productionDate", "expirationDate");

    public static final String FORM = "form";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentDetailsListeners documentDetailsListeners;

    // fixme: refactor
    public void showFieldsByDocumentType(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();

        String documentType = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.RECEIPT.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_INBOUND.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, true);
            showWarehouse(view, false, true);
            enableAttributesADL(view, true);
        } else if (DocumentType.TRANSFER.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            showWarehouse(view, true, true);
            enableAttributesADL(view, false);
        } else if (DocumentType.RELEASE.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_OUTBOUND.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            showWarehouse(view, true, false);
            enableAttributesADL(view, false);
        } else {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            showWarehouse(view, false, false);
            enableAttributesADL(view, false);
        }
    }

    private void showWarehouse(final ViewDefinitionState view, boolean from, boolean to) {
        FieldComponent locationFrom = (FieldComponent) view.getComponentByReference("locationFrom");
        locationFrom.setEnabled(from);

        FieldComponent locationTo = (FieldComponent) view.getComponentByReference("locationTo");
        locationTo.setEnabled(to);
    }

    private void enableInboundDocumentPositionsAttributesAndFillInUnit(final ViewDefinitionState view, final boolean enabled) {

        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference("positions");
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            for (String fieldName : INBOUND_FIELDS) {
                FieldComponent field = positionForm.findFieldComponentByName(fieldName);
                field.setEnabled(enabled);

            }
            fillInUnit(positionForm);

        }
    }

    private void enableAttributesADL(final ViewDefinitionState view, final boolean enabled) {

        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference("positions");
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            AwesomeDynamicListComponent attributeADL = (AwesomeDynamicListComponent) positionForm
                    .findFieldComponentByName("additionalAttributes");
            for (FormComponent innerForm : attributeADL.getFormComponents()) {
                FieldComponent value = innerForm.findFieldComponentByName("value");
                value.setEnabled(enabled);
            }
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
            numberGeneratorService.generateAndInsertNumber(view, MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_DOCUMENT, FORM, DocumentFields.NUMBER);
            FieldComponent date = (FieldComponent) view.getComponentByReference(DocumentFields.TIME);
            FieldComponent user = (FieldComponent) view.getComponentByReference(DocumentFields.USER);
            if (date.getFieldValue() == null) {
                date.setFieldValue(setDateToField(new Date()));
            }
            user.setFieldValue(userService.getCurrentUserEntity().getId());
        } else if (DocumentState.DRAFT.equals(state)) {
            changeAcceptButtonState(window, true);
        } else if (DocumentState.ACCEPTED.equals(state)) {
            formComponent.setFormEnabled(false);
            disableADL(view);
            disableRibbon(window);
        }

    }

    private void disableADL(ViewDefinitionState view) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference("positions");
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            positionForm.setFormEnabled(false);
            enableAttributesADL(view, false);
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

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    public void setCriteriaModifiersParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(FORM);
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference("positions");
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            Entity position = positionForm.getPersistedEntityWithIncludedFormValues();
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            LookupComponent resourcesLookup = (LookupComponent) positionForm.findFieldComponentByName(PositionFields.RESOURCE);
            FilterValueHolder filter = resourcesLookup.getFilterValue();
            if (warehouseFrom != null) {
                filter.put("locationFrom", warehouseFrom.getId());
            }
            if (product != null) {
                filter.put("product", product.getId());
            }
            resourcesLookup.setFilterValue(filter);
        }
    }

}
