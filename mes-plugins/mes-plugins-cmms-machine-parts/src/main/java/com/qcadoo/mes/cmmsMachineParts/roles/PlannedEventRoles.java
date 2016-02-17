package com.qcadoo.mes.cmmsMachineParts.roles;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

import java.util.List;
import java.util.Optional;

public enum PlannedEventRoles {
    ROLE_PLANNED_EVENTS_DOCUMENTS {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            hideTabs(view, "documentsTab");
        }
    },
    ROLE_PLANNED_EVENTS_HISTORY {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            hideTabs(view, "historyTab");
        }
    },
    ROLE_PLANNED_EVENTS_ATTACHMENTS {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
        }
    },

    ROLE_PLANNED_EVENTS_EDIT_RELATED {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            GridComponent grid = (GridComponent) view.getComponentByReference("relatedEvents");
            if (grid != null) {
                grid.setEnabled(false);
            }
        }
    },
    ROLE_PLANNED_EVENTS_ATTACHMENTS_REMOVE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            GridComponent grid = (GridComponent) view.getComponentByReference("attachments");
            if (grid != null) {
                grid.setEnabled(false);
            }
        }
    },

    ROLE_PLANNED_EVENTS_STATES_START_STOP {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (shouldBeActive(view, PlannedEventState.ACCEPTED, Optional.of(PlannedEventType.METER_READING), false, false)
                    || shouldBeActive(view, PlannedEventState.IN_REALIZATION, Optional.of(PlannedEventType.METER_READING), true,
                            false)
                    || shouldBeActive(view, PlannedEventState.IN_EDITING, Optional.of(PlannedEventType.METER_READING), true,
                            false)) {
                lockFromRibbonGroup(view, "status", "realizedEvent");
            }
            lockFromRibbonGroup(view, "status", "startEvent", "stopEvent");
        }
    },
    ROLE_PLANNED_EVENTS_STATES_ACCEPT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (shouldBeActive(view, PlannedEventState.IN_REALIZATION, Optional.of(PlannedEventType.METER_READING), false, false)) {
                lockFromRibbonGroup(view, "status", "realizedEvent");
            }
            lockFromRibbonGroup(view, "status", "planEvent", "plannedEvent");
        }
    },
    ROLE_PLANNED_EVENTS_STATES_OTHER {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (!shouldBeActive(view, PlannedEventState.IN_EDITING, Optional.empty(), false, false)) {
                lockFromRibbonGroup(view, "status", "planEvent", "plannedEvent");
            }
            lockFromRibbonGroup(view, "status", "cancelEvent");
        }
    },
    ROLE_PLANNED_EVENTS_ADVANCED_EDIT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockComponents(view, PlannedEventFields.NUMBER, PlannedEventFields.TYPE, PlannedEventFields.DESCRIPTION,
                    PlannedEventFields.OWNER, PlannedEventFields.PLANNED_SEPARATELY, PlannedEventFields.REQUIRES_SHUTDOWN,
                    PlannedEventFields.BASED_ON, PlannedEventFields.DATE, PlannedEventFields.COUNTER,
                    PlannedEventFields.COUNTER_TOLERANCE, PlannedEventFields.DURATION, PlannedEventFields.COMPANY,
                    PlannedEventFields.SOURCE_COST);
        }
    },
    ROLE_PLANNED_EVENTS_BASIC_EDIT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {

            FormComponent form = (FormComponent) view.getComponentByReference("form");
            Entity plannedEvent = form.getEntity();
            if (plannedEvent.getDataDefinition().getName().equals(CmmsMachinePartsConstants.MODEL_PLANNED_EVENT)) {
                lockComponents(view, PlannedEventFields.FACTORY, PlannedEventFields.DIVISION, PlannedEventFields.PRODUCTION_LINE,
                        PlannedEventFields.WORKSTATION, PlannedEventFields.SUBASSEMBLY, PlannedEventFields.EFFECTIVE_COUNTER,
                        PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.ACTIONS,
                        PlannedEventFields.SOLUTION_DESCRIPTION, PlannedEventFields.RELATED_EVENTS,
                        PlannedEventFields.REALIZATIONS, PlannedEventFields.MACHINE_PARTS_FOR_EVENT);
            }
        }
    },
    ROLE_PLANNED_EVENTS_DATES_EDIT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockComponents(view, PlannedEventFields.START_DATE, PlannedEventFields.FINISH_DATE, PlannedEventFields.IS_DEADLINE,
                    PlannedEventFields.RESPONSIBLE_WORKERS);
        }

    },
    ROLE_PLANNED_EVENTS_ACTIONS_VIEW {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
        }
    },
    ROLE_PLANNED_EVENTS_ACTIONS_EDIT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "actions", "save", "copy", "saveBack", "cancel");
        }
    },
    ROLE_PLANNED_EVENTS_ACTIONS_REMOVE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "actions", "delete");
        }
    },
    ROLE_PLANNED_EVENTS_ACTIONS_ADD {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "actions", "copy");
        }
    },
    ROLE_EVENTS {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "maintenanceEvents", "showMaintenanceEvent");
        }
    };

    public void disableFieldsWhenNotInRole(ViewDefinitionState view) {

    }

    protected void lockFromRibbonGroup(ViewDefinitionState view, String groupName, String... items) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup ribbonGroup = ribbon.getGroupByName(groupName);
        if (ribbonGroup != null) {
            for (String item : items) {
                RibbonActionItem ribbonItem = ribbonGroup.getItemByName(item);
                if (ribbonItem != null) {
                    ribbonItem.setEnabled(false);
                    ribbonItem.requestUpdate(true);
                }
            }
        }
    }

    protected void hideTabs(ViewDefinitionState view, String... tabs) {
        for (String tab : tabs) {
            ComponentState tabComponent = view.getComponentByReference(tab);
            if (tabComponent != null) {
                tabComponent.setVisible(false);
            }
        }
    }

    protected void lockComponents(ViewDefinitionState view, String... components) {
        for (String component : components) {
            ComponentState componentState = view.getComponentByReference(component);
            if (componentState != null) {
                componentState.setEnabled(false);
            }
        }
    }

    protected boolean shouldBeActive(ViewDefinitionState view, PlannedEventState state, Optional<PlannedEventType> type,
            boolean typeEquals, boolean singleRow) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity event = form.getEntity();
        String eventState = event.getStringField(PlannedEventFields.STATE);

        if (eventState == null) {
            GridComponent grid = (GridComponent) view.getComponentByReference("grid");
            List<Entity> entities = grid.getSelectedEntities();
            if (singleRow) {
                if (entities.size() == 1) {
                    event = entities.get(0);
                    eventState = event.getStringField(PlannedEventFields.STATE);
                    return state.getStringValue().equals(eventState) && checkEventType(event, type, typeEquals);

                } else {
                    return false;
                }
            }
            boolean statesEquals = entities.stream().allMatch(
                    e -> state.getStringValue().equals(e.getStringField(MaintenanceEventFields.STATE)));
            if (type.isPresent()) {
                if (typeEquals) {
                    return statesEquals
                            && entities.stream().allMatch(
                                    e -> type.get().getStringValue().equals(e.getStringField(PlannedEventFields.TYPE)));
                } else {
                    return statesEquals
                            && entities.stream().noneMatch(
                                    e -> type.get().getStringValue().equals(e.getStringField(PlannedEventFields.TYPE)));
                }
            }
            return statesEquals;
        }

        return state.getStringValue().equals(eventState) && checkEventType(event, type, typeEquals);
    }

    private boolean checkEventType(final Entity event, Optional<PlannedEventType> type, boolean typeEquals) {
        String eventType = event.getStringField(PlannedEventFields.TYPE);
        boolean isType = true;
        if (type.isPresent()) {
            if (typeEquals) {
                isType = type.get().getStringValue().endsWith(eventType);
            } else {
                isType = !type.get().getStringValue().endsWith(eventType);
            }
        }
        return isType;

    }

}
