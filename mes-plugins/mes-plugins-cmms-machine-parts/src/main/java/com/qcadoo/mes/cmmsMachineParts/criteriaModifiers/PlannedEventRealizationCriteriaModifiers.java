package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PlannedEventRealizationCriteriaModifiers {

    public static final String L_EVENT = "plannedEvent";

    @Autowired
    DataDefinitionService dataDefinitionService;

    public void showActionsForEvent(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_EVENT)) {
            Long eventId = filterValueHolder.getLong(L_EVENT);
            Entity event = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, L_EVENT).get(eventId);
            List<Entity> actions = event.getManyToManyField(PlannedEventFields.ACTIONS);
            List<Long> actionsIds = actions.stream()
                    .map(action -> action.getBelongsToField(ActionForPlannedEventFields.ACTION).getId())
                    .collect(Collectors.toList());
            if (actionsIds.isEmpty()) {
                scb.add(SearchRestrictions.idEq(-1));
            } else {
                scb.add(SearchRestrictions.in("id", actionsIds));
            }
        }
    }
}
