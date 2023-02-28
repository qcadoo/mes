package com.qcadoo.mes.productionScheduling.criteriaModifiers;

import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.operationTimeCalculations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OperCompTimeCalculationsCM {

    public static final String ORDER_PARAMETER = "orderId";

    public static final String TECHNOLOGY_PARAMETER = "technologyId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showEntriesForOrder(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(ORDER_PARAMETER)) {
            Entity orderTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION)
                    .find()
                    .add(SearchRestrictions.belongsTo("order", OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                            filterValue.getLong(ORDER_PARAMETER))).setMaxResults(1).uniqueResult();
            if (Objects.isNull(orderTimeCalculation)) {
                scb.add(SearchRestrictions.idEq(0L));
            } else {
                scb.add(SearchRestrictions.belongsTo(OperCompTimeCalculationsFields.ORDER_TIME_CALCULATION, orderTimeCalculation));
            }
        } else {
            scb.add(SearchRestrictions.idEq(0L));
        }

    }

    public void showEntriesForTechnology(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(TECHNOLOGY_PARAMETER)) {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(filterValue.getLong(TECHNOLOGY_PARAMETER));
            List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
            if (operationComponents.isEmpty()) {
                scb.add(SearchRestrictions.idEq(0L));
            } else {
                scb.createAlias(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT, "opr", JoinType.LEFT);

                scb.add(SearchRestrictions.in("opr.id", operationComponents.stream().map(Entity::getId).collect(Collectors.toList())));
                scb.add(SearchRestrictions.isNull(OperCompTimeCalculationsFields.ORDER_TIME_CALCULATION));
            }
        } else {
            scb.add(SearchRestrictions.idEq(0L));
        }
    }
}
