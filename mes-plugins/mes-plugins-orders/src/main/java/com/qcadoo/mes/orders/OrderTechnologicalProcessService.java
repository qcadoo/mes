package com.qcadoo.mes.orders;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessListFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderTechnologicalProcessService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public boolean generateOrderTechnologicalProcesses(final Entity order) {
        boolean isCreated = false;

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            List<Entity> technologyOperationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

            if (parameterService.getParameter().getBooleanField(ParameterFieldsO.INCLUDE_PACKS_GENERATING_PROCESSES_FOR_ORDER)) {
                List<Entity> orderPacks = filterOrderPacks(order);

                for (Entity orderPack : orderPacks) {
                    createOrderTechnologicalProcesses(order, technologyOperationComponents, orderPack);

                    isCreated = true;
                }
            } else {
                List<Entity> orderTechnologicalProcesses = order.getHasManyField(OrderFields.ORDER_TECHNOLOGICAL_PROCESSES);

                if (orderTechnologicalProcesses.isEmpty()) {
                    createOrderTechnologicalProcesses(order, technologyOperationComponents, null);

                    isCreated = true;
                }
            }
        }

        return isCreated;
    }

    private void createOrderTechnologicalProcesses(final Entity order, final List<Entity> technologyOperationComponents,
            final Entity orderPack) {
        for (Entity technologyOperationComponent : technologyOperationComponents) {
            Entity technologicalProcessList = technologyOperationComponent
                    .getBelongsToField(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST);

            if (Objects.nonNull(technologicalProcessList)) {
                createOrderTechnologicalProcesses(order, technologyOperationComponent, technologicalProcessList, orderPack);
            }
        }
    }

    private List<Entity> filterOrderPacks(final Entity order) {
        List<Entity> filteredOrderPacks = Lists.newArrayList();

        List<Entity> orderPacks = getOrderPackDD().find().add(SearchRestrictions.belongsTo(OrderPackFields.ORDER, order)).list()
                .getEntities();
        List<Entity> orderTechnologicalProcesses = order.getHasManyField(OrderFields.ORDER_TECHNOLOGICAL_PROCESSES);

        orderPacks.forEach(orderPack -> {
            if (orderTechnologicalProcesses.stream().noneMatch(
                    orderTechnologicalProcess -> filterOrderPack(orderTechnologicalProcess, orderPack))) {
                filteredOrderPacks.add(orderPack);
            }
        });

        return filteredOrderPacks;
    }

    private boolean filterOrderPack(final Entity orderTechnologicalProcess, final Entity orderedPack) {
        Entity orderTechnologicalProcessOrderPack = orderTechnologicalProcess
                .getBelongsToField(OrderTechnologicalProcessFields.ORDER_PACK);

        return Objects.nonNull(orderTechnologicalProcessOrderPack)
                && orderTechnologicalProcessOrderPack.getId().equals(orderedPack.getId());
    }

    private void createOrderTechnologicalProcesses(Entity order, final Entity technologyOperationComponent,
            final Entity technologicalProcessList, final Entity orderPack) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        Entity operation = technologicalProcessList.getBelongsToField(TechnologicalProcessListFields.OPERATION);
        List<Entity> technologicalProcessComponents = technologicalProcessList
                .getHasManyField(TechnologicalProcessListFields.TECHNOLOGICAL_PROCESS_COMPONENTS);

        BigDecimal quantity;

        if (Objects.nonNull(orderPack)) {
            quantity = orderPack.getDecimalField(OrderPackFields.QUANTITY);
        } else {
            quantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        }

        for (Entity technologicalProcessComponent : technologicalProcessComponents) {
            Entity technologicalProcess = technologicalProcessComponent
                    .getBelongsToField(TechnologicalProcessComponentFields.TECHNOLOGICAL_PROCESS);

            Entity orderTechnologicalProcess = getOrderTechnologicalProcessDD().create();

            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.ORDER_PACK, orderPack);
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.ORDER, order);
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.PRODUCT, product);
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.OPERATION, operation);
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent);
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS, technologicalProcess);
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.QUANTITY, quantity);

            orderTechnologicalProcess.getDataDefinition().save(orderTechnologicalProcess);
        }
    }

    public boolean checkOrderState(final Entity order) {
        if (Objects.nonNull(order)) {
            String state = order.getStringField(OrderFields.STATE);

            return OrderStateStringValues.DECLINED.equals(state) || OrderStateStringValues.COMPLETED.equals(state)
                    || OrderStateStringValues.ABANDONED.equals(state);
        }

        return false;
    }

    public Entity getOrderTechnologicalProcess(final Long orderTechnologicalProcessId) {
        return getOrderTechnologicalProcessDD().get(orderTechnologicalProcessId);
    }

    public DataDefinition getOrderTechnologicalProcessDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_TECHNOLOGICAL_PROCESS);
    }

    public DataDefinition getOrderPackDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK);
    }

}
