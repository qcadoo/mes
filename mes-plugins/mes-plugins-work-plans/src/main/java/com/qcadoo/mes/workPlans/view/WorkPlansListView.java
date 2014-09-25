package com.qcadoo.mes.workPlans.view;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

public class WorkPlansListView {

    private final RibbonActionItem deleteButton;

    private final GridComponent workPlansGrid;

    private final WindowComponent window;

    public static WorkPlansListView from(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");
        return new WorkPlansListView(window, deleteButton, grid);
    }

    WorkPlansListView(final WindowComponent window, final RibbonActionItem deleteButton, final GridComponent workPlansGrid) {
        this.window = window;
        this.deleteButton = deleteButton;
        this.workPlansGrid = workPlansGrid;
    }

    public void setUpDeleteButton(final boolean isEnabled, final String message) {
        deleteButton.setEnabled(isEnabled);
        deleteButton.setMessage(message);
        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }

    public List<Entity> getSelectedWorkPlans() {
        return workPlansGrid.getSelectedEntities();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        WorkPlansListView rhs = (WorkPlansListView) obj;
        return new EqualsBuilder().append(this.deleteButton, rhs.deleteButton).append(this.workPlansGrid, rhs.workPlansGrid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deleteButton).append(workPlansGrid).toHashCode();
    }
}
