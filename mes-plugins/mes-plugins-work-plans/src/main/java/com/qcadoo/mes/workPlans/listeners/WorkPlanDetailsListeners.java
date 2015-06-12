/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.workPlans.listeners;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.util.OrderHelperService;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.mes.workPlans.print.WorkPlanForDivisionPdfService;
import com.qcadoo.mes.workPlans.print.WorkPlanPdfService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class WorkPlanDetailsListeners {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private WorkPlansService workPlanService;

    @Autowired
    private WorkPlanPdfService workPlanPdfService;

    @Autowired
    private WorkPlanForDivisionPdfService workPlanForDivisionPdfService;

    @Autowired
    private OrderHelperService orderHelperService;

    @Autowired
    private BarcodeOperationComponentService barcodeOperationComponentService;

    @Transactional
    public void generateWorkPlan(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            FieldComponent generatedField = (FieldComponent) view.getComponentByReference(WorkPlanFields.GENERATED);
            FieldComponent dateField = (FieldComponent) view.getComponentByReference(WorkPlanFields.DATE);
            FieldComponent workerField = (FieldComponent) view.getComponentByReference(WorkPlanFields.WORKER);

            Entity workPlan = workPlanService.getWorkPlan((Long) state.getFieldValue());

            if (workPlan == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.isNotBlank(workPlan.getStringField(WorkPlanFields.FILE_NAME))) {
                state.addMessage("workPlans.workPlanDetails.window.workPlan.documentsWasGenerated", MessageType.FAILURE);
                return;
            }

            List<Entity> orders = workPlan.getManyToManyField(WorkPlanFields.ORDERS);

            if (orders == null) {
                state.addMessage("workPlans.workPlanDetails.window.workPlan.missingAssosiatedOrders", MessageType.FAILURE);
                return;
            }

            createBarcodeOCForOrders(orders);

            List<String> numbersOfOrdersWithoutTechnology = orderHelperService.getOrdersWithoutTechnology(orders);

            if (!numbersOfOrdersWithoutTechnology.isEmpty()) {
                state.addMessage("workPlans.workPlanDetails.window.workPlan.missingTechnologyInOrders", MessageType.FAILURE,
                        StringUtils.join(numbersOfOrdersWithoutTechnology, ",<br>"));
                return;
            }

            if ("0".equals(generatedField.getFieldValue())) {
                workerField.setFieldValue(securityService.getCurrentUserName());
                generatedField.setFieldValue("1");
                dateField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, view.getLocale()).format(new Date()));
            }

            state.performEvent(view, "save", new String[0]);

            workPlan = workPlanService.getWorkPlan((Long) state.getFieldValue());

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                workerField.setFieldValue(null);
                generatedField.setFieldValue("0");
                dateField.setFieldValue(null);
                return;
            }

            try {
                generateWorkPlanDocuments(state, workPlan);
                state.performEvent(view, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    @Transactional
    private void generateWorkPlanDocuments(final ComponentState state, final Entity workPlan) throws IOException,
            DocumentException {
        Entity workPlanWithFilename = fileService.updateReportFileName(workPlan, WorkPlanFields.DATE,
                "workPlans.workPlan.report.fileName");
        workPlanPdfService.generateDocument(workPlanWithFilename, state.getLocale());
        if (workPlan.getStringField(WorkPlanFields.TYPE).compareTo(WorkPlanType.BY_DIVISION.getStringValue()) == 0) {
            String fileNameForDivision = "workPlans.workPlan.report.fileNameForDivision";
            Entity workPlanForDivision = fileService.updateReportFileName(workPlanWithFilename, WorkPlanFields.DATE,
                    fileNameForDivision);
            workPlanForDivisionPdfService.generateDocument(workPlanForDivision, state.getLocale(), fileNameForDivision,
                    PageSize.A4.rotate());
        }
    }

    public void printWorkPlan(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_WORK_PLAN, args[1] });
    }

    public void printAtachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("technologyAttachments");
        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("technologies.technologyDetails.window.ribbon.atachments.nonSelectedAtachment", MessageType.INFO);
            return;
        }
        StringBuffer redirectUrl = new StringBuffer();
        redirectUrl.append("/rest/workplans/printAtachment.pdf");
        boolean isFirstParam = true;
        for (Long confectionProtocolId : grid.getSelectedEntitiesIds()) {
            if (isFirstParam) {
                redirectUrl.append("?");
                isFirstParam = false;
            } else {
                redirectUrl.append("&");
            }
            redirectUrl.append("id=");
            redirectUrl.append(confectionProtocolId);
        }
        view.redirectTo(redirectUrl.toString(), true, false);
    }

    private void createBarcodeOCForOrders(final List<Entity> orders) {
        for (Entity order : orders) {
            createBarcodeOCForOrder(order);
        }

    }

    private void createBarcodeOCForOrder(final Entity order) {
        List<Entity> tocs = order.getBelongsToField(OrderFields.TECHNOLOGY)
                .getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        for (Entity toc : tocs) {
            barcodeOperationComponentService.createBarcodeOperationComponent(toc);
        }
    }

}
