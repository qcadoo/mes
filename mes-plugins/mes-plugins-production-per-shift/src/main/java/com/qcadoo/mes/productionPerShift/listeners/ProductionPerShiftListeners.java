/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.productionPerShift.listeners;

import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFields.HAS_CORRECTIONS;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFields.PROGRESS_FOR_DAYS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionPerShiftListeners {

    private static final String PROGRESS_FOR_DAYS_ADL = "progressForDaysADL";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PPSHelper helper;

    @Autowired
    private ProductionPerShiftDetailsHooks detailsHooks;

    public void redirectToProductionPerShift(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Long orderId = (Long) componentState.getFieldValue();

        if (orderId == null) {
            return;
        }

        long ppsId = createCorrespondingProductionPerShfitEntity(orderId);

        redirect(viewState, ppsId);
    }

    void redirect(final ViewDefinitionState viewState, final Long ppsId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", ppsId);

        String url = "../page/productionPerShift/productionPerShiftDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    private long createCorrespondingProductionPerShfitEntity(final Long orderId) {
        DataDefinition orderDD = dataDefinitionService.get("orders", "order");
        Entity order = orderDD.get(orderId);

        DataDefinition ppsDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_PRODUCTION_PER_SHIFT);

        Entity pps = getPps(order, ppsDD);

        if (pps == null) {
            pps = ppsDD.create();
            pps.setField("order", order);
            ppsDD.save(pps);
        }

        return getPps(order, ppsDD).getId();
    }

    private Entity getPps(final Entity order, final DataDefinition ppsDD) {
        return ppsDD.find().add(SearchRestrictions.belongsTo("order", order)).uniqueResult();
    }

    public void fillProducedField(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        detailsHooks.fillProducedField(viewState);
    }

    /**
     * Fill outer AwesomeDynamicList with entities fetched from db. Disable ADL if operation lookup is empty.
     * 
     * @param viewState
     * @param componentState
     * @param args
     */
    public void fillProgressForDays(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        detailsHooks.fillProgressForDays(viewState);
    }

    /**
     * Save outer AwesomeDynamicList entities in db and reset operation lookup & related components
     * 
     * @param viewState
     * @param componentState
     * @param args
     */
    public void saveProgressForDays(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference(PROGRESS_FOR_DAYS_ADL);
        @SuppressWarnings("unchecked")
        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();
        String plannedProgressType = viewState.getComponentByReference(PLANNED_PROGRESS_TYPE).getFieldValue().toString();
        for (Entity progressForDay : progressForDays) {
            progressForDay.setField(CORRECTED, plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
        }
        Entity tioc = ((LookupComponent) viewState.getComponentByReference("productionPerShiftOperation")).getEntity();
        boolean hasCorrections = helper.shouldHasCorrections(viewState);
        if (tioc != null) {
            tioc.setField(HAS_CORRECTIONS, hasCorrections);
            tioc.setField(PROGRESS_FOR_DAYS, prepareProgressForDaysForTIOC(tioc, hasCorrections, progressForDays));
            tioc = tioc.getDataDefinition().save(tioc);
            if (!tioc.isValid()) {
                List<ErrorMessage> errors = tioc.getGlobalErrors();
                for (ErrorMessage error : errors) {
                    componentState.addMessage(error.getMessage(), MessageType.FAILURE, error.getVars());
                }
            }
            if (componentState.isHasError()) {
                componentState.performEvent(viewState, "initialize", new String[0]);
            } else {
                componentState.performEvent(viewState, "save");
            }
        }
    }

    private List<Entity> prepareProgressForDaysForTIOC(final Entity tioc, final boolean hasCorrections,
            final List<Entity> progressForDays) {
        Entity techInstOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(tioc.getId());
        List<Entity> plannedPrograssForDay = techInstOperComp.getHasManyField(PROGRESS_FOR_DAYS).find()
                .add(SearchRestrictions.eq(CORRECTED, !hasCorrections)).list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

    private List<Entity> addCorrectedToPlannedProgressForDay(final Entity tioc, final List<Entity> progressForDays) {
        Entity techInstOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(tioc.getId());
        List<Entity> plannedPrograssForDay = techInstOperComp.getHasManyField(PROGRESS_FOR_DAYS).find().list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

    public void changeView(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        detailsHooks.disablePlannedProgressTypeForPendingOrder(viewState);
        detailsHooks.disableReasonOfCorrection(viewState);
        detailsHooks.fillProgressForDays(viewState);
    }

    public void copyFromPlanned(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        DataDefinition progressForDayDD = dataDefinitionService.get(PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
        Entity tioc = ((LookupComponent) viewState.getComponentByReference("productionPerShiftOperation")).getEntity();
        if (tioc == null) {
            return;
        } else {
            String plannedProgressType = ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE))
                    .getFieldValue().toString();
            List<Entity> progressForDays = getProgressForDayFromTIOC(tioc,
                    plannedProgressType.equals(PlannedProgressType.PLANNED.getStringValue()));
            List<Entity> copiedProgressForDays = new ArrayList<Entity>();
            for (Entity progressForDay : progressForDays) {
                Entity copyProgressForDay = progressForDayDD.copy(progressForDay.getId()).get(0);
                copyProgressForDay.setField(CORRECTED, true);
                copiedProgressForDays.add(copyProgressForDay);
            }
            tioc.setField(HAS_CORRECTIONS, true);
            deleteProgressForDays(viewState, tioc);
            tioc.setField(PROGRESS_FOR_DAYS, addCorrectedToPlannedProgressForDay(tioc, copiedProgressForDays));
            tioc.getDataDefinition().save(tioc);
        }
        detailsHooks.fillProgressForDays(viewState);
    }

    public void deleteProgressForDays(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Entity tioc = ((LookupComponent) viewState.getComponentByReference("productionPerShiftOperation")).getEntity();
        if (tioc == null) {
            return;
        } else {
            deleteProgressForDays(viewState, tioc);
        }
        detailsHooks.fillProgressForDays(viewState);
    }

    private void deleteProgressForDays(final ViewDefinitionState viewState, final Entity tioc) {
        String plannedProgressType = ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue()
                .toString();
        List<Entity> progressForDays = getProgressForDayFromTIOC(tioc,
                plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
        for (Entity progressForDay : progressForDays) {
            progressForDay.getDataDefinition().delete(progressForDay.getId());
        }
        tioc.getDataDefinition().save(tioc);
    }

    private List<Entity> getProgressForDayFromTIOC(final Entity tioc, final boolean corrected) {
        return tioc.getHasManyField(PROGRESS_FOR_DAYS).find().add(SearchRestrictions.eq(CORRECTED, corrected)).list()
                .getEntities();
    }
}
