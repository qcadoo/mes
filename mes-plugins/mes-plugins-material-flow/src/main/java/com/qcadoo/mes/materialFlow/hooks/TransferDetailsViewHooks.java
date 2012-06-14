package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_TO;
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

    public void checkIfTransferHasTransformation(final ViewDefinitionState state) {
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
            FieldComponent stockAreasTo = (FieldComponent) state.getComponentByReference(STOCK_AREAS_TO);
            FieldComponent stockAreasFrom = (FieldComponent) state.getComponentByReference(STOCK_AREAS_FROM);
            FieldComponent staff = (FieldComponent) state.getComponentByReference(STAFF);
            type.setEnabled(false);
            date.setEnabled(false);
            stockAreasTo.setEnabled(false);
            stockAreasFrom.setEnabled(false);
            staff.setEnabled(false);
        }
    }
}
