/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.workPlans.pdf.document.order;

import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.constants.*;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkPlanColumnServiceImpl implements WorkPlanColumnService{

    private ApplicationContext applicationContext;
    private EntityTreeUtilsService entityTreeUtilsService;

    @Autowired
    public WorkPlanColumnServiceImpl(ApplicationContext applicationContext, EntityTreeUtilsService entityTreeUtilsService) {
        this.applicationContext = applicationContext;
        this.entityTreeUtilsService = entityTreeUtilsService;
    }

    @Override
    public Map<OrderColumn, ColumnAlignment> getOrderColumns(final Entity workPlan) {
        Map<OrderColumn, ColumnAlignment> orderColumnToAlignment = new LinkedHashMap<OrderColumn, ColumnAlignment>();
        Map<String, OrderColumn> identifierOrderColumn = applicationContext.getBeansOfType(OrderColumn.class);
        for (Entity column : orderColumns(workPlan)) {
            Entity columnForOrders = columnForOrders(column);
            String identifier = identifier(columnForOrders);
            ColumnAlignment alignment = columnAlignment(columnForOrders);
            orderColumnToAlignment.put(identifierOrderColumn.get(identifier), alignment);
        }
        return orderColumnToAlignment;
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationProductsIn(final Entity workPlan) {
        return operationProducts(workPlan, ProductDirection.IN);
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationProductsOut(final Entity workPlan) {
        return operationProducts(workPlan, ProductDirection.OUT);
    }

    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationProducts(final Entity workPlan, final ProductDirection productDirection) {
        Map<Long, Map<OperationProductColumn, ColumnAlignment>> map = new HashMap<Long, Map<OperationProductColumn, ColumnAlignment>>();
        Map<String, OperationProductColumn> identifierOperationProductColumn = applicationContext.getBeansOfType(OperationProductColumn.class);
        List<Entity> orders = orders(workPlan);
        for (Entity order : orders) {
            Entity technology = technology(order);
            List<Entity> operationComponents = sortedOperationComponents(technology);
            for (Entity operationComponent : operationComponents) {
                List<Entity> columns = getOperationProductEntities(productDirection, operationComponent);
                Map<OperationProductColumn, ColumnAlignment> map2 = new LinkedHashMap<OperationProductColumn, ColumnAlignment>();
                for (Entity column : columns) {
                    String identifier = identifier(column);
                    OperationProductColumn key = identifierOperationProductColumn.get(identifier);
                    if(key != null)
                        map2.put(key, columnAlignment(column));
                }
                map.put(operationComponent.getId(), map2);
            }
        }
        return map;
    }

    private ColumnAlignment columnAlignment(Entity columnForOrders) {
        return ColumnAlignment.parseString(alignment(columnForOrders));
    }

    private String alignment(Entity columnForOrders) {
        return columnForOrders.getStringField(ColumnForOrdersFields.ALIGNMENT);
    }

    private String identifier(Entity columnForOrders) {
        return columnForOrders.getStringField(ColumnForOrdersFields.IDENTIFIER);
    }

    private Entity columnForOrders(Entity workPlanOrderColumn) {
        return workPlanOrderColumn.getBelongsToField(WorkPlanOrderColumnFields.COLUMN_FOR_ORDERS);
    }
    private List<Entity> orderColumns(Entity workPlan) {
        return workPlanOrderColumns(workPlan).find().addOrder(bySuccessionAsc()).list().getEntities();
    }

    private SearchOrder bySuccessionAsc() {
        return SearchOrders.asc(WorkPlanOrderColumnFields.SUCCESSION);
    }

    private EntityList workPlanOrderColumns(Entity workPlan) {
        return workPlan.getHasManyField(WorkPlanFields.WORK_PLAN_ORDER_COLUMNS);
    }

    private List<Entity> getOperationProductEntities(ProductDirection productDirection, Entity operationComponent) {
        String columnDefinitionModel;
        List<Entity> columnComponents;
        if (ProductDirection.IN.equals(productDirection)) {
            columnComponents = technologyOperationInputComponent(operationComponent);
            columnDefinitionModel = TechnologyOperationInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS;
        } else if (ProductDirection.OUT.equals(productDirection)) {
            columnComponents = technologyOperationOutputComponent(operationComponent);
            columnDefinitionModel = TechnologyOperationOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS;
        } else {
            throw new IllegalStateException("Wrong product direction");
        }

        List<Entity> columns = new LinkedList<Entity>();
        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField(columnDefinitionModel);
            columns.add(columnDefinition);
        }
        return columns;
    }

    private List<Entity> technologyOperationInputComponent(Entity operationComponent) {
        return operationComponent
                .getHasManyField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_INPUT_COLUMNS).find()
                .addOrder(SearchOrders.asc(TechnologyOperationInputColumnFields.SUCCESSION)).list().getEntities();
    }

    private List<Entity> sortedOperationComponents(Entity technology) {
        return entityTreeUtilsService.getSortedEntities(treeField(technology));
    }

    private EntityTree treeField(Entity technology) {
        return technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
    }

    private Entity technology(Entity order) {
        return order.getBelongsToField(OrderFields.TECHNOLOGY);
    }

    private List<Entity> orders(Entity workPlan) {
        return workPlan.getManyToManyField(WorkPlanFields.ORDERS);
    }

    private List<Entity> technologyOperationOutputComponent(Entity operationComponent) {
        return operationComponent
                .getHasManyField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_OUTPUT_COLUMNS).find()
                .addOrder(SearchOrders.asc(TechnologyOperationInputColumnFields.SUCCESSION)).list().getEntities();
    }


}
