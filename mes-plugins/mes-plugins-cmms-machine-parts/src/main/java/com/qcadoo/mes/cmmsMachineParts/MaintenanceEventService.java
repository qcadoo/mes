/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.cmmsMachineParts;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT);
    }

    public Optional<Entity> getPlannedEventForMaintenanceEvent(final Entity maintenanceEvent) {
        List<Entity> plannedEvents = maintenanceEvent.getHasManyField(MaintenanceEventFields.PLANNED_EVENTS);

        if (plannedEvents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(plannedEvents.get(0));
    }

    public boolean existsNewEventsToNotification(Long currentUserId) {
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(currentUserId);
        Entity factory = user.getBelongsToField("factory");

       SearchCriteriaBuilder scb = getMaintenanceEventDD()
                .find()
                .setProjection(SearchProjections.alias(SearchProjections.rowCount(), "countrows"))
                .add(SearchRestrictions.eq(MaintenanceEventFields.SOUND_NOTIFICATIONS, true));

        if (factory != null) {
            scb = scb.add(SearchRestrictions.belongsTo("factory", factory));
        }

        scb = scb.addOrder(SearchOrders.asc("countrows"));

        Entity entity = scb.setFirstResult(0).setMaxResults(1).uniqueResult();
        if (entity.getLongField("countrows") > 0) {
            return true;
        }
        return false;
    }
}
