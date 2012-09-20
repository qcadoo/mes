package com.qcadoo.mes.operationalTasksForOrders.listeners;

import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderDetailsListenersOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void deleteOperationTasksWhenTechnologyIsChanged(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        Entity order = form.getEntity().getDataDefinition().get(form.getEntityId());
        Entity technology = order.getBelongsToField(TECHNOLOGY);
        if (technology == null) {
            return;
        }
        Entity technologyFromLookup = ((LookupComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY)).getEntity();
        if (technologyFromLookup == null || !technologyFromLookup.equals(technology)) {
            deleteOperationTaskForOrder(order);
        }
    }

    private void deleteOperationTaskForOrder(final Entity order) {
        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        List<Entity> operationTasksList = operationTaskDD.find()
                .add(SearchRestrictions.belongsTo(OperationalTasksOTFRFields.ORDER, order)).list().getEntities();
        for (Entity operationalTask : operationTasksList) {
            operationTaskDD.delete(operationalTask.getId());
        }
    }
}
