package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrdersConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyOperationComponentInOrderListHooks {

    private static final String TECHNOLOGY = "technology";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setTechnologyNumber(final ViewDefinitionState view) {
        Entity order = dataDefinitionService.get(PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                ((FormComponent) view.getComponentByReference("form")).getEntityId());
        FieldComponent technology = (FieldComponent) view.getComponentByReference(TECHNOLOGY);
        Entity technologyEntity = order.getBelongsToField(TECHNOLOGY);
        if (technologyEntity != null) {
            technology.setFieldValue(technologyEntity.getStringField("number"));
        } else {
            technology.setFieldValue(null);
        }
        technology.requestComponentUpdateState();
    }
}
