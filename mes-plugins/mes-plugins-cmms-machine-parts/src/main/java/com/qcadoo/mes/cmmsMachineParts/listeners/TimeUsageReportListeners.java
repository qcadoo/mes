package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TimeUsageReportListeners {

    public void printXlsReport(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity filterEntity = form.getEntity();
        if (filterEntity.getId() == null) {
            filterEntity = filterEntity.getDataDefinition().save(filterEntity);
        }
        view.redirectTo("/cmmsMachineParts/timeUsageReport.xls?filterId=" + filterEntity.getId(), true, false);
    }

    public void workersSelectionChanged(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        GridComponent workersGrid = (GridComponent) state.getComponentByReference("workers");
        String selected = (String) componentState.getFieldValue();
        if ("01all".equals(selected)) {
            workersGrid.setEntities(Collections.emptyList());
            workersGrid.setEnabled(false);
        } else {
            workersGrid.setEnabled(true);
        }
    }
}
