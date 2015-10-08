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
package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class AbstractFieldsForType implements FieldsForType {

    protected List<String> hiddenTabs;

    protected List<String> hiddenFields;

    protected List<String> gridsToClear;

    public AbstractFieldsForType() {
        this.hiddenFields = Lists.newArrayList();
        this.hiddenTabs = Lists.newArrayList();
        this.gridsToClear = Lists.newArrayList();
    }

    public AbstractFieldsForType(List<String> hiddenFields, List<String> hiddenTabs, List<String> gridsToClear) {
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

    @Override
    public void addHiddenField(String hiddenField) {
        hiddenFields.add(hiddenField);
    }

    @Override
    public void addHiddenTab(String hiddenTab) {
        hiddenTabs.add(hiddenTab);
    }

    @Override
    public void addGridToClear(String gridToClear) {
        gridsToClear.add(gridToClear);
    }
}
