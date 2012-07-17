package com.qcadoo.mes.technologies.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.TreeComponent;

@Service
public class TechnologyDetailsListeners {

    public void setGridEditable(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        TreeComponent technologyTree = (TreeComponent) view.getComponentByReference("technologyTree");
        GridComponent outProducts = (GridComponent) view.getComponentByReference("outProducts");
        GridComponent inProducts = (GridComponent) view.getComponentByReference("inProducts");
        if (technologyTree.getSelectedEntityId() != null) {
            outProducts.setEditable(true);
            inProducts.setEditable(true);
        } else {
            outProducts.setEditable(false);
            inProducts.setEditable(false);
        }
    }
}
