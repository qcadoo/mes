package com.qcadoo.mes.productionCounting.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class TechnologyValidationServicePC {

    @Autowired
    private TechnologyService technologyService;

    public void validateTypeOfProductionRecordingForTechnology(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();
        if (technology != null && !stateChangeContext.getStatus().equals(StateChangeStatus.FAILURE)) {
            EntityTree tree = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
            Entity mainProduct = technologyService.getMainOutputProductComponent(tree.getRoot());
            String typeOfProductionRecording = technology.getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
            if (mainProduct.getBooleanField(OperationProductOutComponentFields.SET)
                    && typeOfProductionRecording.equals("03forEach")) {
                stateChangeContext.addValidationError("technologies.technology.technologyState.error.typeOfProductionRecording");
            }
        }
    }

    public void validateTypeOfProductionRecordingForOrder(final StateChangeContext stateChangeContext) {
        Entity order = stateChangeContext.getOwner();
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology != null && !stateChangeContext.getStatus().equals(StateChangeStatus.FAILURE)) {
            EntityTree tree = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
            Entity mainProduct = technologyService.getMainOutputProductComponent(tree.getRoot());
            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
            if (typeOfProductionRecording != null && mainProduct.getBooleanField(OperationProductOutComponentFields.SET)
                    && typeOfProductionRecording.equals("03forEach")) {
                stateChangeContext.addValidationError("technologies.technology.technologyState.error.typeOfProductionRecording");
            }
        }
    }
}
