package com.qcadoo.mes.deliveries.roles;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

public enum DeliveryRole {

    ROLE_DELIVERIES_STATES_DECLINE {
        @Override
        public void processRole(final ViewDefinitionState view) {
            lockFromRibbonGroup(view, STATUS, "declineDelivery");
        }
    },

    ROLE_DELIVERIES_STATES_OTHER {
        @Override
        public void processRole(final ViewDefinitionState view) {
            lockFromRibbonGroup(view, STATUS, "accept");
        }
    },

    ROLE_DELIVERIES_STATES_APPROVE {
        @Override
        public void processRole(final ViewDefinitionState view) {
            lockFromRibbonGroup(view, STATUS, "approveDelivery");
        }
    },

    ROLE_DELIVERIES_STATES_ACCEPT {
        @Override
        public void processRole(final ViewDefinitionState view) {
            lockFromRibbonGroup(view, STATUS, "approveDelivery");
        }
    },

    ROLE_DELIVERIES_EDIT {
        @Override
        public void processRole(final ViewDefinitionState view) {
            lockFromRibbonGroup(view, "actions", "delete");
        }
    };

    public static final String STATUS = "status";

    public void processRole(final ViewDefinitionState view) {

    }

    protected void lockFromRibbonGroup(ViewDefinitionState view, String groupName, String... items) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
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
}
