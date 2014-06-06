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

    private static final List<String> RIBBON_ACTION_ITEM = Arrays.asList("saveBack", "saveNew", "save");

    private static final String STATE_GROUP = "state";

    private static final String ACCEPT_ITEM = "accept";

    private static final List<String> INBOUND_FIELDS = Arrays.asList("price", "batch", "productionDate", "expirationDate");
    
    public static final String FORM = "form";

    @Autowired
    private NumberGeneratorService numberGeneratorService;
    
    @Autowired
    private UserService userService;

    public void showPositionsAttributes(final ViewDefinitionState view) {

        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();

        String documentType = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.RECEIPT.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_INBOUND.getStringValue().equals(documentType)) {
            showInboundDocumentPositionsAttributesAndFillInUnit(view, true);
        } else {
            showInboundDocumentPositionsAttributesAndFillInUnit(view, false);
        }
    }

    private void showInboundDocumentPositionsAttributesAndFillInUnit(final ViewDefinitionState view, final boolean show) {

        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference("positions");
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            for (String fieldName : INBOUND_FIELDS) {
                FieldComponent field = positionForm.findFieldComponentByName(fieldName);
                field.setVisible(show);
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

    public void disableFormIfDocumentIsAccepted(final ViewDefinitionState view) {

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();
        DocumentState state = DocumentState.parseString(document.getStringField(DocumentFields.STATE));

        if (DocumentState.ACCEPTED.equals(state)) {
            formComponent.setEnabled(false);
            enableRibbon(window, false);
        }
    }

    private void enableRibbon(final WindowComponent window, final boolean enable) {
        for (String actionItem : RIBBON_ACTION_ITEM) {
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).setEnabled(enable);
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).requestUpdate(true);
        }
        RibbonActionItem acceptAction = (RibbonActionItem) window.getRibbon().getGroupByName(STATE_GROUP).getItemByName(ACCEPT_ITEM);
        acceptAction.setEnabled(enable);
        acceptAction.requestUpdate(true);
    }

    public void initializeDocument(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM);
        Long documentId = formComponent.getEntityId();
        if (documentId == null) {
            disableAcceptButton(window);
            numberGeneratorService.generateAndInsertNumber(view, MaterialFlowDocumentsConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowDocumentsConstants.MODEL_DOCUMENT, FORM, DocumentFields.NUMBER);
            FieldComponent date = (FieldComponent) view.getComponentByReference(DocumentFields.TIME);
            FieldComponent user = (FieldComponent) view.getComponentByReference(DocumentFields.USER);
            date.setFieldValue(setDateToField(new Date()));
            user.setFieldValue(userService.getCurrentUserEntity().getId());
        }
    }

    private void disableAcceptButton(WindowComponent window) {
        RibbonActionItem actionItem = (RibbonActionItem)  window.getRibbon().getGroupByName(STATE_GROUP).getItemByName(ACCEPT_ITEM);
        actionItem.setEnabled(false);
        actionItem.requestUpdate(true);
    }

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }
}
