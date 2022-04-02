package com.qcadoo.mes.timeNormsForOperations.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologiesListHooksTNFO {

    public void toggleCopyTimeNormsFromOperationForTechnologiesButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem copyTimeNormsFromOperationForTechnologies = window.getRibbon().getGroupByName("norm")
                .getItemByName("copyTimeNormsFromOperationForTechnologies");

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        copyTimeNormsFromOperationForTechnologies.setEnabled(!grid.getSelectedEntities().isEmpty() &&
                grid.getSelectedEntities().stream().allMatch(e -> e.getStringField(TechnologyFields.STATE).equals(TechnologyState.DRAFT.getStringValue())));

        copyTimeNormsFromOperationForTechnologies.requestUpdate(true);
    }
}
