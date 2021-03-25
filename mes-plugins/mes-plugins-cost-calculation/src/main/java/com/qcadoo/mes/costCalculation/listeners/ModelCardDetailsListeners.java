package com.qcadoo.mes.costCalculation.listeners;

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.ModelCardFields;
import com.qcadoo.mes.costCalculation.print.ModelCardPdfService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ModelCardDetailsListeners {

    @Autowired
    private ReportService reportService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ModelCardPdfService modelCardPdfService;

    public void printModelCard(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state,
                new String[] { args[0], CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_MODEL_CARD });
    }

    @Transactional
    public void generateModelCard(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference(ModelCardFields.GENERATED);
        FieldComponent workerField = (FieldComponent) view.getComponentByReference(ModelCardFields.WORKER);
        FieldComponent dateField = (FieldComponent) view.getComponentByReference(ModelCardFields.DATE);

        workerField.setFieldValue(securityService.getCurrentUserName());
        dateField.setFieldValue(DateUtils.toDateTimeString(new Date()));
        generated.setChecked(true);

        Entity modelCard = form.getEntity();

        modelCard = modelCard.getDataDefinition().save(modelCard);

        form.setEntity(modelCard);

        Entity modelCardWithFileName = fileService.updateReportFileName(modelCard, ModelCardFields.DATE,
                "costCalculation.modelCard.report.fileName");

        try {
            modelCardPdfService.generateDocument(modelCardWithFileName, state.getLocale(), PageSize.A4.rotate());
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        view.addMessage("costCalculation.modelCard.generate.success", ComponentState.MessageType.SUCCESS);
    }
}
