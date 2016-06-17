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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.FaultTypesService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.hooks.EventHooks;
import com.qcadoo.mes.cmmsMachineParts.hooks.FactoryStructureForEventHooks;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementFields;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementType;
import com.qcadoo.mes.productionLines.factoryStructure.FactoryStructureElementsService;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class EventListeners {

    private static final Logger LOG = LoggerFactory.getLogger(EventListeners.class);

    private Long factoryStructureId;

    @Autowired
    private FactoryStructureForEventHooks factoryStructureForEventHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FaultTypesService faultTypesService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MaintenanceEventService maintenanceEventService;

    @Autowired
    private FactoryStructureElementsService factoryStructureElementsService;

    @Autowired
    private EventHooks eventHooks;

    public void addEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String args[]) {
        String eventType = args[0];
        factoryStructureId = Long.parseLong(args[1]);

        EntityTree tree = factoryStructureForEventHooks.getGeneratedTree();
        Optional<Entity> maybeElement = tree.stream().filter(element -> element.getId() == factoryStructureId).findFirst();

        if (!maybeElement.isPresent()) {
            viewDefinitionState.addMessage("cmmsMachineParts.error.elementNotSelected", ComponentState.MessageType.FAILURE);
            return;
        }
        Entity selectedElement = maybeElement.get();
        FactoryStructureElementType elementType = FactoryStructureElementType.of(selectedElement);
        if (elementType.compareTo(FactoryStructureElementType.COMPANY) == 0) {
            viewDefinitionState.addMessage("cmmsMachineParts.error.companySelected", ComponentState.MessageType.INFO);
            return;
        }
        if (elementType.compareTo(FactoryStructureElementType.FACTORY) == 0) {
            viewDefinitionState.addMessage("cmmsMachineParts.error.factorySelected", ComponentState.MessageType.INFO);
            return;
        }

        DataDefinition dataDefinition = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT);
        Entity maintenanceEvent = dataDefinition.create();
        if (elementType.compareTo(FactoryStructureElementType.DIVISION) == 0
                || elementType.compareTo(FactoryStructureElementType.PRODUCTION_LINE) == 0
                || MaintenanceEventType.parseString(eventType).compareTo(MaintenanceEventType.PROPOSAL) == 0) {
            maintenanceEvent.setField(MaintenanceEventFields.FAULT_TYPE, faultTypesService.getDefaultFaultType());
        }
        fillEventFieldsFromSelectedElement(maintenanceEvent, selectedElement);
        maintenanceEvent.setField(MaintenanceEventFields.TYPE, eventType);
        maintenanceEvent = dataDefinition.save(maintenanceEvent);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", maintenanceEvent.getId());
        viewDefinitionState.redirectTo(
                "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false, true,
                parameters);
    }

    public void factoryChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnDivision(view);
        fillSourceCost(view);
    }

    private void fillSourceCost(final ViewDefinitionState view) {
        LookupComponent factoryComponent = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FACTORY);
        if (factoryComponent.getEntity() == null) {
            clearFieldIfExists(view, MaintenanceEventFields.SOURCE_COST);
            eventHooks.fillSourceCost(view);
        } else {
            eventHooks.fillSourceCost(view, factoryComponent.getEntity());
        }
    }

    public void divisionChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnProductionLine(view);
    }

    public void productionLineChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnWorkstation(view);
        setEnabledForField(view, MaintenanceEventFields.WORKSTATION, state.getFieldValue() != null);
    }

    public void workstationChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnSubassembly(view);
        if (state.getFieldValue() == null) {
            clearFilterForFaultType(view, MaintenanceEventFields.WORKSTATION);
        }
    }

    public void subassemblyChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state.getFieldValue() == null) {
            clearFilterForFaultType(view, MaintenanceEventFields.SUBASSEMBLY);
        }
    }

    private void clearSelectionOnDivision(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.DIVISION);
        clearSelectionOnProductionLine(view);
    }

    private void clearSelectionOnProductionLine(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.PRODUCTION_LINE);
        clearSelectionOnWorkstation(view);
    }

    private void clearSelectionOnWorkstation(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.WORKSTATION);
        clearSelectionOnSubassembly(view);
    }

    private void clearSelectionOnSubassembly(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.SUBASSEMBLY);
    }

    private void clearFieldIfExists(ViewDefinitionState view, String reference) {
        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
        if (fieldComponent != null) {
            fieldComponent.setFieldValue(null);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private void setEnabledForField(ViewDefinitionState view, String reference, boolean enabled) {
        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
        fieldComponent.setEnabled(enabled);
    }

    private void clearFilterForFaultType(final ViewDefinitionState view, final String field) {
        LookupComponent faultType = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FAULT_TYPE);
        if (faultType != null) {
            FilterValueHolder filter = faultType.getFilterValue();
            if (filter.has(field)) {
                filter.remove(field);
                filter.remove(WorkstationFields.WORKSTATION_TYPE);
            }
            faultType.setFilterValue(filter);
        }
    }

    private void fillEventFieldsFromSelectedElement(Entity event, final Entity selectedElement) {

        Entity currentElement = selectedElement;
        while (currentElement != null
                && FactoryStructureElementType.of(currentElement).compareTo(FactoryStructureElementType.COMPANY) != 0) {
            Entity relatedEntity = factoryStructureElementsService.getRelatedEntity(currentElement);
            event.setField(currentElement.getStringField(FactoryStructureElementFields.ENTITY_TYPE), relatedEntity);
            currentElement = currentElement.getBelongsToField(FactoryStructureElementFields.PARENT);
        }
    }

    public void fillFieldValues(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (args.length < 2) {
            return;
        }
        for (int i = 0; i < args.length - 1; i = i + 2) {
            String field = args[i];
            String value = args[i + 1];
            if (field.equals(MaintenanceEventFields.TYPE)) {
                FieldComponent type = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.TYPE);
                type.setFieldValue(value);
            } else if (field.equals(MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT)) {
                FieldComponent type = (FieldComponent) view
                        .getComponentByReference(MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT);
                type.setFieldValue(value);
            } else if (field.equals(PlannedEventFields.PLANNED_EVENT_CONTEXT)) {
                FieldComponent type = (FieldComponent) view.getComponentByReference(PlannedEventFields.PLANNED_EVENT_CONTEXT);
                type.setFieldValue(value);
            }
        }
    }

    public void downloadAtachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("attachments");
        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().isEmpty()) {
            state.addMessage("technologies.technologyDetails.window.ribbon.atachments.nonSelectedAtachment",
                    ComponentState.MessageType.INFO);
            return;
        }
        DataDefinition attachmentDD = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, "eventAttachment");
        List<File> atachments = Lists.newArrayList();
        for (Long attachmentId : grid.getSelectedEntitiesIds()) {
            Entity attachment = attachmentDD.get(attachmentId);
            File file = new File(attachment.getStringField(TechnologyAttachmentFields.ATTACHMENT));
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

    public void showSolutions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (!checkAndShow("subassembly", "oldSolutionsSubassembly", view)) {
            if (!checkAndShow("workstation", "oldSolutionsWorkstation", view)) {
                if (!checkAndShow("productionLine", "oldSolutionsLine", view)) {
                    checkAndShow("division", "oldSolutionsDivision", view);
                }
            }
        }
    }

    private boolean checkAndShow(final String modelName, final String viewName, final ViewDefinitionState view) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(modelName);
        if (lookup.getEntity() != null) {
            String url = "../page/cmmsMachineParts/" + viewName + ".html?context={\"" + modelName + ".id\":\""
                    + lookup.getEntity().getId() + "\"}";
            view.openModal(url);
            return true;
        }
        return false;
    }

    public void validateIssueOrProposal(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity event = form.getPersistedEntityWithIncludedFormValues();
        if (event.getId() == null || MaintenanceEventState.of(event) == MaintenanceEventState.NEW
                || MaintenanceEventState.of(event) == MaintenanceEventState.IN_PROGRESS) {
            MaintenanceEventType type = MaintenanceEventType.from(event);
            if (type.compareTo(MaintenanceEventType.ISSUE) == 0 || type.compareTo(MaintenanceEventType.PROPOSAL) == 0) {
                List<Entity> existingEvents = maintenanceEventService.getExistingEventsForEvent(event,
                        MaintenanceEventType.ISSUE.getStringValue());
                existingEvents.addAll(maintenanceEventService.getExistingEventsForEvent(event,
                        MaintenanceEventType.PROPOSAL.getStringValue()));

                if (!existingEvents.isEmpty()) {
                    view.addMessage(
                            "cmmsMachineParts.error.existsOpenIssuesOrProposals",
                            ComponentState.MessageType.INFO,
                            false,
                            existingEvents.stream().map(e -> e.getStringField(MaintenanceEventFields.NUMBER))
                                    .collect(Collectors.joining(",")));
                }
            }
        }
    }

    public void showPlannedEvent(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity maintenanceEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT).get(form.getEntityId());

        Optional<Entity> plannedEvent = maintenanceEventService.getPlannedEventForMaintenanceEvent(maintenanceEvent);
        if (plannedEvent.isPresent()) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", plannedEvent.get().getId());
            view.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/plannedEventDetails.html", false, true,
                    parameters);
        }

    }

}
