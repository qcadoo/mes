package com.qcadoo.mes.costCalculation.hooks;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.AdditionalDirectCostItemFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AdditionalDirectCostItemHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Optional<Entity> maybePreviousCost = findPreviousAdditionalDirectCostItem(entity);
        if (maybePreviousCost.isPresent()) {
            DateTime dateFrom = new DateTime(entity.getDateField(AdditionalDirectCostItemFields.DATE_FROM));
            Entity previousCost = maybePreviousCost.get();
            Date originalDateTo = previousCost.getDateField(AdditionalDirectCostItemFields.DATE_TO);
            if (originalDateTo == null || originalDateTo.compareTo(dateFrom.toDate()) != 0) {
                previousCost.setField(AdditionalDirectCostItemFields.DATE_TO, new DateTime(dateFrom.toDate()).minusDays(1).toDate());
                Entity savedPrevious = dataDefinition.save(previousCost);
                if (!savedPrevious.isValid()) {
                    savedPrevious.getErrors().forEach((key, value) -> entity.addGlobalError(value.getMessage()));
                }
            }

        }

        if (checkDateFromChanged(dataDefinition, entity)) {
            if (checkIfCostForGivenTimeExists(entity)) {
                entity.addError(dataDefinition.getField(AdditionalDirectCostItemFields.DATE_FROM),
                        "costCalculation.additionalDirectCostItem.validation.otherCostExist");
            }
        }

    }


    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getDateField(AdditionalDirectCostItemFields.DATE_FROM) != null
                && entity.getDateField(AdditionalDirectCostItemFields.DATE_TO) != null) {
            entity.addGlobalError("costCalculation.additionalDirectCostItem.onDelete.hasDates");
            return false;
        }

        Optional<Entity> maybePreviousCost = findPreviousAdditionalDirectCostItem(entity);
        if (maybePreviousCost.isPresent()) {
            Entity previousCost = maybePreviousCost.get();
            previousCost.setField(AdditionalDirectCostItemFields.DATE_TO, null);
            dataDefinition.save(previousCost);
        }
        return true;
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = entity.getDateField(AdditionalDirectCostItemFields.DATE_FROM);
        Date dateTo = entity.getDateField(AdditionalDirectCostItemFields.DATE_TO);
        if (dateTo != null && dateTo.compareTo(dateFrom) < 0) {
            entity.addError(dataDefinition.getField(AdditionalDirectCostItemFields.DATE_FROM),
                    "costCalculation.additionalDirectCostItem.validation.datesInvalid");
            return false;
        }
        return true;
    }

    private Optional<Entity> findPreviousAdditionalDirectCostItem(final Entity entity) {
        Entity additionalDirectCost = entity.getBelongsToField(AdditionalDirectCostItemFields.ADDITIONAL_DIRECT_COST);
        Date dateFrom = entity.getDateField(AdditionalDirectCostItemFields.DATE_FROM);
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_ADDITIONAL_DIRECT_COST_ITEM)
                .find().addOrder(SearchOrders.desc(AdditionalDirectCostItemFields.DATE_FROM))
                .add(SearchRestrictions.belongsTo(AdditionalDirectCostItemFields.ADDITIONAL_DIRECT_COST, additionalDirectCost))
                .add(SearchRestrictions.lt(AdditionalDirectCostItemFields.DATE_FROM, dateFrom));

        if (entity.getId() != null) {
            scb.add(SearchRestrictions.idNe(entity.getId()));
        }
        List<Entity> previousCosts = scb.list().getEntities();

        if (previousCosts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(previousCosts.get(0));
    }

    public boolean checkIfCostForGivenTimeExists(final Entity entity) {
        Entity additionalDirectCost = entity.getBelongsToField(AdditionalDirectCostItemFields.ADDITIONAL_DIRECT_COST);

        Date dateFrom = entity.getDateField(AdditionalDirectCostItemFields.DATE_FROM);
        Date dateTo = entity.getDateField(AdditionalDirectCostItemFields.DATE_TO);
        SearchCriterion scb = SearchRestrictions.belongsTo(AdditionalDirectCostItemFields.ADDITIONAL_DIRECT_COST, additionalDirectCost);

        if (dateTo == null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.or(SearchRestrictions.ge(
                    AdditionalDirectCostItemFields.DATE_FROM, dateFrom), SearchRestrictions.and(
                    SearchRestrictions.le(AdditionalDirectCostItemFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(AdditionalDirectCostItemFields.DATE_TO, dateFrom))));
        } else {
            scb = SearchRestrictions.and(scb, SearchRestrictions.and(
                    SearchRestrictions.le(AdditionalDirectCostItemFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(AdditionalDirectCostItemFields.DATE_TO, dateFrom)));
        }
        if (entity.getId() != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.ne("id", entity.getId()));
        }
        long count = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_ADDITIONAL_DIRECT_COST_ITEM).count(scb);
        return count != 0;

    }

    private boolean checkDateFromChanged(final DataDefinition dataDefinition, final Entity originalEntity) {
        if (originalEntity.getId() == null) {
            return true;
        }
        Entity dbEntity = dataDefinition.get(originalEntity.getId());
        Date originalDateFrom = originalEntity.getDateField(AdditionalDirectCostItemFields.DATE_FROM);
        Date dbDateFrom = dbEntity.getDateField(AdditionalDirectCostItemFields.DATE_FROM);
        return originalDateFrom.compareTo(dbDateFrom) != 0;
    }
}
