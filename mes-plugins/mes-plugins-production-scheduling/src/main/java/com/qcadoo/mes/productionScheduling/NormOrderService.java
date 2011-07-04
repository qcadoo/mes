package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class NormOrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    private void checkRequiredField(final Entity entity, final String field) {
        if (entity.getField(field) == null) {
            entity.addError(entity.getDataDefinition().getField(field), "qcadooView.validate.field.error.missing");
        }
    }

    @Transactional
    public void createTechnologyInstanceForOrder(final DataDefinition dataDefinition, final Entity entity) {
        DataDefinition orderOperationComponentDD = dataDefinitionService.get("productionScheduling", "orderOperationComponent");

        EntityTree orderOperationComponents = entity.getTreeField("orderOperationComponents");

        Entity technology = entity.getBelongsToField("technology");

        if (technology == null) {
            if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
                orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
            }
            return;
        }

        if (orderOperationComponents != null && orderOperationComponents.size() > 0
                && orderOperationComponents.getRoot().getBelongsToField("technology").getId().equals(technology.getId())) {
            return;
        }

        if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
            orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
        }

        EntityTree operationComponents = technology.getTreeField("operationComponents");

        entity.setField("orderOperationComponents", Collections.singletonList(createOrderOperationComponent(
                operationComponents.getRoot(), entity, technology, null, orderOperationComponentDD)));
    }

    private Entity createOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final Entity parent, final DataDefinition orderOperationComponentDD) {
        Entity orderOperationComponent = orderOperationComponentDD.create();

        orderOperationComponent.setField("order", order);
        orderOperationComponent.setField("technology", technology);
        orderOperationComponent.setField("parent", parent);

        if ("operation".equals(operationComponent.getField("entityType"))) {
            createOrCopyOrderOperationComponent(operationComponent, order, technology, orderOperationComponentDD,
                    orderOperationComponent);
        } else {
            Entity referenceTechnology = operationComponent.getBelongsToField("referenceTechnology");
            createOrCopyOrderOperationComponent(referenceTechnology.getTreeField("operationComponents").getRoot(), order,
                    technology, orderOperationComponentDD, orderOperationComponent);
        }

        return orderOperationComponent;
    }

    private void createOrCopyOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final DataDefinition orderOperationComponentDD, final Entity orderOperationComponent) {
        orderOperationComponent.setField("operation", operationComponent.getBelongsToField("operation"));
        orderOperationComponent.setField("technologyOperationComponent", operationComponent);
        orderOperationComponent.setField("priority", operationComponent.getField("priority"));
        orderOperationComponent.setField("entityType", "operation");
        orderOperationComponent.setField("tpz", operationComponent.getField("tpz"));
        orderOperationComponent.setField("tj", operationComponent.getField("tj"));
        orderOperationComponent.setField("countRealized",
                operationComponent.getField("countRealized") != null ? operationComponent.getField("countRealized") : "01all");
        orderOperationComponent.setField("countMachine", operationComponent.getField("countMachine"));
        orderOperationComponent.setField("timeNextOperation", operationComponent.getField("timeNextOperation"));

        List<Entity> newOrderOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newOrderOperationComponents.add(createOrderOperationComponent(child, order, technology, orderOperationComponent,
                    orderOperationComponentDD));
        }

        orderOperationComponent.setField("children", newOrderOperationComponents);
    }

    public void showOrderParameters(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/productionScheduling/orderOperationComponentList.html?context={\"form.id\":\"" + orderId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void showOperationsGantt(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        scheduleOrder(orderId);

        if (orderId != null) {
            String url = "../page/productionScheduling/ganttOrderOperationsCalendar.html?context={\"gantt.orderId\":\"" + orderId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    private void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get("orders", "order").get(orderId);

        if (order == null) {
            return;
        }

        DataDefinition dataDefinition = dataDefinitionService.get("productionScheduling", "orderOperationComponent");

        List<Entity> operations = dataDefinition.find().add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        Date orderStartDate = null;

        if (order.getField("effectiveDateFrom") != null) {
            orderStartDate = (Date) order.getField("effectiveDateFrom");
        } else if (order.getField("dateFrom") != null) {
            orderStartDate = (Date) order.getField("dateFrom");
        } else {
            return;
        }

        for (Entity operation : operations) {
            Integer offset = (Integer) operation.getField("operationOffSet");
            Integer duration = (Integer) operation.getField("effectiveOperationRealizationTime");

            operation.setField("effectiveDateFrom", null);
            operation.setField("effectiveDateTo", null);

            if (offset == null || duration == null || duration.equals(0)) {
                continue;
            }

            if (offset == 0) {
                offset = 1;
            }

            Date dateFrom = shiftsService.findDateToForOrder(orderStartDate, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = shiftsService.findDateToForOrder(orderStartDate, offset + duration);

            if (dateTo == null) {
                continue;
            }

            operation.setField("effectiveDateFrom", dateFrom);
            operation.setField("effectiveDateTo", dateTo);
        }

        for (Entity operation : operations) {
            dataDefinition.save(operation);
        }
    }

    public void changeCountRealized(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        // ignore
    }

    public void disableComponents(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpz = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tj = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");

        tpz.setEnabled(true);
        tpz.setRequired(true);
        tj.setEnabled(true);
        tj.setRequired(true);
        countRealized.setEnabled(true);
        countRealized.setRequired(true);

        if ("02specified".equals(countRealized.getFieldValue())) {
            countMachine.setEnabled(true);
            countMachine.setRequired(true);
            if (countMachine.getFieldValue() == null || !StringUtils.hasText(String.valueOf(countMachine.getFieldValue()))) {
                countMachine.setFieldValue("1");
            }
        } else {
            countMachine.setEnabled(false);
            countMachine.setRequired(false);
        }

        timeNextOperation.setEnabled(true);
        timeNextOperation.setRequired(true);
    }

}
