package com.qcadoo.mes.cmmsMachineParts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ActionsService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfActionAppliesToOthers(final Entity action) {

        return ActionAppliesTo.from(action).compareTo(ActionAppliesTo.NONE) == 0 || action.getId() == getDefaultAction().getId();
    }

    public boolean checkIfActionAppliesToWorkstation(final Entity action, final Entity workstation) {
        return checkIfActionAppliesToWorkstationOrSubassembly(action, workstation, ActionFields.WORKSTATIONS);
    }

    public boolean checkIfActionAppliesToSubassembly(final Entity action, final Entity subassembly) {
        return checkIfActionAppliesToWorkstationOrSubassembly(action, subassembly, ActionFields.SUBASSEMBLIES);
    }

    public boolean checkIfActionAppliesToWorkstationOrSubassembly(final Entity action, final Entity entity, final String fieldName) {
        if (ActionAppliesTo.from(action).compareTo(ActionAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            return checkIfActionAppliesToEntity(action, entity, fieldName)
                    || checkIfActionAppliesToEntity(action, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                            ActionFields.WORKSTATION_TYPES);
        } else if (ActionAppliesTo.from(action).compareTo(ActionAppliesTo.WORKSTATION_TYPE) == 0) {
            return checkIfActionAppliesToEntity(action, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                    ActionFields.WORKSTATION_TYPES);
        }
        Entity defaultAction = getDefaultAction();
        if (defaultAction == null) {
            return false;
        }
        return action.getId() == defaultAction.getId();
    }

    public boolean checkIfActionAppliesToEntity(final Entity action, final Entity entity, final String fieldToTest) {
        boolean result = action.getManyToManyField(fieldToTest).stream().anyMatch(e -> e.getId().equals(entity.getId()));
        return result;
    }

    public Entity getDefaultAction() {
        String other = translationService.translate("cmmsMachineParts.action.name.other", LocaleContextHolder.getLocale());
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_ACTION)
                .find().add(SearchRestrictions.eq(ActionFields.NAME, other)).setMaxResults(1).uniqueResult();
    }
}
