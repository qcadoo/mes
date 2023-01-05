package com.qcadoo.mes.basic.hooks;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PieceRateItemFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class PieceRateItemHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Optional<Entity> maybePreviousComponent = findPreviousPieceRateItem(entity);
        if (maybePreviousComponent.isPresent()) {
            DateTime dateFrom = new DateTime(entity.getDateField(PieceRateItemFields.DATE_FROM));
            Entity previousComponent = maybePreviousComponent.get();
            Date originalDateTo = previousComponent.getDateField(PieceRateItemFields.DATE_TO);
            if (originalDateTo == null || originalDateTo.compareTo(dateFrom.toDate()) != 0) {
                previousComponent.setField(PieceRateItemFields.DATE_TO, new DateTime(dateFrom.toDate()).minusDays(1).toDate());
                Entity savedPrevious = dataDefinition.save(previousComponent);
                if (!savedPrevious.isValid()) {
                    savedPrevious.getErrors().forEach((key, value) -> entity.addGlobalError(value.getMessage()));
                }
            }

        }

        if (shouldValidateDates(dataDefinition, entity)) {
            if (checkIfRateForGivenTimeExists(entity)) {
                entity.addError(dataDefinition.getField(PieceRateItemFields.DATE_FROM),
                        "basic.technologicalProcessRateItem.validation.otherComponentsExist");
            }
        }

    }


    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getDateField(PieceRateItemFields.DATE_FROM) != null
                && entity.getDateField(PieceRateItemFields.DATE_TO) != null) {
            entity.addGlobalError("basic.technologicalProcessRateItem.onDelete.hasDates");
            return false;
        }

        Optional<Entity> maybePreviousComponent = findPreviousPieceRateItem(entity);
        if (maybePreviousComponent.isPresent()) {
            Entity previousComponent = maybePreviousComponent.get();
            previousComponent.setField(PieceRateItemFields.DATE_TO, null);
            dataDefinition.save(previousComponent);
        }
        return true;
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = entity.getDateField(PieceRateItemFields.DATE_FROM);
        Date dateTo = entity.getDateField(PieceRateItemFields.DATE_TO);
        if (dateTo != null && dateTo.compareTo(dateFrom) < 0) {
            entity.addError(dataDefinition.getField(PieceRateItemFields.DATE_FROM),
                    "basic.technologicalProcessRateItem.validation.datesInvalid");
            return false;
        }
        return true;
    }

    private Optional<Entity> findPreviousPieceRateItem(final Entity pieceRateItem) {
        Entity pieceRate = pieceRateItem.getBelongsToField(PieceRateItemFields.PIECE_RATE);
        Date dateFrom = pieceRateItem.getDateField(PieceRateItemFields.DATE_FROM);
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PIECE_RATE_ITEM)
                .find().addOrder(SearchOrders.desc(PieceRateItemFields.DATE_FROM))
                .add(SearchRestrictions.belongsTo(PieceRateItemFields.PIECE_RATE, pieceRate))
                .add(SearchRestrictions.lt(PieceRateItemFields.DATE_FROM, dateFrom));

        if (pieceRateItem.getId() != null) {
            scb.add(SearchRestrictions.idNe(pieceRateItem.getId()));
        }
        List<Entity> previousComponents = scb.list().getEntities();

        if (previousComponents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(previousComponents.get(0));
    }

    public boolean checkIfRateForGivenTimeExists(final Entity pieceRateItem) {
        Entity pieceRate = pieceRateItem.getBelongsToField(PieceRateItemFields.PIECE_RATE);

        Date dateFrom = pieceRateItem.getDateField(PieceRateItemFields.DATE_FROM);
        Date dateTo = pieceRateItem.getDateField(PieceRateItemFields.DATE_TO);
        SearchCriterion scb = SearchRestrictions.belongsTo(PieceRateItemFields.PIECE_RATE, pieceRate);

        if (dateTo == null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.or(SearchRestrictions.ge(
                    PieceRateItemFields.DATE_FROM, dateFrom), SearchRestrictions.and(
                    SearchRestrictions.le(PieceRateItemFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(PieceRateItemFields.DATE_TO, dateFrom))));
        } else {
            scb = SearchRestrictions.and(scb, SearchRestrictions.and(
                    SearchRestrictions.le(PieceRateItemFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(PieceRateItemFields.DATE_TO, dateFrom)));
        }
        if (pieceRateItem.getId() != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.ne("id", pieceRateItem.getId()));
        }
        long count = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_PIECE_RATE_ITEM).count(scb);
        return count != 0;

    }
    private boolean shouldValidateDates(final DataDefinition dataDefinition, final Entity originalEntity) {
        if (originalEntity.getId() == null) {
            return true;
        }
        Entity dbEntity = dataDefinition.get(originalEntity.getId());
        Date originalDateFrom = originalEntity.getDateField(PieceRateItemFields.DATE_FROM);
        Date dbDateFrom = dbEntity.getDateField(PieceRateItemFields.DATE_FROM);
        return originalDateFrom.compareTo(dbDateFrom) != 0;
    }
}
