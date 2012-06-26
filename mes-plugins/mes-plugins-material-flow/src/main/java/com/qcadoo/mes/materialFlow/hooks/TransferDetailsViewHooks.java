package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Component
public class TransferDetailsViewHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void checkIfTransferHasTransformations(final ViewDefinitionState state) {
        String number = (String) state.getComponentByReference(NUMBER).getFieldValue();

        if (number == null) {
            return;
        }

        Entity transfer = dataDefinitionService
                .get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER).find()
                .add(SearchRestrictions.eq(NUMBER, number)).uniqueResult();

        if (transfer == null) {
            return;
        }

        if (transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION) != null
                || transfer.getBelongsToField(TransferFields.TRANSFORMATIONS_PRODUCTION) != null) {
            FieldComponent type = (FieldComponent) state.getComponentByReference(TYPE);
            FieldComponent date = (FieldComponent) state.getComponentByReference(TIME);
            FieldComponent locationTo = (FieldComponent) state.getComponentByReference(LOCATION_TO);
            FieldComponent locationFrom = (FieldComponent) state.getComponentByReference(LOCATION_FROM);
            FieldComponent staff = (FieldComponent) state.getComponentByReference(STAFF);

            type.setEnabled(false);
            date.setEnabled(false);
            locationTo.setEnabled(false);
            locationFrom.setEnabled(false);
            staff.setEnabled(false);
        }
    }
}
