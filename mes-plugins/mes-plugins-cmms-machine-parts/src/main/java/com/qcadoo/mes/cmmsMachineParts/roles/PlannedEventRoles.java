package com.qcadoo.mes.cmmsMachineParts.roles;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
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
    ROLE_PLANNED_EVENTS_ATTACHMENTS_REMOVE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            GridComponent grid = (GridComponent) view.getComponentByReference("attachments");
            if (grid != null) {
                grid.setEnabled(false);
            }
        }
    },
    ROLE_PLANNED_EVENTS_STATES_ACCEPT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            FormComponent form = (FormComponent) view.getComponentByReference("form");
            Entity plannedEvent = form.getEntity();
            if (plannedEvent.getDataDefinition().getName().equals(CmmsMachinePartsConstants.MODEL_PLANNED_EVENT)) {
                PlannedEventState state = PlannedEventState.of(plannedEvent);
                PlannedEventType type = PlannedEventType.from(plannedEvent);
                if (state.compareTo(PlannedEventState.IN_REALIZATION) == 0 && type.compareTo(PlannedEventType.METER_READING) != 0) {
                    lockFromRibbonGroup(view, "status", "realizedEvent");
                }
            } else {
                lockFromRibbonGroup(view, "status", "realizedEvent");
            }
        }
    },
    ROLE_PLANNED_EVENTS_STATES_REALIZED {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            FormComponent form = (FormComponent) view.getComponentByReference("form");
            Entity plannedEvent = form.getEntity();
            if (plannedEvent.getDataDefinition().getName().equals(CmmsMachinePartsConstants.MODEL_PLANNED_EVENT)) {
                PlannedEventState state = PlannedEventState.of(plannedEvent);
                PlannedEventType type = PlannedEventType.from(plannedEvent);
                if ((state.compareTo(PlannedEventState.ACCEPTED) == 0 && type.compareTo(PlannedEventType.METER_READING) != 0)
                        || (state.compareTo(PlannedEventState.IN_REALIZATION) == 0 && type
                                .compareTo(PlannedEventType.METER_READING) == 0)) {
                    lockFromRibbonGroup(view, "status", "realizedEvent");
                }
            } else {
                lockFromRibbonGroup(view, "status", "realizedEvent");
            }
        }
    },
    ROLE_PLANNED_EVENTS_STATES_OTHER {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "status", "planEvent", "cancelEvent", "plannedEvent");
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

}
