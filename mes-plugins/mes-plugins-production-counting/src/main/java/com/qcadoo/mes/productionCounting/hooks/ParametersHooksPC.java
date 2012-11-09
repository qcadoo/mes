package com.qcadoo.mes.productionCounting.hooks;

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ParametersHooksPC {

    @Autowired
    private ParameterService parameterService;

    public void checkIfTypeIsCumulatedAndRegisterPieceworkIsFalse(final ViewDefinitionState viewDefinitionState) {
        String typeOfProductionRecording = ((FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING)).getFieldValue().toString();
        FieldComponent registerPiecework = (FieldComponent) viewDefinitionState.getComponentByReference(REGISTER_PIECEWORK);
        if (typeOfProductionRecording != null
                && typeOfProductionRecording.equals(TypeOfProductionRecording.CUMULATED.getStringValue())) {
            registerPiecework.setFieldValue(false);
            registerPiecework.setEnabled(false);
        } else {
            registerPiecework.setEnabled(true);
        }
        registerPiecework.requestComponentUpdateState();
    }

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState) {
        Entity parameter = parameterService.getParameter();
        if (parameter == null || parameter.getField(TYPE_OF_PRODUCTION_RECORDING) == null) {
            FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                    .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
            typeOfProductionRecording.setFieldValue(TypeOfProductionRecording.CUMULATED.getStringValue());
            typeOfProductionRecording.requestComponentUpdateState();
        }
        if (parameter == null || parameter.getField(REGISTER_PIECEWORK) == null) {
            FieldComponent registerPiecework = (FieldComponent) viewDefinitionState.getComponentByReference(REGISTER_PIECEWORK);
            registerPiecework.setFieldValue(false);
            registerPiecework.requestComponentUpdateState();
        }
        for (String componentReference : Arrays.asList(REGISTER_QUANTITY_IN_PRODUCT, OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT,
                OrderFieldsPC.REGISTER_PRODUCTION_TIME, OrderFieldsPC.JUST_ONE, OrderFieldsPC.ALLOW_TO_CLOSE,
                OrderFieldsPC.AUTO_CLOSE_ORDER)) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (parameter == null || parameter.getField(componentReference) == null) {
                component.setFieldValue(true);
                component.requestComponentUpdateState();
            }
        }

    }
}
