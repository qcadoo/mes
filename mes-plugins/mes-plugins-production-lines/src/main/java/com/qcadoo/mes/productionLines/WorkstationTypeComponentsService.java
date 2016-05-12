package com.qcadoo.mes.productionLines;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionLines.constants.WorkstationTypeComponentFields;
import com.qcadoo.mes.productionLines.helper.WorkstationTypeComponentQuantity;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class WorkstationTypeComponentsService {

    private static final String COUNT_ALIAS = "count";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<WorkstationTypeComponentQuantity> getWorkstationTypeComponentsForPeriod(final Entity productionLine,
            final Entity workstationType, final Date from, final Date to) {
        List<WorkstationTypeComponentQuantity> results = Lists.newArrayList();
        List<Entity> componentsEntity = findWorkstationTypeComponentsForPeriod(productionLine, workstationType, from, to);
        results = calculateWorkstationTypeComponentQuantity(componentsEntity, from, to);
        return results;
    }

    public boolean isWorkstationTypeComponentsAfterDate(final Entity productionLine, final Entity workstationType, final Date date) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT)
                .find().add(SearchRestrictions.belongsTo(WorkstationTypeComponentFields.PRODUCTIONLINE, productionLine))
                .add(SearchRestrictions.belongsTo(WorkstationTypeComponentFields.WORKSTATIONTYPE, workstationType))
                .add(SearchRestrictions.gt(WorkstationTypeComponentFields.DATE_FROM, date));
        scb.setProjection(SearchProjections.alias(SearchProjections.countDistinct("id"), COUNT_ALIAS));
        scb.addOrder(SearchOrders.desc(COUNT_ALIAS));

        Entity projectionResult = scb.setMaxResults(1).uniqueResult();
        Long countValue = (Long) projectionResult.getField(COUNT_ALIAS);
        return countValue > 0;
    }

    private List<WorkstationTypeComponentQuantity> calculateWorkstationTypeComponentQuantity(final List<Entity> componentsEntity,
            final Date from, final Date to) {
        List<WorkstationTypeComponentQuantity> results = Lists.newArrayList();
        componentsEntity.forEach(entity -> createWorkstationTypeComponentQuantityEntry(entity, from, to, results));
        return results;
    }

    private void createWorkstationTypeComponentQuantityEntry(Entity entity, Date from, Date to,
            List<WorkstationTypeComponentQuantity> results) {
        Date componentDateFrom = entity.getDateField(WorkstationTypeComponentFields.DATE_FROM);
        Date componentDateTo = entity.getDateField(WorkstationTypeComponentFields.DATE_TO);
        Integer quantity = entity.getIntegerField(WorkstationTypeComponentFields.QUANTITY);
        WorkstationTypeComponentQuantity workstationTypeComponentQuantity = new WorkstationTypeComponentQuantity(quantity,
                resolveDateFrom(from, componentDateFrom), resolveDateTo(to, componentDateTo));
        if (componentDateTo == null) {
            workstationTypeComponentQuantity.setToInfinity(true);
        }
        results.add(workstationTypeComponentQuantity);
    }

    private DateTime resolveDateTo(Date to, Date componentDateTo) {
        if (componentDateTo == null) {
            return new DateTime(to);
        }
        if (to.before(componentDateTo)) {
            return new DateTime(to);
        }
        return new DateTime(componentDateTo);
    }

    private DateTime resolveDateFrom(Date from, Date componentDateFrom) {
        if (componentDateFrom == null) {
            return new DateTime(from);
        }
        if (componentDateFrom.after(from)) {
            return new DateTime(componentDateFrom);
        }
        return new DateTime(from);
    }

    private List<Entity> findWorkstationTypeComponentsForPeriod(final Entity productionLine, final Entity workstationType,
            final Date from, final Date to) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT)
                .find()
                .addOrder(SearchOrders.asc(WorkstationTypeComponentFields.DATE_FROM))
                .add(SearchRestrictions.belongsTo(WorkstationTypeComponentFields.PRODUCTIONLINE, productionLine))
                .add(SearchRestrictions.belongsTo(WorkstationTypeComponentFields.WORKSTATIONTYPE, workstationType))
                .add(SearchRestrictions.or(SearchRestrictions.gt(WorkstationTypeComponentFields.DATE_TO, from),
                        SearchRestrictions.isNull(WorkstationTypeComponentFields.DATE_TO)))
                .add(SearchRestrictions.lt(WorkstationTypeComponentFields.DATE_FROM, to));
        return scb.list().getEntities();
    }

    public Optional<Entity> findPreviousWorkstationTypeComponent(final Entity workstationTypeComponent) {
        Entity productionLine = workstationTypeComponent.getBelongsToField(WorkstationTypeComponentFields.PRODUCTIONLINE);
        Entity workstationType = workstationTypeComponent.getBelongsToField(WorkstationTypeComponentFields.WORKSTATIONTYPE);
        Date dateFrom = workstationTypeComponent.getDateField(WorkstationTypeComponentFields.DATE_FROM);
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT)
                .find().addOrder(SearchOrders.desc(WorkstationTypeComponentFields.DATE_FROM))
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
        Date dateTo = workstationTypeComponent.getDateField(WorkstationTypeComponentFields.DATE_TO);
        SearchCriterion scb = SearchRestrictions.and(
                SearchRestrictions.belongsTo(WorkstationTypeComponentFields.PRODUCTIONLINE, productionLine),
                SearchRestrictions.belongsTo(WorkstationTypeComponentFields.WORKSTATIONTYPE, workstationType));

        if (dateTo == null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.or(SearchRestrictions.ge(
                    WorkstationTypeComponentFields.DATE_FROM, dateFrom), SearchRestrictions.and(
                    SearchRestrictions.le(WorkstationTypeComponentFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(WorkstationTypeComponentFields.DATE_TO, dateFrom))));
        } else {
            scb = SearchRestrictions.and(scb, SearchRestrictions.and(
                    SearchRestrictions.le(WorkstationTypeComponentFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(WorkstationTypeComponentFields.DATE_TO, dateFrom)));
        }
        if (workstationTypeComponent.getId() != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.ne("id", workstationTypeComponent.getId()));
        }
        long count = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT).count(scb);
        return count != 0;

    }

}
