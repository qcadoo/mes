package com.qcadoo.mes.cmmsMachineParts;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MaintenanceEventService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean existOpenFailrueForObjectFromEvent(final Entity event) {

        return !getExistingEventsForEvent(event, MaintenanceEventType.FAILURE.getStringValue()).isEmpty();
    }

    public List<Entity> getExistingEventsForEvent(final Entity event, final String type) {
        SearchCriteriaBuilder scb = getMaintenanceEventDD().find();
        if (event.getId() != null) {
            scb = scb.add(SearchRestrictions.ne("id", event.getId()));
        }
        scb = scb.add(SearchRestrictions.eq(MaintenanceEventFields.TYPE, type));
        scb = scb.add(SearchRestrictions.or(
                SearchRestrictions.eq(MaintenanceEventFields.STATE, MaintenanceEventStateStringValues.NEW),
                SearchRestrictions.eq(MaintenanceEventFields.STATE, MaintenanceEventStateStringValues.IN_PROGRESS)));
        if (event.getBelongsToField(MaintenanceEventFields.SUBASSEMBLY) != null) {
            scb.add(SearchRestrictions.belongsTo(MaintenanceEventFields.SUBASSEMBLY,
                    event.getBelongsToField(MaintenanceEventFields.SUBASSEMBLY)));
        } else {
            scb = scb.add(SearchRestrictions.isNull(MaintenanceEventFields.SUBASSEMBLY));
        }

        if (event.getBelongsToField(MaintenanceEventFields.WORKSTATION) != null) {
            scb.add(SearchRestrictions.belongsTo(MaintenanceEventFields.WORKSTATION,
                    event.getBelongsToField(MaintenanceEventFields.WORKSTATION)));
        } else {
            scb = scb.add(SearchRestrictions.isNull(MaintenanceEventFields.WORKSTATION));
        }

        if (event.getBelongsToField(MaintenanceEventFields.PRODUCTION_LINE) != null) {
            scb.add(SearchRestrictions.belongsTo(MaintenanceEventFields.PRODUCTION_LINE,
                    event.getBelongsToField(MaintenanceEventFields.PRODUCTION_LINE)));
        } else {
            scb = scb.add(SearchRestrictions.isNull(MaintenanceEventFields.PRODUCTION_LINE));
        }

        if (event.getBelongsToField(MaintenanceEventFields.DIVISION) != null) {
            scb.add(SearchRestrictions.belongsTo(MaintenanceEventFields.DIVISION,
                    event.getBelongsToField(MaintenanceEventFields.DIVISION)));
        } else {
            scb = scb.add(SearchRestrictions.isNull(MaintenanceEventFields.DIVISION));
        }

        if (event.getBelongsToField(MaintenanceEventFields.FACTORY) != null) {
            scb.add(SearchRestrictions.belongsTo(MaintenanceEventFields.FACTORY,
                    event.getBelongsToField(MaintenanceEventFields.FACTORY)));
        } else {
            scb = scb.add(SearchRestrictions.isNull(MaintenanceEventFields.FACTORY));
        }
        return scb.list().getEntities();
    }

    public DataDefinition getMaintenanceEventDD() {
        return dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT);
    }
}
