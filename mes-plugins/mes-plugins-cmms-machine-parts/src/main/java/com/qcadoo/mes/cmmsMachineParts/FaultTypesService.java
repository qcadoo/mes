package com.qcadoo.mes.cmmsMachineParts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class FaultTypesService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfFaultTypeAppliesToOthers(final Entity faultType) {

        return FaultTypeAppliesTo.from(faultType).compareTo(FaultTypeAppliesTo.NONE) == 0
                || faultType.getId() == getDefaultFaultType().getId();
    }

    public boolean checkIfFaultTypeAppliesToWorkstation(final Entity faultType, final Entity workstation) {
        return checkIfFaultTypeAppliesToWorkstationOrSubassembly(faultType, workstation, FaultTypeFields.WORKSTATIONS);
    }

    public boolean checkIfFaultTypeAppliesToSubassembly(final Entity faultType, final Entity subassembly) {
        return checkIfFaultTypeAppliesToWorkstationOrSubassembly(faultType, subassembly, FaultTypeFields.SUBASSEMBLIES);
    }

    public boolean checkIfFaultTypeAppliesToWorkstationOrSubassembly(final Entity faultType, final Entity entity,
            final String fieldName) {
        if (FaultTypeAppliesTo.from(faultType).compareTo(FaultTypeAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            return checkIfFaultTypeAppliesToEntity(faultType, entity, fieldName)
                    || checkIfFaultTypeAppliesToEntity(faultType, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                            FaultTypeFields.WORKSTATION_TYPES);
        } else if (FaultTypeAppliesTo.from(faultType).compareTo(FaultTypeAppliesTo.WORKSTATION_TYPE) == 0) {
            return checkIfFaultTypeAppliesToEntity(faultType, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                    FaultTypeFields.WORKSTATION_TYPES);
        }
        Entity defaultType = getDefaultFaultType();
        if (defaultType == null) {
            return false;
        }
        return faultType.getId() == defaultType.getId();
    }

    public boolean checkIfFaultTypeAppliesToEntity(final Entity faultType, final Entity entity, final String fieldToTest) {
        boolean result = faultType.getManyToManyField(fieldToTest).stream().anyMatch(e -> e.getId().equals(entity.getId()));
        return result;
    }

    public Entity getDefaultFaultType() {
        String other = translationService.translate("cmmsMachineParts.faultType.name.other", LocaleContextHolder.getLocale());
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_FAULT_TYPE)
                .find().add(SearchRestrictions.eq(FaultTypeFields.NAME, other)).setMaxResults(1).uniqueResult();
    }
}
