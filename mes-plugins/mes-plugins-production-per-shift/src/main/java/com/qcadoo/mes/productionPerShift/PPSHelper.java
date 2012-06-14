package com.qcadoo.mes.productionPerShift;

import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_TYPE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class PPSHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity getTiocFromOperationLookup(final ViewDefinitionState viewState) {
        ComponentState operationLookup = viewState.getComponentByReference("productionPerShiftOperation");
        Long id = (Long) operationLookup.getFieldValue();
        Entity tioc = null;
        if (id != null) {
            tioc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(id);
        }
        return tioc;
    }

    public Entity getOrderFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("order");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) lookup.getFieldValue());
    }

    public Entity getShiftFromLookup(final FieldComponent shift) {
        if (!(shift.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).get(
                (Long) shift.getFieldValue());
    }

    public boolean shouldHasCorrections(final ViewDefinitionState viewState) {
        return ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue().equals(
                PlannedProgressType.CORRECTED.getStringValue());
    }
}
