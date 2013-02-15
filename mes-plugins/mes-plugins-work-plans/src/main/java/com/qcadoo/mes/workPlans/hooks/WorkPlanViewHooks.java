/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.workPlans.hooks;

import static com.qcadoo.mes.workPlans.constants.WorkPlanFields.DATE;
import static com.qcadoo.mes.workPlans.constants.WorkPlanFields.DONT_PRINT_ORDERS_IN_WORK_PLANS;
import static com.qcadoo.mes.workPlans.constants.WorkPlanFields.GENERATED;
import static com.qcadoo.mes.workPlans.constants.WorkPlanFields.NAME;
import static com.qcadoo.mes.workPlans.constants.WorkPlanFields.TYPE;
import static com.qcadoo.mes.workPlans.constants.WorkPlanFields.WORKER;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.util.OrderHelperService;
import com.qcadoo.mes.orders.util.RibbonReportService;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
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
    private WorkPlansService workPlanService;

    @Autowired
    private RibbonReportService ribbonReportService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderHelperService orderHelperService;

    @Autowired
    private ParameterService parameterService;

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

    public final void disableFormForGeneratedWorkPlan(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        FieldComponent generated = (FieldComponent) view.getComponentByReference(GENERATED);

        if (form == null) {
            return;
        }

        if (form.getEntityId() == null) {
            view.getComponentByReference("workPlanComponents").setEnabled(false);
            view.getComponentByReference("columnsForOrders").setEnabled(false);
        } else {
            if ("1".equals(generated.getFieldValue())) {
                view.getComponentByReference(NAME).setEnabled(false);
                view.getComponentByReference(TYPE).setEnabled(false);
                view.getComponentByReference("workPlanComponents").setEnabled(false);
                view.getComponentByReference(DONT_PRINT_ORDERS_IN_WORK_PLANS).setEnabled(false);
                view.getComponentByReference("columnsForOrders").setEnabled(false);
            } else {
                view.getComponentByReference(NAME).setEnabled(true);
                view.getComponentByReference(TYPE).setEnabled(true);
                view.getComponentByReference("workPlanComponents").setEnabled(true);
                view.getComponentByReference(DONT_PRINT_ORDERS_IN_WORK_PLANS).setEnabled(true);
                view.getComponentByReference("columnsForOrders").setEnabled(true);
            }
        }
    }

    public final void setGridGenerateButtonState(final ViewDefinitionState state) {
        ribbonReportService.setGridGenerateButtonState(state, state.getLocale(), WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
    }

    @Transactional
    public final void generateWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference(GENERATED);
            ComponentState date = viewDefinitionState.getComponentByReference(DATE);
            ComponentState worker = viewDefinitionState.getComponentByReference(WORKER);

            Entity workPlan = workPlanService.getWorkPlan((Long) state.getFieldValue());

            if (workPlan == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.isNotBlank(workPlan.getStringField("fileName"))) {
                state.addMessage("workPlans.workPlanDetails.window.workPlan.documentsWasGenerated", MessageType.FAILURE);
                return;
            }
            List<Entity> orders = workPlan.getManyToManyField(WorkPlanFields.ORDERS);
            if (orders == null) {
                state.addMessage("workPlans.workPlanDetails.window.workPlan.missingAssosiatedOrders", MessageType.FAILURE);
                return;
            }
            List<String> numbersOfOrdersWithoutTechnology = orderHelperService.getOrdersWithoutTechnology(orders);
            if (!numbersOfOrdersWithoutTechnology.isEmpty()) {
                state.addMessage("workPlans.workPlanDetails.window.workPlan.missingTechnologyInOrders", MessageType.FAILURE,
                        StringUtils.join(numbersOfOrdersWithoutTechnology, ",<br>"));
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, viewDefinitionState.getLocale())
                        .format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            workPlan = workPlanService.getWorkPlan((Long) state.getFieldValue());

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
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN });
    }

    public final void setWorkPlanDefaultValues(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        setWorkPlanDefaultValues(view);
    }

    public final void setWorkPlanDefaultValues(final ViewDefinitionState view) {
        FormComponent form = getForm(view);

        if (form.getEntityId() == null) {
            FieldComponent field = getFieldComponent(view, DONT_PRINT_ORDERS_IN_WORK_PLANS);
            field.setFieldValue(parameterService.getParameter().getField(DONT_PRINT_ORDERS_IN_WORK_PLANS));
        }
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

}
