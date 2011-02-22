package com.qcadoo.mes.qualityControl.print;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public class QualityControlsReportService {

    @Autowired
    private TranslationService translationService;

    public void printQualityControlReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponentState) {
            FieldComponentState dateFrom = (FieldComponentState) viewDefinitionState.getComponentByReference("dateFrom");
            FieldComponentState dateTo = (FieldComponentState) viewDefinitionState.getComponentByReference("dateTo");
            if (dateFrom != null && dateTo != null && dateFrom.getFieldValue() != null && dateTo.getFieldValue() != null) {
                if ("forOrder".equals(args[1])) {
                    viewDefinitionState.redirectTo(
                            "/qualityControl/qualityControlForOrder." + args[0] + "?dateFrom=" + dateFrom.getFieldValue()
                                    + "&dateTo=" + dateTo.getFieldValue(), true, false);
                } else if ("forUnit".equals(args[1])) {
                    viewDefinitionState.redirectTo(
                            "/qualityControl/qualityControlForUnit." + args[0] + "?dateFrom=" + dateFrom.getFieldValue()
                                    + "&dateTo=" + dateTo.getFieldValue(), true, false);
                } else if ("forBatch".equals(args[1])) {
                    viewDefinitionState.redirectTo(
                            "/qualityControl/qualityControlForBatch." + args[0] + "?dateFrom=" + dateFrom.getFieldValue()
                                    + "&dateTo=" + dateTo.getFieldValue(), true, false);
                } else if ("forOperation".equals(args[1])) {
                    viewDefinitionState.redirectTo("/qualityControl/qualityControlForOperation." + args[0] + "?dateFrom="
                            + dateFrom.getFieldValue() + "&dateTo=" + dateTo.getFieldValue(), true, false);
                }
            } else {
                state.addMessage(translationService.translate("qualityControl.report.invalidDates", state.getLocale()),
                        MessageType.FAILURE);
            }
        } else {
            state.addMessage(translationService.translate("qualityControl.report.invalidDates", state.getLocale()),
                    MessageType.FAILURE);
        }
    }
}
