package com.qcadoo.mes.genealogies.print;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;

@Service
public class ReportService {

    public void generateReportForComponent(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        viewDefinitionState.redirectTo("/genealogies/genealogyForComponent.pdf?value=" + state.getFieldValue(), true);
    }

}
