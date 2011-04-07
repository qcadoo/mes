/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.util.OrderReportService;
import com.qcadoo.mes.orders.util.RibbonReportService;
import com.qcadoo.mes.workPlans.print.pdf.WorkPlanForMachinePdfService;
import com.qcadoo.mes.workPlans.print.pdf.WorkPlanForProductPdfService;
import com.qcadoo.mes.workPlans.print.pdf.WorkPlanForWorkerPdfService;
import com.qcadoo.mes.workPlans.print.xls.WorkPlanForMachineXlsService;
import com.qcadoo.mes.workPlans.print.xls.WorkPlanForProductXlsService;
import com.qcadoo.mes.workPlans.print.xls.WorkPlanForWorkerXlsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.utils.DateUtils;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.components.FieldComponentState;
import com.qcadoo.view.components.form.FormComponentState;

@Service
public final class WorkPlanService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private WorkPlanForWorkerPdfService workPlanForWorkerPdfService;

    @Autowired
    private WorkPlanForMachinePdfService workPlanForMachinePdfService;

    @Autowired
    private WorkPlanForProductPdfService workPlanForProductPdfService;

    @Autowired
    private WorkPlanForWorkerXlsService workPlanForWorkerXlsService;

    @Autowired
    private WorkPlanForMachineXlsService workPlanForMachineXlsService;

    @Autowired
    private WorkPlanForProductXlsService workPlanForProductXlsService;

    @Value("${reportPath}")
    private String path;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RibbonReportService ribbonReportService;

    @Autowired
    private OrderReportService orderReportService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);
        entity.setField("worker", null);
        return true;
    }

    public void generateWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state instanceof FormComponentState) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity workPlan = dataDefinitionService.get("workPlans", "workPlan").get((Long) state.getFieldValue());

            if (workPlan == null) {
                String message = translationService.translate("core.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(workPlan.getStringField("fileName"))) {
                String message = translationService.translate("workPlans.workPlan.window.workPlan.documentsWasGenerated",
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

            if (state.getFieldValue() == null || !((FormComponentState) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            workPlan = dataDefinitionService.get("workPlans", "workPlan").get((Long) state.getFieldValue());

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

    public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale) {
        ribbonReportService.setGenerateButtonState(state, locale, "workPlans", "workPlan");
    }

    public void setGridGenerateButtonState(final ViewDefinitionState state, final Locale locale) {
        ribbonReportService.setGridGenerateButtonState(state, locale, "workPlans", "workPlan");
    }

    public void printWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

        if (state.getFieldValue() instanceof Long) {
            Entity workPlan = dataDefinitionService.get("workPlans", "workPlan").get((Long) state.getFieldValue());
            if (workPlan == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(workPlan.getStringField("fileName"))) {
                state.addMessage(
                        translationService.translate("workPlans.workPlan.window.workPlan.documentsWasNotGenerated",
                                state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/workPlans/workPlan" + args[1] + "." + args[0] + "?id=" + state.getFieldValue(),
                        false, false);
            }
        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void disableFormForExistingWorkPlan(final ViewDefinitionState state, final Locale locale) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState workPlanComponents = state.getComponentByReference("workPlanComponents");
        FieldComponentState generated = (FieldComponentState) state.getComponentByReference("generated");

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

        SearchResult searchResult = dataDefinition.find()
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("order"), order.getId()))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("workPlan"), workPlan.getId())).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("order"), "workPlans.validate.global.error.workPlanDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public void printWorkPlanForOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Entity workPlan = orderReportService.printWorkPlanForOrder(state);
        if (workPlan == null) {
            return;
        }
        try {
            generateWorkPlanDocuments(state, workPlan);

            viewDefinitionState.redirectTo("/workPlans/workPlan" + args[1] + "." + args[0] + "?id=" + workPlan.getId(), false,
                    false);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void generateWorkPlanDocuments(final ComponentState state, final Entity workPlan) throws IOException,
            DocumentException {
        Entity workPlanWithFileName = updateFileName(workPlan, getFullFileName((Date) workPlan.getField("date"), "Work_plan"),
                "workPlan");
        workPlanForMachinePdfService.generateDocument(workPlanWithFileName, state.getLocale());
        workPlanForMachineXlsService.generateDocument(workPlanWithFileName, state.getLocale());
        workPlanForWorkerPdfService.generateDocument(workPlanWithFileName, state.getLocale());
        workPlanForWorkerXlsService.generateDocument(workPlanWithFileName, state.getLocale());
        workPlanForProductPdfService.generateDocument(workPlanWithFileName, state.getLocale());
        workPlanForProductXlsService.generateDocument(workPlanWithFileName, state.getLocale());
    }

    private String getFullFileName(final Date date, final String fileName) {
        return path + fileName + "_" + new SimpleDateFormat(DateUtils.REPORT_DATE_TIME_FORMAT).format(date) + "_";
    }

    private Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get("workPlans", entityName).save(entity);
    }

}
