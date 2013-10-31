package com.qcadoo.mes.basicProductionCounting.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ParameterFieldsBPC;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;

@Service
public class OrderParametersListenersBPC {

    private static final Logger LOG = LoggerFactory.getLogger(OrderParametersListenersBPC.class);

    public void onChangeLockProductionProgress(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent lockProductionProgressCheckBox = (CheckBoxComponent) componentState;
        CheckBoxComponent lockOrderPlannedQuantityCheckBox = (CheckBoxComponent) viewState
                .getComponentByReference(ParameterFieldsO.BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER);

        if (lockOrderPlannedQuantityCheckBox == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("orderParameters view: can't find component with reference='%s'",
                        ParameterFieldsO.BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER));
            }
            return;
        }
        if (lockOrderPlannedQuantityCheckBox.isEnabled() && lockProductionProgressCheckBox.isChecked()) {
            lockOrderPlannedQuantityCheckBox.setChecked(true);
        }
    }

    public void onChangeLockOrderPlannedQuantity(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent lockOrderPlannedQuantityCheckBox = (CheckBoxComponent) componentState;
        CheckBoxComponent lockProgressCheckBox = (CheckBoxComponent) viewState
                .getComponentByReference(ParameterFieldsBPC.LOCK_PRODUCTION_PROGRESS);
        if (!lockOrderPlannedQuantityCheckBox.isChecked()) {
            lockProgressCheckBox.setChecked(false);
        }
    }

}
