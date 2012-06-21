package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;
import static com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields.STATUS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TechnologyDetailsViewHooks {

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("grid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(STATUS,
                Lists.newArrayList(SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }
}
