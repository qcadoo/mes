package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class AbstractFieldsForType implements FieldsForType {

    protected List<String> hiddenTabs;

    protected List<String> hiddenFields;

    protected List<String> gridsToClear;

    protected AbstractFieldsForType() {
        this.hiddenFields = Lists.newArrayList();
        this.hiddenTabs = Lists.newArrayList();
        this.gridsToClear = Lists.newArrayList();
    }

    protected AbstractFieldsForType(List<String> hiddenFields, List<String> hiddenTabs, List<String> gridsToClear) {
        this.hiddenTabs = hiddenTabs;
        this.hiddenFields = hiddenFields;
        this.gridsToClear = gridsToClear;
    }

    @Override
    public List<String> getHiddenFields() {
        return hiddenFields;
    }

    @Override
    public List<String> getHiddenTabs() {
        return hiddenTabs;
    }

    @Override
    public boolean shouldLockBasedOn() {
        return false;
    }

    @Override
    public List<String> getGridsToClear() {
        return gridsToClear;
    }
}
