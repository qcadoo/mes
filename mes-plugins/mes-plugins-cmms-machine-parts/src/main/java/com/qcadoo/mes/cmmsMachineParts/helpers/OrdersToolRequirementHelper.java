package com.qcadoo.mes.cmmsMachineParts.helpers;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.OrdersToolRequirementToolFields;
import com.qcadoo.mes.cmmsMachineParts.constants.TechnologyOperationComponentFieldsCMP;
import com.qcadoo.mes.cmmsMachineParts.constants.TechnologyOperationToolFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class OrdersToolRequirementHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> generateOrdersToolRequirementTools(final Entity ordersToolRequirement, final List<Entity> orders) {
        List<Entity> ordersToolRequirementTools = Lists.newArrayList();

        orders.forEach(order -> {
            Date dateFrom = order.getDateField(OrderFields.DATE_FROM);
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (Objects.nonNull(technology)) {
                List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

                operationComponents.forEach(technologyOperationComponent -> {
                    Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);
                    List<Entity> tools = technologyOperationComponent.getHasManyField(TechnologyOperationComponentFieldsCMP.TOOLS);

                    tools.forEach(technologyOperationTool -> {
                        BigDecimal quantity = technologyOperationTool.getDecimalField(TechnologyOperationToolFields.QUANTITY);

                        Entity ordersToolRequirementTool = createOrdersToolRequirementTool(dateFrom, order, operation, technologyOperationTool, quantity);

                        ordersToolRequirementTools.add(ordersToolRequirementTool);
                    });
                });
            }
        });

        return ordersToolRequirementTools;
    }

    private Entity createOrdersToolRequirementTool(final Date date, final Entity order, final Entity operation, final Entity technologyOperationTool, final BigDecimal quantity) {
        Entity ordersToolRequirementTool = getOrdersToolRequirementToolDD().create();

        ordersToolRequirementTool.setField(OrdersToolRequirementToolFields.DATE, date);
        ordersToolRequirementTool.setField(OrdersToolRequirementToolFields.ORDER, order);
        ordersToolRequirementTool.setField(OrdersToolRequirementToolFields.OPERATION, operation);
        ordersToolRequirementTool.setField(OrdersToolRequirementToolFields.TECHNOLOGY_OPERATION_TOOL, technologyOperationTool);
        ordersToolRequirementTool.setField(OrdersToolRequirementToolFields.QUANTITY, quantity);

        return ordersToolRequirementTool;
    }

    private DataDefinition getOrdersToolRequirementToolDD() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_ORDERS_TOOL_REQUIREMENT_TOOL);
    }

}
