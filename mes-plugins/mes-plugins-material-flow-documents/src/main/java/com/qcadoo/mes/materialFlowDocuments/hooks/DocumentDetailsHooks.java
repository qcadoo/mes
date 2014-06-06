package com.qcadoo.mes.materialFlowDocuments.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentState;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentType;
import com.qcadoo.mes.materialFlowDocuments.constants.PositionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;

@Service
public class DocumentDetailsHooks {

    private static final String RIBBON_GROUP = "actions";

    private static final List<String> RIBBON_ACTION_ITEM = Arrays.asList("saveBack", "saveNew", "save");

    private static final String STATE_GROUP = "state";

    private static final String ACCEPT_ITEM = "accept";

    private static final List<String> INBOUND_FIELDS = Arrays.asList("price", "batch", "productionDate", "expirationDate");

    public void showPositionsAttributes(final ViewDefinitionState view) {

        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
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
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
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
        window.getRibbon().getGroupByName(STATE_GROUP).getItemByName(ACCEPT_ITEM).setEnabled(enable);
        window.requestRibbonRender();
    }
}
