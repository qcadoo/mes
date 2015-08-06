package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import java.util.List;

public interface FieldsForType {

    public List<String> getHiddenFields();

    public List<String> getHiddenTabs();

    public boolean shouldLockBasedOn();
}
