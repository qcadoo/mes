package com.qcadoo.mes.cmmsMachineParts.roles;

import java.util.List;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;


public enum EventRoles {
    ROLE_EVENTS_DELETE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "actions", "delete");
        }
    },
    ROLE_EVENTS_START {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (!eventInState(view, MaintenanceEventState.IN_PROGRESS)) {
                lockFromRibbonGroup(view, "status", "startEvent");
            }
        }
    },
    ROLE_EVENTS_STOP {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (eventInState(view, MaintenanceEventState.IN_PROGRESS)) {
                lockFromRibbonGroup(view, "status", "startEvent");
            }
        }
    },
    ROLE_EVENTS_PLAN {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (!eventInState(view, MaintenanceEventState.EDITED)) {
                lockFromRibbonGroup(view, "status", "planEvent");
            }
        }
    },
    ROLE_EVENTS_REVOKE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "status", "revokeEvent");
        }
    },
    ROLE_EVENTS_CLOSE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (!eventInState(view, MaintenanceEventState.EDITED)) {
                lockFromRibbonGroup(view, "status", "closeEvent");
            }
        }
    },
    ROLE_EVENTS_ACCEPT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            if (eventInState(view, MaintenanceEventState.EDITED)) {
                lockFromRibbonGroup(view, "status", "closeEvent", "planEvent");
            }
        }
    },
    ROLE_EVENTS_ADD_FAILURE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "customActions", "addFailure");
        }
    },
    ROLE_EVENTS_ADD_ISSUE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "customActions", "addIssue");
        }
    },
    ROLE_EVENTS_ADD_PROPOSAL {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "customActions", "addProposal");
        }
    },
    ROLE_EVENTS_TAB_DOCUMENTS {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            hideTabs(view, "documentsTab");
        }
    },
    ROLE_EVENTS_TAB_HISTORY {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            hideTabs(view, "history");
        }
    },
    ROLE_EVENTS_TAB_ATTACHMENTS {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            hideTabs(view, "attachmentsTab");
        }
    },
    ROLE_EVENTS_SOURCE_COST_CHANGE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockComponents(view, "sourceCost");
        }
    },
    ROLE_EVENTS_DELETE_ATTACHMENT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockComponents(view, "attachments");
        }
    },
    ROLE_EVENTS_EDIT_AFTER_SAVE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            ComponentState contextTab = view.getComponentByReference("contextTab");
            FormComponent form = (FormComponent) view.getComponentByReference("form");
            if (contextTab == null && form != null && form.getEntity().getId() != null) {
                lockComponents(view, "number", "type", "factory", "division", "productionLine", "workstation", "subassembly",
                        "faultType", "description", "personReceiving", "sourceCost");
            }
        }
    },
    ROLE_EVENTS_VIEW_PARTS_TIME_DESCRIPTION {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            FormComponent form = (FormComponent) view.getComponentByReference("form");
            if (form != null && form.getEntity().getId() != null) {
                lockComponents(view, "machinePartsForEvent", "staffWorkTimes", "solutionDescription");
            }
        }
    },
    ROLE_EVENTS_LIST_EXPORT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "genericExport", "csv", "pdf");
        }
    },
    ROLE_PLANNED_EVENTS_ACTIONS_VIEW {
        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "plannedEvents", "showPlannedEvent");
        }
    };

    public void disableFieldsWhenNotInRole(ViewDefinitionState view) {

    }

    protected void lockFromRibbonGroup(ViewDefinitionState view, String groupName, String... items) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup ribbonGroup = ribbon.getGroupByName(groupName);
        if(ribbonGroup != null) {
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

    protected boolean eventInState(ViewDefinitionState view, MaintenanceEventState state) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity event = form.getEntity();
        String eventState = event.getStringField(MaintenanceEventFields.STATE);
        if (eventState == null) {
            GridComponent grid = (GridComponent) view.getComponentByReference("grid");
            List<Entity> entities = grid.getSelectedEntities();
            return entities.stream().allMatch(e -> state.getStringValue().equals(e.getStringField(MaintenanceEventFields.STATE)));
        }
        return state.getStringValue().equals(eventState);
    }
}
