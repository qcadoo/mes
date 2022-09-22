package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.OrdersToolRequirementFields;
import com.qcadoo.mes.cmmsMachineParts.helpers.OrdersToolRequirementHelper;
import com.qcadoo.mes.cmmsMachineParts.reports.pdf.OrdersToolRequirementPdfService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class OrdersToolRequirementDetailsListeners {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private FileService fileService;

    @Autowired
    private OrdersToolRequirementHelper ordersToolRequirementHelper;

    @Autowired
    private OrdersToolRequirementPdfService ordersToolRequirementPdfService;

    public void generateOrdersToolRequirement(final ViewDefinitionState view, final ComponentState state,
                                              final String[] args) {
        FormComponent ordersToolRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(OrdersToolRequirementFields.ORDERS);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OrdersToolRequirementFields.GENERATED);
        FieldComponent workerField = (FieldComponent) view.getComponentByReference(OrdersToolRequirementFields.WORKER);
        FieldComponent dateField = (FieldComponent) view.getComponentByReference(OrdersToolRequirementFields.DATE);

        List<Entity> orders = ordersGrid.getEntities();

        if (validateOrdersToolRequirement(ordersToolRequirementForm, orders)) {
            workerField.setFieldValue(securityService.getCurrentUserName());
            dateField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            generatedCheckBox.setChecked(true);

            Entity ordersToolRequirement = ordersToolRequirementForm.getEntity();

            List<Entity> ordersToolRequirementTools = ordersToolRequirementHelper
                    .generateOrdersToolRequirementTools(ordersToolRequirement, orders);

            ordersToolRequirement.setField(OrdersToolRequirementFields.ORDERS_TOOL_REQUIREMENT_TOOLS,
                    ordersToolRequirementTools);

            ordersToolRequirement = ordersToolRequirement.getDataDefinition().save(ordersToolRequirement);

            ordersToolRequirementForm.setEntity(ordersToolRequirement);

            Entity ordersToolRequirementWithFileName = fileService.updateReportFileName(ordersToolRequirement,
                    OrdersToolRequirementFields.DATE, "cmmsMachineParts.ordersToolRequirement.report.fileName");

            try {
                ordersToolRequirementPdfService.generateDocument(ordersToolRequirementWithFileName, state.getLocale(), PageSize.A4.rotate());
            } catch (IOException | DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            view.addMessage("cmmsMachineParts.ordersToolRequirement.generate.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("cmmsMachineParts.ordersToolRequirement.generate.failure", ComponentState.MessageType.FAILURE);
        }
    }

    private boolean validateOrdersToolRequirement(final FormComponent ordersToolRequirementForm, final List<Entity> orders) {
        boolean isValid = true;

        if (orders.isEmpty()) {
            ordersToolRequirementForm.addMessage("cmmsMachineParts.ordersToolRequirement.orders.empty", ComponentState.MessageType.FAILURE);

            isValid = false;
        }

        return isValid;
    }

    public void printOrdersToolRequirement(final ViewDefinitionState view, final ComponentState state,
                                           final String[] args) {
        reportService.printGeneratedReport(view, state, new String[]{args[0], CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_ORDERS_TOOL_REQUIREMENT});
    }

}
