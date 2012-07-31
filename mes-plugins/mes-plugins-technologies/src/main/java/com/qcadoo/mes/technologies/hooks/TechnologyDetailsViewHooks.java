package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields.STATUS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TechnologyDetailsViewHooks {

    private static final String OUT_PRODUCTS_REFERENCE = "outProducts";

    private static final String IN_PRODUCTS_REFERENCE = "inProducts";

    private static final String TECHNOLOGY_TREE_REFERENCE = "technologyTree";

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("grid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(STATUS,
                Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void setTreeTabEditable(final ViewDefinitionState view) {
        final boolean treeTabShouldBeEnabled = TechnologyState.DRAFT.equals(getTechnologyState(view))
                && technologyIsAlreadySaved(view);
        for (String componentReference : Sets
                .newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE, TECHNOLOGY_TREE_REFERENCE)) {
            view.getComponentByReference(componentReference).setEnabled(treeTabShouldBeEnabled);
        }
    }

    private boolean technologyIsAlreadySaved(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        return form.getEntityId() != null;
    }

    private TechnologyState getTechnologyState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        final Entity technology = form.getEntity();
        TechnologyState state = TechnologyState.DRAFT;
        if (technology != null) {
            state = TechnologyState.parseString(technology.getStringField(TechnologyFields.STATE));
        }
        return state;
    }

}
