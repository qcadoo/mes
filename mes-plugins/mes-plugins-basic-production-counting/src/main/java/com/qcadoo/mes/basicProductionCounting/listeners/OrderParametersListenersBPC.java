package com.qcadoo.mes.basicProductionCounting.listeners;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ParameterFieldsBPC;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderParametersListenersBPC {

    private static final Logger LOG = LoggerFactory.getLogger(OrderParametersListenersBPC.class);

    public void onChangeLockProductionProgress(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        ComponentState lockOrderPlannedQuantityCheckbox = viewState
                .getComponentByReference(ParameterFieldsO.BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER);
        if (lockOrderPlannedQuantityCheckbox == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("orderParameters view: can't find component with reference='%s'",
                        ParameterFieldsO.BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER));
            }
            return;
        }
        String lockProgressStringValue = (String) componentState.getFieldValue();
        if (lockOrderPlannedQuantityCheckbox.isEnabled() && StringUtils.isNotBlank(lockProgressStringValue)
                && lockProgressStringValue.equals("1")) {
            lockOrderPlannedQuantityCheckbox.setFieldValue("1");
        }
    }

    public void onChangeLockOrderPlannedQuantity(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        ComponentState lockProgressCheckbox = viewState.getComponentByReference(ParameterFieldsBPC.LOCK_PRODUCTION_PROGRESS);
        String lockOrderPlannedQuantityStringValue = (String) componentState.getFieldValue();
        if (StringUtils.isBlank(lockOrderPlannedQuantityStringValue) || !lockOrderPlannedQuantityStringValue.equals("1")) {
            lockProgressCheckbox.setFieldValue(null);
        }
    }

}
