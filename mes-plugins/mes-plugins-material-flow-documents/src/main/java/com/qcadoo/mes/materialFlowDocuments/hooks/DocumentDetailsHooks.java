package com.qcadoo.mes.materialFlowDocuments.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentState;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentType;
import com.qcadoo.mes.materialFlowDocuments.constants.MaterialFlowDocumentsConstants;
import com.qcadoo.mes.materialFlowDocuments.constants.PositionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
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

    public void showFieldsByDocumentType(final ViewDefinitionState view) {

        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();

        String documentType = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.RECEIPT.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_INBOUND.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, true);
            showWarehouse(view, false, true);
        } else if(DocumentType.TRANSFER.getStringValue().equals(documentType)){
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            showWarehouse(view, true, true);
        } else if(DocumentType.RELEASE.getStringValue().equals(documentType) ||
                DocumentType.INTERNAL_OUTBOUND.getStringValue().equals(documentType)) {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            showWarehouse(view, true, false);
        } else {
            enableInboundDocumentPositionsAttributesAndFillInUnit(view, false);
            showWarehouse(view, false, false);
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
                field.setFieldValue(null);
            }
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

        position.setField(PositionFields.UNIT, unit);
        positionForm.setEntity(position);
    }

    public void initializeDocument(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Long documentId = formComponent.getEntityId();
        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();
        DocumentState state = DocumentState.parseString(document.getStringField(DocumentFields.STATE));

        if (documentId == null) {
            changeAcceptButtonState(window, false);
            numberGeneratorService.generateAndInsertNumber(view, MaterialFlowDocumentsConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowDocumentsConstants.MODEL_DOCUMENT, FORM, DocumentFields.NUMBER);
            FieldComponent date = (FieldComponent) view.getComponentByReference(DocumentFields.TIME);
            FieldComponent user = (FieldComponent) view.getComponentByReference(DocumentFields.USER);
            date.setFieldValue(setDateToField(new Date()));
            user.setFieldValue(userService.getCurrentUserEntity().getId());
        } else if (DocumentState.DRAFT.equals(state)){
            changeAcceptButtonState(window, true);
        } else if (DocumentState.ACCEPTED.equals(state)) {
            formComponent.setFormEnabled(false);
            disableRibbon(window);
        }
    }

    private void disableRibbon(final WindowComponent window) {
        for (String actionItem : RIBBON_ACTION_ITEM) {
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).setEnabled(false);
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).requestUpdate(true);
        }
        changeAcceptButtonState(window, false);
    }

    private void changeAcceptButtonState(WindowComponent window, final boolean enable) {
        RibbonActionItem actionItem = (RibbonActionItem)  window.getRibbon().getGroupByName(STATE_GROUP).getItemByName(ACCEPT_ITEM);
        actionItem.setEnabled(enable);
        actionItem.requestUpdate(true);
    }

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }
}
