package com.qcadoo.mes.orders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessFields;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessListFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessService {

    private static final String L_TECHNOLOGICAL_PROCESS = "technologicalProcess";

    private static final String L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS = "divideOrderTechnologicalProcess";

    private static final String L_TECHNOLOGICAL_PROCESS_NAME = "technologicalProcessName";

    private static final String L_QUANTITY_UNIT = "quantityUnit";

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
                createOrderTechnologicalProcesses(order, technologicalProcessList, orderPack);
            }
        }
    }

    private List<Entity> filterOrderPacks(final Entity order) {
        List<Entity> filteredOrderPacks = Lists.newArrayList();

        List<Entity> orderPacks = order.getHasManyField(OrderFields.ORDER_PACKS);
        List<Entity> orderTechnologicalProcesses = order.getHasManyField(OrderFields.ORDER_TECHNOLOGICAL_PROCESSES);

        orderPacks.forEach(orderPack -> {
            if (orderTechnologicalProcesses.stream()
                    .noneMatch(orderTechnologicalProcess -> filterOrderPack(orderTechnologicalProcess, orderPack))) {
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

    private void createOrderTechnologicalProcesses(final Entity order, final Entity technologicalProcessList,
            final Entity orderPack) {
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
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS, technologicalProcess);
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.QUANTITY, quantity);

            orderTechnologicalProcess.getDataDefinition().save(orderTechnologicalProcess);
        }
    }

    public void updateRibbonState(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup technologicalProcessGroup = window.getRibbon().getGroupByName(L_TECHNOLOGICAL_PROCESS);
        RibbonActionItem divideOrderTechnologicalProcessActionItem = technologicalProcessGroup
                .getItemByName(L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS);

        Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);

        boolean isSaved = Objects.nonNull(orderTechnologicalProcess.getId());
        boolean isOrderStateValid = !checkOrderState(order);

        divideOrderTechnologicalProcessActionItem.setEnabled(isSaved && isOrderStateValid);
        divideOrderTechnologicalProcessActionItem.requestUpdate(true);
    }

    public boolean checkOrderState(final Entity order) {
        if (Objects.nonNull(order)) {
            String state = order.getStringField(OrderFields.STATE);

            return OrderStateStringValues.DECLINED.equals(state) || OrderStateStringValues.COMPLETED.equals(state)
                    || OrderStateStringValues.ABANDONED.equals(state);
        }

        return false;
    }

    public void setFormEnabled(final ViewDefinitionState view, final FormComponent orderTechnologicalProcessForm,
            final Entity orderTechnologicalProcess) {
        Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);

        boolean isSaved = Objects.nonNull(orderTechnologicalProcess.getId());
        boolean isOrderStateValid = !checkOrderState(order);

        orderTechnologicalProcessForm.setFormEnabled(isSaved && isOrderStateValid);
    }

    public void fillUnit(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference(L_QUANTITY_UNIT);

        Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.PRODUCT);

        if (Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);

            quantityUnit.setFieldValue(unit);
            quantityUnit.requestComponentUpdateState();
        }
    }

    public void fillTechnologicalProcessName(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent technologicalProcessNameField = (FieldComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_NAME);

        Entity technologicalProcess = orderTechnologicalProcess
                .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

        if (Objects.nonNull(technologicalProcess)) {
            technologicalProcessNameField.setFieldValue(technologicalProcess.getStringField(TechnologicalProcessFields.NAME));
            technologicalProcessNameField.requestComponentUpdateState();
        }
    }

    private DataDefinition getOrderTechnologicalProcessDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_TECHNOLOGICAL_PROCESS);
    }

}
