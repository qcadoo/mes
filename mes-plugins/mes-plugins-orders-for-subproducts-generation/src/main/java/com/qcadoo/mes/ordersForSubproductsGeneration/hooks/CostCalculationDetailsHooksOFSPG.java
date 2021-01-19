package com.qcadoo.mes.ordersForSubproductsGeneration.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.CostCalculationFieldsOFSPG;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CostCalculationDetailsHooksOFSPG {

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() == null) {
            CheckBoxComponent fieldComponent = (CheckBoxComponent) view.getComponentByReference("includeComponents");
            boolean check = parameterService.getParameter().getBooleanField("includeComponents");
            fieldComponent.setChecked(check);
            fieldComponent.requestComponentUpdateState();
        }
        enableSaveNominalCostForComponentButton(view);
    }

    private void enableSaveNominalCostForComponentButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem saveNominalCostsForComponent = window.getRibbon()
                .getGroupByName(CostCalculationFieldsOFSPG.SAVE_COSTS_EXTENSION)
                .getItemByName(CostCalculationFieldsOFSPG.NOMINAL_COSTS_FOR_COMPONENTS);
        CheckBoxComponent generatedField = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.GENERATED);
        CheckBoxComponent includeComponents = (CheckBoxComponent) view
                .getComponentByReference(CostCalculationFields.INCLUDE_COMPONENTS);
        includeComponents.setEnabled(!generatedField.isChecked());
        includeComponents.requestComponentUpdateState();
        boolean enable = false;

        if (generatedField.isChecked() && includeComponents.isChecked()) {
            enable = true;
        }
        saveNominalCostsForComponent.setEnabled(enable);
        saveNominalCostsForComponent.requestUpdate(true);
    }
}
