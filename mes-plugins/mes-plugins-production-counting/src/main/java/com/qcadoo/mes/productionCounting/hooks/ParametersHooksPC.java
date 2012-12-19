package com.qcadoo.mes.productionCounting.hooks;

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ParametersHooksPC {

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

}
