package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TechnologicalProcessRateItemHooks {

    public static final String L_DATE_TO = "dateTo";

    public static final String L_DATE_FROM = "dateFrom";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Optional<Entity> maybePreviousComponent = findPreviousTechnologicalProcessRateItem(entity);
        if (maybePreviousComponent.isPresent()) {
            DateTime dateFrom = new DateTime(entity.getDateField(L_DATE_FROM));
            Entity previousComponent = maybePreviousComponent.get();
            Date originalDateTo = previousComponent.getDateField(L_DATE_TO);
            if (originalDateTo == null || originalDateTo.compareTo(dateFrom.toDate()) != 0) {
                previousComponent.setField(L_DATE_TO, new DateTime(dateFrom.toDate()).minusDays(1).toDate());
                Entity savedPrevious = dataDefinition.save(previousComponent);
                if (!savedPrevious.isValid()) {
                    savedPrevious.getErrors().entrySet().stream()
                            .forEach(entry -> entity.addGlobalError(entry.getValue().getMessage()));
                }
            }

        }

        if (shouldValidateDates(dataDefinition, entity)) {
            boolean componentExists = checkIfRateForGivenTimeExists(entity);
            if (componentExists) {
                entity.addError(dataDefinition.getField(L_DATE_FROM),
                        "basic.technologicalProcessRateItem.validation.otherComponentsExist");
            }
        }

    }


    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        boolean cannotDelete = entity.getDateField(L_DATE_FROM) != null
                && entity.getDateField(L_DATE_TO) != null;
        if (cannotDelete) {
            entity.addGlobalError("basic.technologicalProcessRateItem.onDelete.hasDates");
            return false;
        }

        Optional<Entity> maybePreviousComponent = findPreviousTechnologicalProcessRateItem(entity);
        if (maybePreviousComponent.isPresent()) {
            Entity previousComponent = maybePreviousComponent.get();
            previousComponent.setField(L_DATE_TO, null);
            dataDefinition.save(previousComponent);
        }
        return true;
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = entity.getDateField(L_DATE_FROM);
        Date dateTo = entity.getDateField(L_DATE_TO);
        if (dateTo != null && dateTo.compareTo(dateFrom) < 0) {
            entity.addError(dataDefinition.getField(L_DATE_FROM),
                    "basic.technologicalProcessRateItem.validation.datesInvalid");
            return false;
        }
        return true;
    }

    private Optional<Entity> findPreviousTechnologicalProcessRateItem(final Entity technologicalProcessRateItem) {
        Entity technologicalProcessRate = technologicalProcessRateItem.getBelongsToField("technologicalProcessRate");
        Date dateFrom = technologicalProcessRateItem.getDateField(L_DATE_FROM);
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, "technologicalProcessRateItem")
                .find().addOrder(SearchOrders.desc(L_DATE_FROM))
                .add(SearchRestrictions.belongsTo("technologicalProcessRate", technologicalProcessRate))
                .add(SearchRestrictions.lt(L_DATE_FROM, dateFrom));

        if (technologicalProcessRateItem.getId() != null) {
            scb.add(SearchRestrictions.idNe(technologicalProcessRateItem.getId()));
        }
        List<Entity> previousComponents = scb.list().getEntities();

        if (previousComponents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(previousComponents.get(0));
    }

    private boolean checkIfRateForGivenTimeExists(final Entity technologicalProcessRateItem) {

        Entity technologicalProcessRate = technologicalProcessRateItem.getBelongsToField("technologicalProcessRate");

        Date dateFrom = technologicalProcessRateItem.getDateField(L_DATE_FROM);
        Date dateTo = technologicalProcessRateItem.getDateField(L_DATE_TO);
        SearchCriterion scb = SearchRestrictions.belongsTo("technologicalProcessRate", technologicalProcessRate);

        if (dateTo == null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.or(SearchRestrictions.ge(
                    L_DATE_FROM, dateFrom), SearchRestrictions.and(
                    SearchRestrictions.le(L_DATE_FROM, dateFrom),
                    SearchRestrictions.gt(L_DATE_TO, dateFrom))));
        } else {
            scb = SearchRestrictions.and(scb, SearchRestrictions.and(
                    SearchRestrictions.le(L_DATE_FROM, dateFrom),
                    SearchRestrictions.gt(L_DATE_TO, dateFrom)));
        }
        if (technologicalProcessRateItem.getId() != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.ne("id", technologicalProcessRateItem.getId()));
        }
        long count = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                "technologicalProcessRateItem").count(scb);
        return count != 0;

    }
    private boolean shouldValidateDates(final DataDefinition dataDefinition, final Entity originalEntity) {
        if (originalEntity.getId() == null) {
            return true;
        }
        Entity dbEntity = dataDefinition.get(originalEntity.getId());
        Date originalDateFrom = originalEntity.getDateField(L_DATE_FROM);
        Date dbDateFrom = dbEntity.getDateField(L_DATE_FROM);
        return originalDateFrom.compareTo(dbDateFrom) != 0;
    }
}
