package com.qcadoo.mes.productionCounting.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.FinalProductAnalysisFor;
import com.qcadoo.mes.productionCounting.constants.FinalProductAnalysisGeneratorFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class FinalProductAnalysisGeneratorListeners {

    public void generateAnalysis(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity generator = form.getEntity();

        String analysisFor = generator.getStringField(FinalProductAnalysisGeneratorFields.ANALYSIS_FOR);
        if (FinalProductAnalysisFor.FINAL_PRODUCTS.getStringValue().equals(analysisFor)) {

        } else if (FinalProductAnalysisFor.BEFORE_ADDITIONAL_ACTIONS.getStringValue().equals(analysisFor)) {

        }

    }

}
