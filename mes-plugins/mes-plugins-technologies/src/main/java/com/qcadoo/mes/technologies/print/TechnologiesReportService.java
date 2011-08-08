package com.qcadoo.mes.technologies.print;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologiesReportService {

    @Autowired
    private TranslationService translationService;

    public final void printTechnologyDetailsReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            viewDefinitionState.redirectTo("/technologies/technologyDetailsReport." + args[0] + "?id=" + state.getFieldValue(),
                    false, false);
        } else {
            state.addMessage(translationService.translate("technologies.report.componentError", state.getLocale()),
                    MessageType.FAILURE);
        }
    }

}
