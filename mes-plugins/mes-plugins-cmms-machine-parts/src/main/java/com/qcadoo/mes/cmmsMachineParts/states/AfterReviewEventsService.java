package com.qcadoo.mes.cmmsMachineParts.states;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventState;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class AfterReviewEventsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    public void createAfterReviewEvents(final StateChangeContext stateChangeContext) {

        Entity plannedEvent = stateChangeContext.getOwner();
        if (!PlannedEventType.REVIEW.equals(PlannedEventType.from(plannedEvent))) {
            return;
        }
        List<Entity> incorrectActions = plannedEvent
                .getHasManyField(PlannedEventFields.ACTIONS)
                .stream()
                .filter(action -> action.getStringField(ActionForPlannedEventFields.STATE).equals(
                        ActionForPlannedEventState.INCORRECT.getStringValue())).collect(Collectors.toList());

        List<String> createdActions = Lists.newArrayList();
        for (Entity action : incorrectActions) {
            createdActions.add(createAfterReviewEventForIncorrectAction(action, plannedEvent));
        }
        if (!createdActions.isEmpty()) {
            stateChangeContext.addMessage("cmmsMachineParts.plannedEvent.afterReviewEventsCreated", StateMessageType.INFO, true,
                    createdActions.stream().collect(Collectors.joining(", ")));
        }
    }

    private String createAfterReviewEventForIncorrectAction(final Entity action, final Entity plannedEvent) {

        Entity afterReviewEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).create();

        String number = numberGeneratorService.generateNumber(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT);
        String description = action.getStringField(ActionForPlannedEventFields.REASON)
                + " "
                + translationService.translate("cmmsMachineParts.plannedEvent.basedOnEvent", LocaleContextHolder.getLocale(),
                        plannedEvent.getStringField(PlannedEventFields.NUMBER));
        afterReviewEvent.setField(PlannedEventFields.NUMBER, number);
        afterReviewEvent.setField(PlannedEventFields.TYPE, PlannedEventType.AFTER_REVIEW.getStringValue());
        afterReviewEvent.setField(PlannedEventFields.FACTORY, plannedEvent.getBelongsToField(PlannedEventFields.FACTORY));
        afterReviewEvent.setField(PlannedEventFields.DIVISION, plannedEvent.getBelongsToField(PlannedEventFields.DIVISION));
        afterReviewEvent.setField(PlannedEventFields.PRODUCTION_LINE,
                plannedEvent.getBelongsToField(PlannedEventFields.PRODUCTION_LINE));
        afterReviewEvent.setField(PlannedEventFields.WORKSTATION, plannedEvent.getBelongsToField(PlannedEventFields.WORKSTATION));
        afterReviewEvent.setField(PlannedEventFields.SUBASSEMBLY, plannedEvent.getBelongsToField(PlannedEventFields.SUBASSEMBLY));
        afterReviewEvent.setField(PlannedEventFields.DESCRIPTION, description);
        afterReviewEvent.setField(PlannedEventFields.BASED_ON, PlannedEventBasedOn.DATE.getStringValue());
        afterReviewEvent.setField(PlannedEventFields.AFTER_REVIEW, true);
        afterReviewEvent.getDataDefinition().save(afterReviewEvent);
        return number;
    }
}
