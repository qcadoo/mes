/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.workPlans.pdf.document.order;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.ParameterInputColumnFields;
import com.qcadoo.mes.workPlans.constants.ParameterOutputColumnFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanOrderColumnFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;

@Service
public class WorkPlanColumnServiceImpl implements WorkPlanColumnService {

    public static final String L_IDENTIFIER = "identifier";

    public static final String L_ALIGNMENT = "alignment";

    private final ApplicationContext applicationContext;

    private final EntityTreeUtilsService entityTreeUtilsService;

    private final ParameterService parameterService;

    @Autowired
    public WorkPlanColumnServiceImpl(final ApplicationContext applicationContext,
            final EntityTreeUtilsService entityTreeUtilsService, final ParameterService parameterService) {
        this.applicationContext = applicationContext;
        this.entityTreeUtilsService = entityTreeUtilsService;
        this.parameterService = parameterService;
    }

    @Override
    public Map<OrderColumn, ColumnAlignment> getOrderColumns(final Entity workPlan) {
        Map<OrderColumn, ColumnAlignment> orderColumnWithAlignment = Maps.newLinkedHashMap();

        Map<String, OrderColumn> identifierOrderColumn = applicationContext.getBeansOfType(OrderColumn.class);

        List<Entity> columns = getWorkPlanOrderColumns(workPlan);

        for (Entity column : columns) {
            Entity columnForOrders = getColumnForOrders(column);

            String identifier = getIdentifier(columnForOrders);
            ColumnAlignment alignment = getColumnAlignment(columnForOrders);

            OrderColumn key = identifierOrderColumn.get(identifier);

            if (key != null) {
                orderColumnWithAlignment.put(key, alignment);
            }
        }

        return orderColumnWithAlignment;
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationProductInputColumns(final Entity workPlan) {
        return getOperationProductColumns(workPlan, ProductDirection.IN);
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationProductOutputColumns(final Entity workPlan) {
        return getOperationProductColumns(workPlan, ProductDirection.OUT);
    }

    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationProductColumns(final Entity workPlan,
            final ProductDirection productDirection) {
        Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdWithOperationProductColumnAndAlignment = Maps
                .newHashMap();

        Map<String, OperationProductColumn> identifierOperationProductColumn = applicationContext
                .getBeansOfType(OperationProductColumn.class);

        List<Entity> orders = getWorkPlanOrders(workPlan);

        for (Entity order : orders) {
            Entity technology = getOrderTechnology(order);

            List<Entity> operationComponents = getSortedTechnologyOperationComponents(technology);

            for (Entity operationComponent : operationComponents) {
                List<Entity> columns = getOperationProductColumns(productDirection);

                Map<OperationProductColumn, ColumnAlignment> operationProductColumnWithAlignment = Maps.newLinkedHashMap();

                for (Entity column : columns) {
                    String identifier = getIdentifier(column);
                    ColumnAlignment alignment = getColumnAlignment(column);

                    OperationProductColumn key = identifierOperationProductColumn.get(identifier);

                    if (key != null) {
                        operationProductColumnWithAlignment.put(key, alignment);
                    }
                }

                operationComponentIdWithOperationProductColumnAndAlignment.put(operationComponent.getId(),
                        operationProductColumnWithAlignment);
            }
        }

        return operationComponentIdWithOperationProductColumnAndAlignment;
    }

    private List<Entity> getWorkPlanOrderColumns(final Entity workPlan) {
        return workPlan.getHasManyField(WorkPlanFields.WORK_PLAN_ORDER_COLUMNS).find()
                .addOrder(SearchOrders.asc(WorkPlanOrderColumnFields.SUCCESSION)).list().getEntities();
    }

    private Entity getColumnForOrders(final Entity workPlanOrderColumn) {
        return workPlanOrderColumn.getBelongsToField(WorkPlanOrderColumnFields.COLUMN_FOR_ORDERS);
    }

    private List<Entity> getWorkPlanOrders(final Entity workPlan) {
        return workPlan.getManyToManyField(WorkPlanFields.ORDERS);
    }

    private Entity getOrderTechnology(final Entity order) {
        return order.getBelongsToField(OrderFields.TECHNOLOGY);
    }

    private List<Entity> getSortedTechnologyOperationComponents(final Entity technology) {
        return entityTreeUtilsService.getSortedEntities(getTechnologyOperationComponents(technology));
    }

    private EntityTree getTechnologyOperationComponents(final Entity technology) {
        return technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
    }

    private List<Entity> getOperationProductColumns(final ProductDirection productDirection) {
        String columnDefinitionModel;

        List<Entity> columnComponents;

        if (ProductDirection.IN.equals(productDirection)) {
            columnComponents = getParameterInputColumns();
            columnDefinitionModel = ParameterInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS;
        } else if (ProductDirection.OUT.equals(productDirection)) {
            columnComponents = getParameterOutputColumns();
            columnDefinitionModel = ParameterOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS;
        } else {
            throw new IllegalStateException("Wrong product direction");
        }

        List<Entity> columns = Lists.newLinkedList();

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField(columnDefinitionModel);

            columns.add(columnDefinition);
        }

        return columns;
    }

    private List<Entity> getParameterInputColumns() {
        return parameterService.getParameter().getHasManyField(ParameterFieldsWP.PARAMETER_INPUT_COLUMNS).find()
                .addOrder(SearchOrders.asc(ParameterInputColumnFields.SUCCESSION)).list().getEntities();
    }

    private List<Entity> getParameterOutputColumns() {
        return parameterService.getParameter().getHasManyField(ParameterFieldsWP.PARAMETER_OUTPUT_COLUMNS).find()
                .addOrder(SearchOrders.asc(ParameterOutputColumnFields.SUCCESSION)).list().getEntities();
    }

    private String getIdentifier(final Entity column) {
        return column.getStringField(L_IDENTIFIER);
    }

    private ColumnAlignment getColumnAlignment(final Entity column) {
        return ColumnAlignment.parseString(getAlignment(column));
    }

    private String getAlignment(final Entity column) {
        return column.getStringField(L_ALIGNMENT);
    }

}
