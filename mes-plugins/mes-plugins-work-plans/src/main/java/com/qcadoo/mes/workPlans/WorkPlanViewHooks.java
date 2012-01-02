package com.qcadoo.mes.workPlans;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

    public final void addSelectedOrdersToWorkPlan(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        GridComponent grid = (GridComponent) component;

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put("entities", grid.getSelectedEntitiesIds());

        Map<String, Object> navigationParameters = Maps.newHashMap();
        navigationParameters.put("workPlanComponents.options", gridOptions);
        navigationParameters.put("window.activeMenu", "reports.workPlans");

        view.redirectTo("/page/workPlans/workPlanDetails.html", false, true, navigationParameters);
    }

    public final void disableFormForGeneratedWorkPlan(final ViewDefinitionState state) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState workPlanComponents = state.getComponentByReference("workPlanComponents");
        FieldComponent generated = (FieldComponent) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            workPlanComponents.setEnabled(false);
        } else {
            name.setEnabled(true);
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
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, Locale.getDefault()).format(new Date()));
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
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, Locale.getDefault()).format(new Date()));
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
        if (state.getFieldValue() instanceof Long) {
            Long workPlanId = (Long) state.getFieldValue();
            Entity workPlan = workPlanService.getWorkPlan(workPlanId);
            if (workPlan == null) {
                addFailureMessage(state, "qcadooView.message.entityNotFound");
            } else if (StringUtils.hasText(workPlan.getStringField("fileName"))) {
                String url = getUrlForReport((Long) state.getFieldValue());
                viewDefinitionState.redirectTo(url, true, false);
            } else {
                addFailureMessage(state, "workPlans.workPlan.window.workPlan.documentsWasNotGenerated");
            }
        } else {
            if (state instanceof FormComponent) {
                addFailureMessage(state, "qcadooView.form.entityWithoutIdentifier");
            } else {
                addFailureMessage(state, "qcadooView.grid.noRowSelectedError");
            }
        }
    }

    private void addFailureMessage(final ComponentState component, final String messageKey) {
        component.addMessage(translationService.translate(messageKey, component.getLocale()), MessageType.FAILURE);
    }

    private String getUrlForReport(final Long workPlanId) {
        StringBuilder url = new StringBuilder("/generateSavedReport/");
        url.append(WorkPlansConstants.PLUGIN_IDENTIFIER);
        url.append("/");
        url.append(WorkPlansConstants.MODEL_WORK_PLAN);
        url.append(".pdf?id=");
        url.append(workPlanId);
        url.append("&fieldDate=date");
        return url.toString();
    }
}
