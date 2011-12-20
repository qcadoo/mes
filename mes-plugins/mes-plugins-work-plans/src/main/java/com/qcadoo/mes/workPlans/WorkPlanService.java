/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.util.RibbonReportService;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.mes.workPlans.print.WorkPlanPdfService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public final class WorkPlanService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private WorkPlanPdfService workPlanPdfService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RibbonReportService ribbonReportService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);
        entity.setField("worker", null);
        return true;
    }

    public void generateWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity workPlan = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN)
                    .get((Long) state.getFieldValue());

            if (workPlan == null) {
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(workPlan.getStringField("fileName"))) {
                String message = translationService.translate("workPlans.workPlanDetails.window.workPlan.documentsWasGenerated",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (workPlan.getHasManyField("orders").isEmpty()) {
                String message = translationService.translate("workPlans.workPlan.window.workPlan.missingAssosiatedOrders",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            workPlan = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN).get(
                    (Long) state.getFieldValue());

            try {
                generateWorkPlanDocuments(state, workPlan);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void setGenerateButtonState(final ViewDefinitionState state) {
        ribbonReportService.setGenerateButtonState(state, state.getLocale(), WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
    }

    public void setGridGenerateButtonState(final ViewDefinitionState state) {
        ribbonReportService.setGridGenerateButtonState(state, state.getLocale(), WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN);
    }

    public void printWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

        if (state.getFieldValue() instanceof Long) {
            Entity workPlan = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN)
                    .get((Long) state.getFieldValue());
            if (workPlan == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(workPlan.getStringField("fileName"))) {
                state.addMessage(
                        translationService.translate("workPlans.workPlan.window.workPlan.documentsWasNotGenerated",
                                state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/generateSavedReport/" + WorkPlansConstants.PLUGIN_IDENTIFIER + "/"
                        + WorkPlansConstants.MODEL_WORK_PLAN + "." + args[0] + "?id=" + state.getFieldValue()
                        + "&fieldDate=date&suffix=" + args[1], true, false);
            }
        } else {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("qcadooView.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("qcadooView.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void disableFormForExistingWorkPlan(final ViewDefinitionState state) {
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

    public boolean checkWorkPlanComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField("order");
        Entity workPlan = entity.getBelongsToField("workPlan");

        if (workPlan == null || order == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find().belongsTo("order", order.getId())
                .belongsTo("workPlan", workPlan.getId()).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("order"), "workPlans.validate.global.error.workPlanDuplicated");
            return false;
        } else {
            return true;
        }
    }

    @Transactional
    private void generateWorkPlanDocuments(final ComponentState state, final Entity workPlan) throws IOException,
            DocumentException {
        Entity workPlanWithFileName = workPlanPdfService.updateFileName(workPlan, "Work_plan");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        workPlanPdfService.generateDocument(workPlanWithFileName, company, state.getLocale());
    }
}
