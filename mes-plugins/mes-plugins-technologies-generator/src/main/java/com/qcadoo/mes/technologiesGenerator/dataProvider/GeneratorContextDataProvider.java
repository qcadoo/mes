/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.technologiesGenerator.dataProvider;

import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.mes.technologiesGenerator.domain.ContextId;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.*;

@Service
public class GeneratorContextDataProvider {

    private final DataDefinitionService dataDefinitionService;

    @Autowired
    public GeneratorContextDataProvider(final DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    /**
     * Try find technology generator context entity by its id.
     * 
     * @param contextId
     *            unique identifier of an context.
     * @return matching context entity, if exists.
     */
    public Optional<Entity> find(final ContextId contextId) {
        return Optional.ofNullable(getContextDD().get(contextId.get()));
    }

    /**
     * Deletes technology generator context entities that weren't used since given date and time.
     * 
     * By 'weren't used' we mean contexts having update date (or create date if update date is not defined) earlier than given
     * one.
     * 
     * @param threshold
     *            date time threshold used to decide which contexts need to be deleted
     * @return either the messages holder if operation didn't finish successfully or list of deleted context ids.
     */
    public Either<EntityMessagesHolder, List<ContextId>> deleteContextsNotUsedSince(final DateTime threshold) {
        List<Long> contextIdsToBeDeleted = findContextsOlderThan(threshold);
        if (contextIdsToBeDeleted.isEmpty()) {
            return Either.right(Lists.newArrayList());
        }
        EntityOpResult result = getContextDD().delete(contextIdsToBeDeleted.toArray(new Long[contextIdsToBeDeleted.size()]));
        if (result.isSuccessfull()) {
            return Either.right(contextIdsToBeDeleted.stream().map(ContextId::new).collect(Collectors.toList()));
        }
        return Either.left(result.getMessagesHolder());
    }

    private List<Long> findContextsOlderThan(final DateTime threshold) {
        SearchCriteriaBuilder scb = getContextDD().find();
        scb.add(getCriteria(threshold.toDate()));
        scb.add(SearchRestrictions.eq(GeneratorContextFields.SAVED, false));
        scb.setProjection(alias(id(), "id"));

        return scb.list().getEntities().stream().map(e -> (Long) e.getField("id")).collect(Collectors.toList());
    }

    private SearchCriterion getCriteria(final Date threshold) {
        SearchCriterion updateDateIsEarlierThanThreshold = lt(GeneratorContextFields.UPDATE_DATE, threshold);
        SearchCriterion updateDateIsNull = isNull(GeneratorContextFields.UPDATE_DATE);
        SearchCriterion createDateIsEarlierThanThreshold = lt(GeneratorContextFields.UPDATE_DATE, threshold);

        return or(updateDateIsEarlierThanThreshold, and(updateDateIsNull, createDateIsEarlierThanThreshold));
    }

    private DataDefinition getContextDD() {
        return dataDefinitionService.get(TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER,
                TechnologiesGeneratorConstants.MODEL_GENERATOR_CONTEXT);
    }
}
