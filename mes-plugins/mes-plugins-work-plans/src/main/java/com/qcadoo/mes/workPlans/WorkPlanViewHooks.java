/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.workPlans;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.util.RibbonReportService;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class WorkPlanViewHooks {

    @Autowired
    private WorkPlanService workPlanService;

    @Autowired
    private RibbonReportService ribbonReportService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportService reportService;

    public final void addSelectedOrdersToWorkPlan(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        GridComponent grid = (GridComponent) component;

        List<Entity> orders = workPlanService.getSelectedOrders(grid.getSelectedEntitiesIds());
        Entity workPlan = workPlanService.generateWorkPlanEntity(orders);

        Map<String, Object> navigationParameters = Maps.newHashMap();
        navigationParameters.put("form.id", workPlan.getId());
        navigationParameters.put("window.activeMenu", "reports.workPlans");

        view.redirectTo("/page/workPlans/workPlanDetails.html", false, true, navigationParameters);
    }

    public final void disableFormForGeneratedWorkPlan(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent generated = (FieldComponent) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            form.setFormEnabled(false);
        }
    }

    public final void setGridGenerateButtonState(final ViewDefinitionState state) {
        ribbonReportService.setGridGenerateButtonState(state, state.getLocale(), WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
    }

    public final void generateTestWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, viewDefinitionState.getLocale())
                        .format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            Entity workPlan = workPlanService.getWorkPlan((Long) state.getFieldValue());

            try {
                workPlanService.generateWorkPlanDocuments(state, workPlan);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public final void generateWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity workPlan = workPlanService.getWorkPlan((Long) state.getFieldValue());

            if (workPlan == null) {
                addFailureMessage(state, "qcadooView.message.entityNotFound");
                return;
            } else if (StringUtils.hasText(workPlan.getStringField("fileName"))) {
                addFailureMessage(state, "workPlans.workPlanDetails.window.workPlan.documentsWasGenerated");
                return;
            } else if (workPlan.getHasManyField("orders") == null) {
                addFailureMessage(state, "workPlans.workPlan.window.workPlan.missingAssosiatedOrders");
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, viewDefinitionState.getLocale())
                        .format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            try {
                workPlanService.generateWorkPlanDocuments(state, workPlan);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public final void printWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        args[1] = WorkPlansConstants.PLUGIN_IDENTIFIER;
        args[2] = WorkPlansConstants.MODEL_WORK_PLAN;
        reportService.printGeneratedReport(viewDefinitionState, state, args);
    }

    private void addFailureMessage(final ComponentState component, final String messageKey) {
        component.addMessage(translationService.translate(messageKey, component.getLocale()), MessageType.FAILURE);
    }

}
