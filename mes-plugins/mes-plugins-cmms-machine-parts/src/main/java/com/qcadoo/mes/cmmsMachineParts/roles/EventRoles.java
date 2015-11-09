package com.qcadoo.mes.cmmsMachineParts.roles;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
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
            lockFromRibbonGroup(view, "status", "startEvent");
        }
    },
    ROLE_EVENTS_STOP {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "status", "stopEvent");
        }
    },
    ROLE_EVENTS_PLAN {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "status", "planEvent");
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
            lockFromRibbonGroup(view, "status", "closeEvent");
        }
    },
    ROLE_EVENTS_ACCEPT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "status", "acceptEvent");
        }
    },
    ROLE_EVENTS_ADD_FAILURE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "customActions", "addFailure", "addFailure");
        }
    },
    ROLE_EVENTS_ADD_ISSUE {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "customActions", "addFailure", "addIssue");
        }
    },
    ROLE_EVENTS_ADD_PROPOSAL {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "customActions", "addFailure", "addProposal");
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
            FormComponent form = (FormComponent) view.getComponentByReference("form");
            if (form != null && form.getEntity().getId() != null) {
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
                lockComponents(view, "number", "staffWorkTimes", "solutionDescription");
            }
        }
    },
    ROLE_EVENTS_EXPORT {

        @Override
        public void disableFieldsWhenNotInRole(ViewDefinitionState view) {
            lockFromRibbonGroup(view, "genericExport", "csv", "pdf");
        }
    };

    /*
     * ROLE_EVENTS_PLANNER {
     * @Override public void disableFieldsForRole(ViewDefinitionState view) { WindowComponent window = (WindowComponent)
     * view.getComponentByReference("window"); Ribbon ribbon = window.getRibbon(); lockFromGroup(ribbon, "actions", "delete",
     * "accept"); } }, ROLE_EVENTS_ADMIN, ROLE_EVENTS_MAINTENANCE_WORKER {
     * @Override public void disableFieldsForRole(ViewDefinitionState view) { WindowComponent window = (WindowComponent)
     * view.getComponentByReference("window"); Ribbon ribbon = window.getRibbon(); lockFromGroup(ribbon, "actions", "delete",
     * "accept"); lockFromGroup(ribbon, "customActions", "addFailure", "addIssue"); lockFromGroup(ribbon, "status", "planEvent",
     * "revokeEvent"); hideTabs(view, "documentsTab", "history"); lockComponents(view, "sourceCost", "attachments"); } },
     * ROLE_EVENTS_PRODUCTION {
     * @Override public void disableFieldsForRole(ViewDefinitionState view) { WindowComponent window = (WindowComponent)
     * view.getComponentByReference("window"); Ribbon ribbon = window.getRibbon(); lockFromGroup(ribbon, "actions", "delete",
     * "accept"); lockFromGroup(ribbon, "customActions", "addFailure", "addIssue"); lockFromGroup(ribbon, "status", "startEvent",
     * "stopEvent", "acceptEvent", "planEvent", "revokeEvent"); hideTabs(view, "documentsTab", "history"); lockTabs(view,
     * "machinePartsTab"); lockComponents(view, "sourceCost", "attachments"); } }, ROLE_EVENTS_SHIFT_LEADER,
     * ROLE_EVENTS_QUALITY_ASSURANCE, ROLE_EVENTS_PALLET, ROLE_EVENTS_HR_WORKER;
     */
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

    protected void lockTabs(ViewDefinitionState view, String... tabs) {
        for (String tab : tabs) {
            ComponentState tabComponent = view.getComponentByReference(tab);
            if (tabComponent != null) {
                tabComponent.setEnabled(false);
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
