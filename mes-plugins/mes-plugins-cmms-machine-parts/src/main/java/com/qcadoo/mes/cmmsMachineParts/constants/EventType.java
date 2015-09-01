package com.qcadoo.mes.cmmsMachineParts.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public enum EventType {
    MAINTENANCE_EVENT {
        @Override public String getModelName() {
            return CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT;
        }
        @Override public String getMachinePartsName() {
            return MaintenanceEventFields.MACHINE_PARTS_FOR_EVENT;
        }
    },
    PLANNED_EVENT {
        @Override public String getModelName() {
            return CmmsMachinePartsConstants.MODEL_PLANNED_EVENT;
        }
        @Override public String getMachinePartsName() {
            return PlannedEventFields.MACHINE_PARTS_FOR_EVENT;
        }
    };

    public static EventType of(final Entity event) {
        DataDefinition dd = event.getDataDefinition();
        Preconditions.checkArgument(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER.equals(dd.getPluginIdentifier()));
        if (CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT.equals(dd.getName())) {
            return MAINTENANCE_EVENT;
        } else if (CmmsMachinePartsConstants.MODEL_PLANNED_EVENT.equals(dd.getName())) {
            return PLANNED_EVENT;
        }
        throw new IllegalArgumentException(String.format("Unsupported model type: '%s'", dd.getName()));
    }

    public abstract String getModelName();
    public abstract String getMachinePartsName();

}
