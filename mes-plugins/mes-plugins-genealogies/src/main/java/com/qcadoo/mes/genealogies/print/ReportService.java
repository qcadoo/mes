package com.qcadoo.mes.genealogies.print;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public class ReportService {

    @Autowired
    private TranslationService translationService;

    public void generateReportForComponent(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponentState batchState = (FieldComponentState) viewDefinitionState.getComponentByReference("batch");
        if (state instanceof FormComponentState) {
            if (batchState != null && batchState.getFieldValue() != null) {
                viewDefinitionState
                        .redirectTo("/genealogies/genealogyForComponent.pdf?value=" + batchState.getFieldValue(), true);
            } else {
                state.addMessage(
                        translationService.translate("genealogies.genealogyForComponent.report.noBatch", state.getLocale()),
                        MessageType.FAILURE);
            }
        } else {
            state.addMessage(translationService.translate("genealogies.genealogyForComponent.report.noBatch", state.getLocale()),
                    MessageType.FAILURE);
        }

    }

}
