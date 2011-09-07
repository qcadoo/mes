package com.qcadoo.mes.costCalculation.print;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostCalculationReportService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    TranslationService translationService;

    @Autowired
    CostCalculationPdfService costCalculationPdfService;

    @Value("${reportPath}")
    private String path;

    public void printCostCalculation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity costCalculation = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                    CostCalculateConstants.MODEL_COST_CALCULATION).get((Long) state.getFieldValue());
            if (costCalculation == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(costCalculation.getStringField("fileName"))) {
                state.addMessage(translationService.translate(
                        "costCalculation.costCalculationDetails.window.costCalculation.documentsWasNotGenerated",
                        state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/costCalculation/costCalculation." + args[0] + "?id=" + state.getFieldValue(),
                        false, false);
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

    public void generateCostCalculation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState date = viewDefinitionState.getComponentByReference("dateOfCalculation");
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            Entity costCalculation = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                    CostCalculateConstants.MODEL_COST_CALCULATION).get((Long) state.getFieldValue());

            if (costCalculation == null) {
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(costCalculation.getStringField("fileName"))) {
                String message = translationService.translate(
                        "costCalculation.costCalculationDetails.window.costCalculation.documentsWasNotGenerated",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(new Date()));
                generated.setFieldValue("1");
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                date.setFieldValue(null);
                generated.setFieldValue("0");
                return;
            }
            costCalculation = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                    CostCalculateConstants.MODEL_COST_CALCULATION).get((Long) state.getFieldValue());
            try {
                generateCostCalDocuments(state, costCalculation);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void generateCostCalDocuments(final ComponentState state, final Entity costCalculation) throws IOException,
            DocumentException {
        Entity costCalculationWithFileName = updateFileName(costCalculation,
                getFullFileName((Date) costCalculation.getField("dateOfCalculation"), "Cost_calculation"),
                CostCalculateConstants.MODEL_COST_CALCULATION);
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .uniqueResult();
        costCalculationPdfService.generateDocument(costCalculationWithFileName, company, state.getLocale());
    }

    private String getFullFileName(final Date date, final String fileName) {
        return path + fileName + "_" + new SimpleDateFormat(DateUtils.REPORT_DATE_TIME_FORMAT).format(date) + "_";
    }

    private Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER, entityName).save(entity);
    }

}
