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
package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventAttachmentFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.hooks.EventHooks;
import com.qcadoo.mes.cmmsMachineParts.hooks.PlannedEventDetailsHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlannedEventDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedEventDetailsListeners.class);

    @Autowired
    private PlannedEventDetailsHooks plannedEventDetailsHooks;

    @Autowired
    private EventHooks eventHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FileService fileService;

    public void toggleEnabledFromBasedOn(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        eventHooks.toggleEnabledFromBasedOn(view);
    }

    public void toggleFieldsVisible(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        plannedEventDetailsHooks.toggleFieldsVisible(view);
    }

    public void downloadAttachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("attachments");
        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("cmmsMachineParts.plannedEventDetails.window.ribbon.attachments.nonSelectedAttachment",
                    ComponentState.MessageType.INFO);
            return;
        }
        DataDefinition attachmentDD = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT_ATTACHMENT);
        List<File> atachments = Lists.newArrayList();
        for (Long attachmentId : grid.getSelectedEntitiesIds()) {
            Entity attachment = attachmentDD.get(attachmentId);
            File file = new File(attachment.getStringField(PlannedEventAttachmentFields.ATTACHMENT));
            atachments.add(file);
        }

        File zipFile = null;
        try {
            zipFile = fileService.compressToZipFile(atachments, false);
        } catch (IOException e) {
            LOG.error("Unable to compress documents to zip file.", e);
            return;
        }

        view.redirectTo(fileService.getUrl(zipFile.getAbsolutePath()) + "?clean", true, false);
    }

    public void onAddExistingRelatedEvent(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (args.length < 1) {
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity currentEvent = form.getPersistedEntityWithIncludedFormValues();
        List<Long> addedRelatedEventsIds = parseIds(args[0]);
        for (Long addedRelatedEventId : addedRelatedEventsIds) {
            Entity addedEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                    CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).get(addedRelatedEventId);
            List<Entity> relatedEvents = Lists.newArrayList(addedEvent.getManyToManyField(PlannedEventFields.RELATED_EVENTS));
            relatedEvents.add(currentEvent);
            addedEvent.setField(PlannedEventFields.RELATED_EVENTS, relatedEvents);
            addedEvent = addedEvent.getDataDefinition().save(addedEvent);
            List<Entity> newRelatedEvents = addedEvent.getManyToManyField(PlannedEventFields.RELATED_EVENTS);
            if (newRelatedEvents.isEmpty()) {

            }
        }
        GridComponent relatedEventsGrid = (GridComponent) view.getComponentByReference(PlannedEventFields.RELATED_EVENTS);
        relatedEventsGrid.reloadEntities();
    }

    private List<Long> parseIds(final String ids) {
        List<Long> result = Lists.newArrayList();
        String[] splittedIds = ids.replace("[", "").replace("]", "").replace("\"", "").split(",");
        for (int i = 0; i < splittedIds.length; i++) {
            result.add(Long.parseLong(splittedIds[i]));
        }
        return result;
    }

    public void onRemoveRelatedEvents(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent relatedEventsGrid = (GridComponent) view.getComponentByReference(PlannedEventFields.RELATED_EVENTS);

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity currentEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).get(form.getEntityId());
        List<Entity> relatedEventsForCurrentEvent = Lists.newArrayList(currentEvent
                .getManyToManyField(PlannedEventFields.RELATED_EVENTS));
        List<Entity> relatedEventsToDelete = relatedEventsGrid.getSelectedEntities();
        for (Entity relatedEventToDelete : relatedEventsToDelete) {
            List<Entity> relatedEvents = Lists.newArrayList(relatedEventToDelete
                    .getManyToManyField(PlannedEventFields.RELATED_EVENTS));
            Optional<Entity> eventToDelete = relatedEvents.stream()
                    .filter(event -> event.getId().compareTo(currentEvent.getId()) == 0).findFirst();
            if (eventToDelete.isPresent()) {
                relatedEvents.remove(eventToDelete.get());
                relatedEventToDelete.setField(PlannedEventFields.RELATED_EVENTS, relatedEvents);
                relatedEventToDelete.getDataDefinition().save(relatedEventToDelete);
            }

            Optional<Entity> eventToDeleteFromCurrent = relatedEventsForCurrentEvent.stream()
                    .filter(event -> event.getId().compareTo(relatedEventToDelete.getId()) == 0).findFirst();

            if (eventToDeleteFromCurrent.isPresent()) {
                relatedEventsForCurrentEvent.remove(eventToDeleteFromCurrent.get());
            }
        }
        currentEvent.setField(PlannedEventFields.RELATED_EVENTS, relatedEventsForCurrentEvent);
        currentEvent.getDataDefinition().save(currentEvent);
    }

    public void showMaintenanceEvent(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity plannedEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).get(form.getEntityId());

        Entity maintenanceEvent = plannedEvent.getBelongsToField(PlannedEventFields.MAINTENANCE_EVENT);
        if (maintenanceEvent != null) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", maintenanceEvent.getId());
            view.redirectTo("/page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false,
                    true, parameters);
        }
    }

    public void showRecurringEvent(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity plannedEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).get(form.getEntityId());

        Entity maintenanceEvent = plannedEvent.getBelongsToField(PlannedEventFields.MAINTENANCE_EVENT);
        if (maintenanceEvent != null) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", maintenanceEvent.getId());
            view.redirectTo("/page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/recurringEventDetails.html", false,
                    true, parameters);
        }
    }
}
