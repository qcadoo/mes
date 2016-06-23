/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.FaultTypeAppliesTo;
import com.qcadoo.mes.basic.constants.FaultTypeFields;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangeFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventState;
import com.qcadoo.mes.productionLines.constants.DivisionFieldsPL;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class EventCriteriaModifiersCMP {

    public static final String L_MAINTENANCE_EVENT_CONTEXT_FACTORY = "maintenanceEventContextFactory";

    public static final String L_MAINTENANCE_EVENT_CONTEXT_DIVISION = "maintenanceEventContextDivision";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void hideFailedStateChanges(final SearchCriteriaBuilder searchCriteriaBuilder) {
        searchCriteriaBuilder.add(SearchRestrictions.eq(MaintenanceEventStateChangeFields.STATUS, "03successful"));
    }

    public void filterRevokedAndPlannedEvents(final SearchCriteriaBuilder searchCriteriaBuilder) {
        searchCriteriaBuilder.add(
                SearchRestrictions.ne(MaintenanceEventFields.STATE, MaintenanceEventState.REVOKED.getStringValue())).add(
                SearchRestrictions.ne(MaintenanceEventFields.STATE, MaintenanceEventState.PLANNED.getStringValue()));
    }

    public void filterCanceledEvents(final SearchCriteriaBuilder searchCriteriaBuilder) {
        searchCriteriaBuilder.add(SearchRestrictions.ne(PlannedEventFields.STATE, PlannedEventState.CANCELED.getStringValue()))
                .add(SearchRestrictions.ne(PlannedEventFields.TYPE, PlannedEventType.UDT_REVIEW.getStringValue()))
                .add(SearchRestrictions.ne(PlannedEventFields.TYPE, PlannedEventType.METER_READING.getStringValue()));
    }

    public void selectFactory(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValueHolder) {

    }

    public void selectDivision(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(MaintenanceEventFields.FACTORY)) {
            DataDefinition factoryDataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_FACTORY);

            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DivisionFields.FACTORY, factoryDataDefinition,
                    filterValueHolder.getLong(MaintenanceEventFields.FACTORY)));
        }
    }

    public void selectWorkstation(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(MaintenanceEventFields.DIVISION)) {
            Long divisionId = filterValueHolder.getLong(MaintenanceEventFields.DIVISION);

            searchCriteriaBuilder.createAlias(WorkstationFields.DIVISION, WorkstationFields.DIVISION, JoinType.INNER).add(
                    SearchRestrictions.eq(WorkstationFields.DIVISION + ".id", divisionId));
        }
        if (filterValueHolder.has(MaintenanceEventFields.PRODUCTION_LINE)) {
            Long productionLineId = filterValueHolder.getLong(MaintenanceEventFields.PRODUCTION_LINE);

            searchCriteriaBuilder.createAlias(WorkstationFieldsPL.PRODUCTION_LINE, WorkstationFieldsPL.PRODUCTION_LINE,
                    JoinType.INNER).add(SearchRestrictions.eq(WorkstationFieldsPL.PRODUCTION_LINE + ".id", productionLineId));
        }
    }

    public void selectProductionLine(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(MaintenanceEventFields.DIVISION)) {
            Long divisionId = filterValueHolder.getLong(MaintenanceEventFields.DIVISION);

            Entity division = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_DIVISION).get(
                    divisionId);

            List<Long> productionLinesIds = division.getHasManyField(DivisionFieldsPL.PRODUCTION_LINES).stream()
                    .map(Entity::getId).collect(Collectors.toList());

            if (productionLinesIds.isEmpty()) {
                return;
            }

            searchCriteriaBuilder.add(SearchRestrictions.in("id", productionLinesIds));
        }
    }

    public void selectSubassembly(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(MaintenanceEventFields.WORKSTATION)) {
            DataDefinition workstationDataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_WORKSTATION);

            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(SubassemblyFields.WORKSTATION, workstationDataDefinition,
                    filterValueHolder.getLong(MaintenanceEventFields.WORKSTATION)));
        }
    }

    public void selectFaultType(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValue) {
        if (filterValue.has(MaintenanceEventFields.SUBASSEMBLY)) {
            addSubassemblyCriteria(searchCriteriaBuilder, filterValue);
        } else if (filterValue.has(MaintenanceEventFields.WORKSTATION)) {
            addWorkstationCriteria(searchCriteriaBuilder, filterValue);
        } else {
            addDefaultCriteria(searchCriteriaBuilder, filterValue);
        }
    }

    private void addSubassemblyCriteria(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        Long subassemblyId = filterValueHolder.getLong(MaintenanceEventFields.SUBASSEMBLY);

        addCriteriaRestrictions(searchCriteriaBuilder, filterValueHolder, subassemblyId, FaultTypeFields.SUBASSEMBLIES);
    }

    private void addWorkstationCriteria(final SearchCriteriaBuilder ssearchCriteriaBuilderb,
            final FilterValueHolder filterValueHolder) {
        Long workstationId = filterValueHolder.getLong(MaintenanceEventFields.WORKSTATION);

        addCriteriaRestrictions(ssearchCriteriaBuilderb, filterValueHolder, workstationId, FaultTypeFields.WORKSTATIONS);
    }

    private void addDefaultCriteria(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValue) {
        addCriteriaRestrictions(searchCriteriaBuilder, filterValue, null, null);
    }

    private void addCriteriaRestrictions(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder, Long elementId, String alias) {
        SearchCriterion searchCriterion;

        if (filterValueHolder.has(MaintenanceEventFields.SUBASSEMBLY)
                || filterValueHolder.has(MaintenanceEventFields.WORKSTATION)) {
            if (filterValueHolder.has(WorkstationFields.WORKSTATION_TYPE)) {
                Long workstationTypeId = filterValueHolder.getLong(WorkstationFields.WORKSTATION_TYPE);

                searchCriterion = SearchRestrictions.or(
                        SearchRestrictions.eq(FaultTypeFields.WORKSTATION_TYPES + ".id", workstationTypeId),
                        SearchRestrictions.eq(alias + ".id", elementId));
            } else {
                searchCriterion = SearchRestrictions.eq(alias + ".id", elementId);
            }

            searchCriteriaBuilder
                    .createAlias(FaultTypeFields.WORKSTATION_TYPES, FaultTypeFields.WORKSTATION_TYPES, JoinType.LEFT)
                    .createAlias(alias, alias, JoinType.LEFT)
                    .add(SearchRestrictions.or(SearchRestrictions.and(
                            SearchRestrictions.in(FaultTypeFields.APPLIES_TO, getFaultTypeAppliesToStringValues()),
                            searchCriterion), SearchRestrictions.eq(FaultTypeFields.IS_DEFAULT, true)));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.or(
                    SearchRestrictions.in(FaultTypeFields.APPLIES_TO, getFaultTypeAppliesToStringValues()),
                    SearchRestrictions.isNull(FaultTypeFields.APPLIES_TO),
                    SearchRestrictions.eq(FaultTypeFields.IS_DEFAULT, true)));
        }
    }

    private List<String> getFaultTypeAppliesToStringValues() {
        return Stream.of(FaultTypeAppliesTo.values()).map(FaultTypeAppliesTo::getStringValue).collect(Collectors.toList());
    }

    public void showEventsFromContext(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_MAINTENANCE_EVENT_CONTEXT_FACTORY)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(MaintenanceEventFields.FACTORY + "_id",
                    filterValueHolder.getInteger(L_MAINTENANCE_EVENT_CONTEXT_FACTORY)));
        }

        if (filterValueHolder.has(L_MAINTENANCE_EVENT_CONTEXT_DIVISION)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(MaintenanceEventFields.DIVISION + "_id",
                    filterValueHolder.getInteger(L_MAINTENANCE_EVENT_CONTEXT_DIVISION)));
        }

    }

    public void showPlannedEventsFromContext(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_MAINTENANCE_EVENT_CONTEXT_FACTORY)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(MaintenanceEventFields.FACTORY + "_id",
                    filterValueHolder.getInteger(L_MAINTENANCE_EVENT_CONTEXT_FACTORY)));
        }

        if (filterValueHolder.has(L_MAINTENANCE_EVENT_CONTEXT_DIVISION)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(MaintenanceEventFields.DIVISION + "_id",
                    filterValueHolder.getInteger(L_MAINTENANCE_EVENT_CONTEXT_DIVISION)));
        }
    }

}
