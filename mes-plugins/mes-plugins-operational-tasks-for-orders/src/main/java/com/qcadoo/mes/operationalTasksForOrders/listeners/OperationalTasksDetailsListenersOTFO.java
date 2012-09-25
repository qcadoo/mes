package com.qcadoo.mes.operationalTasksForOrders.listeners;

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.mes.operationalTasksForOrders.hooks.OperationalTasksDetailsHooksOTFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksDetailsListenersOTFO {

    @Autowired
    private OperationalTasksDetailsHooksOTFO detailsHooks;

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_FORM = "form";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    public void disabledFieldWhenOrderTypeIsSelected(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        detailsHooks.disabledFieldWhenOrderTypeIsSelected(viewDefinitionState);
    }

    public void setProductionLineFromOrderAndClearOperation(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        Entity order = ((LookupComponent) viewDefinitionState.getComponentByReference(OperationalTasksOTFRFields.ORDER))
                .getEntity();
        LookupComponent technologyLookup = (LookupComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
        technologyLookup.setFieldValue(null);
        technologyLookup.requestComponentUpdateState();
        FieldComponent productionLine = (FieldComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksFields.PRODUCTION_LINE);
        if (order == null) {
            productionLine.setFieldValue(null);
        } else {
            productionLine.setFieldValue(order.getBelongsToField(OrderFields.PRODUCTION_LINE).getId());
        }
        productionLine.requestComponentUpdateState();
    }

    public void setOperationalNameAndDescription(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Entity techInstOperComp = ((LookupComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).getEntity();
        FieldComponent description = (FieldComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksFields.DESCRIPTION);
        FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference(OperationalTasksFields.NAME);
        if (techInstOperComp == null) {
            description.setFieldValue(null);
            name.setFieldValue(null);
        } else {
            description.setFieldValue(techInstOperComp.getStringField(TechnologyInstanceOperCompFields.COMMENT));
            name.setFieldValue(techInstOperComp.getBelongsToField(TechnologyInstanceOperCompFields.OPERATION).getStringField(
                    OperationFields.NAME));
        }
        description.requestComponentUpdateState();
        name.requestComponentUpdateState();
    }

    public final void showOperationalTasksWithOrder(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent operationTaskForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity operationTask = operationTaskForm.getEntity();

        if (operationTask.getId() == null) {
            return;
        }

        Entity order = operationTask.getBelongsToField("order");

        if (order == null) {
            return;
        }

        String orderNumber = order.getStringField(NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("orderNumber", orderNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "operationalTask.operationalTasks");

        String url = "../page/operationalTasks/operationalTasksList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOrder(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        Entity order = ((LookupComponent) viewState.getComponentByReference(OperationalTasksOTFRFields.ORDER)).getEntity();
        if (order == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", order.getId());

        String url = "../page/orders/orderDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public final void showOperationParameter(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Entity techInstOperComp = ((LookupComponent) viewState
                .getComponentByReference(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).getEntity();
        if (techInstOperComp == null) {
            return;
        }
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", techInstOperComp.getId());

        String url = "../page/technologies/technologyInstanceOperationComponentDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }
}
