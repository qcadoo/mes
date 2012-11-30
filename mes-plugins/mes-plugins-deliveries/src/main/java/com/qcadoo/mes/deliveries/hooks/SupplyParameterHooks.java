package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DefaultAddressType.OTHER;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.DEFAULT_ADDRESS;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.OTHER_ADDRESS;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class SupplyParameterHooks {

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        setFieldsVisibleAndRequired(view);
    }

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view) {
        FieldComponent defaultAddress = (FieldComponent) view.getComponentByReference(DEFAULT_ADDRESS);

        boolean selectForAddress = OTHER.getStringValue().equals(defaultAddress.getFieldValue());

        changeFieldsState(view, OTHER_ADDRESS, selectForAddress);
    }

    private void changeFieldsState(final ViewDefinitionState view, final String fieldName, final boolean selectForAddress) {
        FieldComponent field = (FieldComponent) view.getComponentByReference(fieldName);
        field.setVisible(selectForAddress);
        field.setRequired(selectForAddress);

        if (!selectForAddress) {
            field.setFieldValue(null);
        }

        field.requestComponentUpdateState();
    }
}
