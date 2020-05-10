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
package com.qcadoo.mes.timeGapsPreview.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionLines.ProductionLinesSearchService;
import com.qcadoo.mes.timeGapsPreview.TimeGapsContext;
import com.qcadoo.mes.timeGapsPreview.TimeGapsGenerator;
import com.qcadoo.mes.timeGapsPreview.TimeGapsSearchResult;
import com.qcadoo.mes.timeGapsPreview.constants.TimeGapsContextFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Service
public class GenerateTimeGapsListeners {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateTimeGapsListeners.class);

    @Autowired
    private TimeGapsGenerator timeGapsGenerator;

    @Autowired
    private ProductionLinesSearchService productionLinesModelHelper;

    public void generateTimeGaps(final ViewDefinitionState viewState, final ComponentState formState, final String[] args)
            throws ParseException {
        FormComponent form = (FormComponent) formState;
        Entity contextEntity = form.getPersistedEntityWithIncludedFormValues();
        // We don't want to reuse contexts - in case of the user working with many browser tabs to compare a couple of results
        contextEntity.setId(null);
        contextEntity.setField(TimeGapsContextFields.TIME_GAPS, Collections.<Entity> emptyList());

        // Call validation
        contextEntity = contextEntity.getDataDefinition().save(contextEntity);
        if (!contextEntity.isValid()) {
            clearSummaryFields(contextEntity);
            form.setEntity(contextEntity);
            return;
        }

        // Generate & persist results
        TimeGapsSearchResult searchResult = performGeneration(contextEntity);
        Entity persistedContext = persistResults(contextEntity, searchResult);

        if (persistedContext.isValid()) {
            // Show 'time gaps' tab
            WindowComponent window = (WindowComponent) findComponentByReferenceName(viewState, QcadooViewConstants.L_WINDOW);
            window.setActiveTab("timeGaps");
        }
        form.setEntity(persistedContext);
    }

    private void clearSummaryFields(final Entity contextEntity) {
        contextEntity.setField(TimeGapsContextFields.LONGEST_DURATION_LINE, null);
        contextEntity.setField(TimeGapsContextFields.LONGEST_DURATION, null);
        contextEntity.setField(TimeGapsContextFields.TOTAL_DURATION, null);
    }

    private Set<Long> findMatchingProductionLines(final Entity contextEntity) {
        Entity technology = contextEntity.getBelongsToField(TimeGapsContextFields.SUPPORTED_TECHNOLOGY);
        if (technology != null) {
            return productionLinesModelHelper.findLinesSupportingTechnology(technology.getId());
        }
        Entity techGroup = contextEntity.getBelongsToField(TimeGapsContextFields.SUPPORTED_TECHNOLOGY_GROUP);
        if (techGroup != null) {
            return productionLinesModelHelper.findLinesSupportingTechnologyGroup(techGroup.getId());
        }
        return productionLinesModelHelper.findAllLines();
    }

    private TimeGapsSearchResult performGeneration(final Entity contextEntity) {
        Integer durationFilterInSeconds = contextEntity.getIntegerField(TimeGapsContextFields.DURATION_FILTER);
        Duration durationFilter = Seconds.seconds(durationFilterInSeconds).toStandardDuration();
        TimeGapsContext context = new TimeGapsContext(extractSearchInterval(contextEntity),
                findMatchingProductionLines(contextEntity), durationFilter);
        return timeGapsGenerator.generate(context);
    }

    private Interval extractSearchInterval(final Entity contextEntity) {
        Date from = contextEntity.getDateField(TimeGapsContextFields.FROM_DATE);
        Date to = contextEntity.getDateField(TimeGapsContextFields.TO_DATE);
        return new Interval(from.getTime(), to.getTime());
    }

    private Entity persistResults(final Entity contextEntity, final TimeGapsSearchResult searchResult) {
        contextEntity.setField(TimeGapsContextFields.TIME_GAPS, searchResult.asEntities());
        contextEntity.setField(TimeGapsContextFields.TOTAL_DURATION, searchResult.getTotalDuration().getStandardSeconds());
        contextEntity.setField(TimeGapsContextFields.LONGEST_DURATION, searchResult.getLongestInterval().toDuration()
                .getStandardSeconds());
        contextEntity.setField(TimeGapsContextFields.LONGEST_DURATION_LINE, searchResult.getLongestIntervalLineId());
        return contextEntity.getDataDefinition().save(contextEntity);
    }

    public void clearOtherLineCriteriaLookupComponents(final ViewDefinitionState viewState, final ComponentState eventPerformer,
            final String[] arguments) {
        if (((LookupComponent) eventPerformer).getEntity() == null) {
            return;
        }
        LookupComponent techLookup = (LookupComponent) findComponentByReferenceName(viewState, "supportedTechnology");
        LookupComponent techGroupLookup = (LookupComponent) findComponentByReferenceName(viewState, "supportedTechnologyGroup");
        Iterable<LookupComponent> lookupComponents = Lists.newArrayList(techLookup, techGroupLookup);
        for (LookupComponent lookupComponent : lookupComponents) {
            if (lookupComponent.getName().equals(eventPerformer.getName())) {
                continue;
            }
            lookupComponent.setFieldValue(null);
        }
    }

    private ComponentState findComponentByReferenceName(final ViewDefinitionState viewState, final String referenceName) {
        ComponentState component = viewState.getComponentByReference(referenceName);
        if (component == null && LOGGER.isWarnEnabled()) {
            LOGGER.warn(String.format("Can't find component with reference name: '%s'", referenceName));
        }
        return component;
    }
}
