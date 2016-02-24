package com.qcadoo.mes.productionLines;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionLines.constants.WorkstationTypeComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class WorkstationTypeComponentsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Optional<Entity> findPreviousWorkstationTypeComponent(final Entity workstationTypeComponent) {
        Entity productionLine = workstationTypeComponent.getBelongsToField(WorkstationTypeComponentFields.PRODUCTIONLINE);
        Entity workstationType = workstationTypeComponent.getBelongsToField(WorkstationTypeComponentFields.WORKSTATIONTYPE);
        Date dateFrom = workstationTypeComponent.getDateField(WorkstationTypeComponentFields.DATE_FROM);
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT).find()
                .addOrder(SearchOrders.desc(WorkstationTypeComponentFields.DATE_FROM))
                .add(SearchRestrictions.belongsTo(WorkstationTypeComponentFields.PRODUCTIONLINE, productionLine))
                .add(SearchRestrictions.belongsTo(WorkstationTypeComponentFields.WORKSTATIONTYPE, workstationType))
                .add(SearchRestrictions.lt(WorkstationTypeComponentFields.DATE_FROM, dateFrom));

        if (workstationTypeComponent.getId() != null) {
            scb.add(SearchRestrictions.idNe(workstationTypeComponent.getId()));
        }
        List<Entity> previousComponents = scb.list().getEntities();

        if (previousComponents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(previousComponents.get(0));
    }

    public boolean checkIfComponentForGivenTimeExists(final Entity workstationTypeComponent) {

        Entity productionLine = workstationTypeComponent.getBelongsToField(WorkstationTypeComponentFields.PRODUCTIONLINE);
        Entity workstationType = workstationTypeComponent.getBelongsToField(WorkstationTypeComponentFields.WORKSTATIONTYPE);
        Date dateFrom = workstationTypeComponent.getDateField(WorkstationTypeComponentFields.DATE_FROM);
        SearchCriterion scb = SearchRestrictions.and(
                SearchRestrictions.belongsTo(WorkstationTypeComponentFields.PRODUCTIONLINE, productionLine),
                SearchRestrictions.belongsTo(WorkstationTypeComponentFields.WORKSTATIONTYPE, workstationType),
                SearchRestrictions.or(
                        SearchRestrictions.and(SearchRestrictions.le(WorkstationTypeComponentFields.DATE_FROM, dateFrom),
                                SearchRestrictions.ge(WorkstationTypeComponentFields.DATE_TO, dateFrom)),
                        SearchRestrictions.ge(WorkstationTypeComponentFields.DATE_FROM, dateFrom)));

        if (workstationTypeComponent.getId() != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.ne("id", workstationType.getId()));
        }
        long count = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT)
                .count(scb);
        return count != 0;

    }

}
