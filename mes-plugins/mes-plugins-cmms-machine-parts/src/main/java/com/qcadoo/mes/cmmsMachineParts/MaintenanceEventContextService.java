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
package com.qcadoo.mes.cmmsMachineParts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventContextFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.criteriaModifiers.EventCriteriaModifiersCMP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.form.FormComponentState;

@Service
public class MaintenanceEventContextService {

    private static final String L_FORM = "form";

    private static final String L_GRID = "grid";

    private static final String L_PLANNED_EVENT = "plannedEvent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void confirmOrChangeContext(ViewDefinitionState view, ComponentState componentState, String[] args) {
        FormComponent formComponent = (FormComponentState) view.getComponentByReference(L_FORM);
        Entity maintenanceEventContextEntity = prepareContextEntity(formComponent.getEntity());

        if (maintenanceEventContextEntity.getBooleanField(MaintenanceEventContextFields.CONFIRMED)) {
            maintenanceEventContextEntity = changeContext(view, maintenanceEventContextEntity);
        } else {
            maintenanceEventContextEntity = confirmContext(maintenanceEventContextEntity, args);
        }

        formComponent.setEntity(maintenanceEventContextEntity);
    }

    private Entity changeContext(ViewDefinitionState view, Entity maintenanceEventContextEntity) {
        FormComponent formComponent = (FormComponentState) view.getComponentByReference(L_FORM);

        maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, false);
        maintenanceEventContextEntity = maintenanceEventContextEntity.getDataDefinition().save(maintenanceEventContextEntity);
        formComponent.setEntity(maintenanceEventContextEntity);

        return maintenanceEventContextEntity;
    }

    private Entity confirmContext(Entity maintenanceEventContextEntity, String[] args) {
        maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, true);
        maintenanceEventContextEntity = maintenanceEventContextEntity.getDataDefinition().save(maintenanceEventContextEntity);

        Long maintenanceEventContextEntityId = maintenanceEventContextEntity.getId();

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("contextId", maintenanceEventContextEntityId);

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

        if (args.length > 0 && args[0].equals(L_PLANNED_EVENT)) {
            String sql = "update cmmsmachineparts_plannedevent set plannedeventcontext_id = :contextId";

            jdbcTemplate.update(sql, namedParameters);

        } else {
            String sql = "update cmmsmachineparts_maintenanceevent set maintenanceeventcontext_id = :contextId";

            jdbcTemplate.update(sql, namedParameters);
        }

        return maintenanceEventContextEntity;
    }

    public Entity prepareContextEntity(Entity maintenanceEventContextEntity) {
        SearchCriteriaBuilder searchCriteriaBuilder = maintenanceEventContextEntity.getDataDefinition().find();
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(MaintenanceEventContextFields.DIVISION,
                maintenanceEventContextEntity.getBelongsToField(MaintenanceEventContextFields.DIVISION)));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(MaintenanceEventContextFields.FACTORY,
                maintenanceEventContextEntity.getBelongsToField(MaintenanceEventContextFields.FACTORY)));

        Entity maintenanceEventContextEntityFromDb = searchCriteriaBuilder.uniqueResult();

        if (maintenanceEventContextEntityFromDb == null) {
            maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, false);
            maintenanceEventContextEntity = maintenanceEventContextEntity.getDataDefinition().save(maintenanceEventContextEntity);
        } else {
            Long id = maintenanceEventContextEntity.getId();
            maintenanceEventContextEntity = maintenanceEventContextEntityFromDb;

            if (id == null) {
                maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, false);
            }
        }

        return maintenanceEventContextEntity;
    }

    public void beforeRenderListView(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        Entity maintenanceEventContext = formComponent.getEntity();

        if (maintenanceEventContext.getBooleanField(MaintenanceEventContextFields.CONFIRMED)) {
            prepareViewWithContext(view, maintenanceEventContext);
        } else {
            prepareViewWithEmptyContext(view, maintenanceEventContext);
        }
    }

    private void prepareViewWithContext(ViewDefinitionState view, Entity maintenanceEventContext) {
        setEnableOfRibbonActions(view, true);
        setEnableOfContextTab(view, false);
        setEnableOfMainTab(view, true);

        setGridFilterParameters(view, maintenanceEventContext);
    }

    private void prepareViewWithEmptyContext(ViewDefinitionState view, Entity maintenanceEventContext) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        grid.setEntities(Arrays.asList());

        setEnableOfRibbonActions(view, false);
        setEnableOfContextTab(view, true);
        setEnableOfMainTab(view, false);
    }

    private void setEnableOfContextTab(ViewDefinitionState view, boolean enabled) {
        view.<FieldComponent> tryFindComponentByReference(MaintenanceEventContextFields.FACTORY).orNull().setEnabled(enabled);
        view.<FieldComponent> tryFindComponentByReference(MaintenanceEventContextFields.DIVISION).orNull().setEnabled(enabled);
    }

    private void setEnableOfMainTab(ViewDefinitionState view, boolean enabled) {
        view.getComponentByReference(L_GRID).setEnabled(enabled);
    }

    private void setEnableOfRibbonActions(ViewDefinitionState viewDefinitionState, boolean enabled) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();

        RibbonGroup customActions = ribbon.getGroupByName("customActions");

        if (customActions == null) {
            return;
        }

        for (RibbonActionItem ribbonActionItem : customActions.getItems()) {
            ribbonActionItem.setEnabled(enabled);
            ribbonActionItem.requestUpdate(true);
        }
    }

    private void setGridFilterParameters(ViewDefinitionState view, Entity maintenanceEventContext) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(L_GRID);

        FilterValueHolder filterValueHolder = gridComponent.getFilterValue();

        Entity factoryEntity = maintenanceEventContext.getBelongsToField(MaintenanceEventContextFields.FACTORY);

        if (factoryEntity != null) {
            filterValueHolder.put(EventCriteriaModifiersCMP.L_MAINTENANCE_EVENT_CONTEXT_FACTORY,
                    Math.toIntExact(factoryEntity.getId()));
        }

        Entity divisionEntity = maintenanceEventContext.getBelongsToField(MaintenanceEventContextFields.DIVISION);

        if (divisionEntity != null) {
            filterValueHolder.put(EventCriteriaModifiersCMP.L_MAINTENANCE_EVENT_CONTEXT_DIVISION,
                    Math.toIntExact(divisionEntity.getId()));
        }

        gridComponent.setFilterValue(filterValueHolder);
    }

    public void onSelectedEventChange(ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        DataDefinition maintenanceEventDD = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT);

        for (Entity eventEntity : grid.getSelectedEntities()) {
            eventEntity.setField(MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT, form.getEntityId());

            maintenanceEventDD.save(eventEntity);
        }
    }

    public Entity getCurrentContext(ViewDefinitionState viewDefinitionState, ComponentState triggerState, String[] args) {
        FormComponent formComponent = (FormComponentState) viewDefinitionState.getComponentByReference(L_FORM);

        return prepareContextEntity(formComponent.getEntity());
    }

}
