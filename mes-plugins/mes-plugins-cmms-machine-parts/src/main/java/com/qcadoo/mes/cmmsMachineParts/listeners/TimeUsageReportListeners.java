package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.TimeUsageReportFilterFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TimeUsageReportListeners {

    public void printXlsReport(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        form.performEvent(view, "save");
        Entity filterEntity = form.getPersistedEntityWithIncludedFormValues();
        if (workersPresent(filterEntity, view) && form.isValid()) {
            view.redirectTo("/cmmsMachineParts/timeUsageReport.xls?filterId=" + filterEntity.getId(), true, false);
        }
    }

    private boolean workersPresent(Entity filterEntity, ViewDefinitionState view) {
        String selection = filterEntity.getStringField(TimeUsageReportFilterFields.WORKERS_SELECTION);
        if ("02selected".equals(selection)) {
            EntityList workers = filterEntity.getHasManyField(TimeUsageReportFilterFields.WORKERS);
            if (workers.isEmpty()) {
                view.addMessage("cmmsMachineParts.timeUsageReport.error.noWorkers", MessageType.FAILURE);
                return false;
            }
            return true;
        }
        return true;
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
