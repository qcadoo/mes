package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class AbstractFieldsForType implements FieldsForType {

    protected List<String> hiddenTabs;

    protected List<String> hiddenFields;

    protected AbstractFieldsForType() {
        this.hiddenFields = Lists.newArrayList();
        this.hiddenTabs = Lists.newArrayList();
    }

    protected AbstractFieldsForType(List<String> hiddenFields, List<String> hiddenTabs) {
        this.hiddenTabs = hiddenTabs;
        this.hiddenFields = hiddenFields;
    }

    public List<String> getHiddenFields() {
        return hiddenFields;
    }

    public List<String> getHiddenTabs() {
        return hiddenTabs;
    }

    public boolean shouldLockBasedOn() {
        return false;
    }

}
