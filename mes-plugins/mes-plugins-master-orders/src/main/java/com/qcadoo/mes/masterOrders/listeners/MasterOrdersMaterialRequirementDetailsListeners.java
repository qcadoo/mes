package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersMaterialRequirementFields;
import com.qcadoo.mes.masterOrders.helpers.MasterOrdersMaterialRequirementHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MasterOrdersMaterialRequirementDetailsListeners {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MasterOrdersMaterialRequirementHelper masterOrdersMaterialRequirementHelper;

    public void generateMasterOrdersMaterialRequirement(final ViewDefinitionState view, final ComponentState state,
                                                        final String[] args) {
        FormComponent masterOrdersMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent masterOrdersGrid = (GridComponent) view.getComponentByReference(MasterOrdersMaterialRequirementFields.MASTER_ORDERS);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(MasterOrdersMaterialRequirementFields.GENERATED);
        FieldComponent workerField = (FieldComponent) view.getComponentByReference(MasterOrdersMaterialRequirementFields.WORKER);
        FieldComponent dateField = (FieldComponent) view.getComponentByReference(MasterOrdersMaterialRequirementFields.DATE);

        List<Entity> masterOrders = masterOrdersGrid.getEntities();

        if (validateMasterOrdersMaterialRequirement(masterOrdersMaterialRequirementForm, masterOrders)) {
            workerField.setFieldValue(securityService.getCurrentUserName());
            dateField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            generatedCheckBox.setChecked(true);

            Entity masterOrdersMaterialRequirement = masterOrdersMaterialRequirementForm.getEntity();

            List<Entity> masterOrdersMaterialRequirementProducts = masterOrdersMaterialRequirementHelper
                    .generateMasterOrdersMaterialRequirementProducts(masterOrdersMaterialRequirement, masterOrders);

            masterOrdersMaterialRequirement.setField(MasterOrdersMaterialRequirementFields.MASTER_ORDERS_MATERIAL_REQUIREMENT_PRODUCTS,
                    masterOrdersMaterialRequirementProducts);

            masterOrdersMaterialRequirement = masterOrdersMaterialRequirement.getDataDefinition().save(masterOrdersMaterialRequirement);

            masterOrdersMaterialRequirementForm.setEntity(masterOrdersMaterialRequirement);

            view.addMessage("masterOrders.masterOrdersMaterialRequirement.generate.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("masterOrders.masterOrdersMaterialRequirement.generate.failure", ComponentState.MessageType.FAILURE);
        }
    }

    private boolean validateMasterOrdersMaterialRequirement(final FormComponent masterOrdersMaterialRequirementForm, final List<Entity> masterOrders) {
        boolean isValid = true;

        if (masterOrders.isEmpty()) {
            masterOrdersMaterialRequirementForm.addMessage("masterOrders.masterOrdersMaterialRequirement.masterOrders.empty", ComponentState.MessageType.FAILURE);

            isValid = false;
        }

        return isValid;
    }

}
