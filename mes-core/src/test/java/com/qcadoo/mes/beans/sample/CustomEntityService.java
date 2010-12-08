/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.beans.sample;

import java.util.Locale;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;

public class CustomEntityService {

    public void onUpdate(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("name", "update");
    }

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("age", 11);
    }

    public void onCreate(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("name", "create");
    }

    public void onDelete(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("name", "delete");
    }

    public boolean isEqualToQwerty(final DataDefinition dataDefinition, final Object object) {
        return String.valueOf(object).equals("qwerty");
    }

    public boolean hasAge18AndNameMrT(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("age").equals(18) && entity.getField("name").equals("Mr T")) {
            return true;
        } else {
            entity.addError(dataDefinition.getField("name"), "xxx");
            return false;
        }
    }

    public void onView(final ViewDefinitionState state, final Locale locale) {
        // TODO Auto-generated method stub
    }

    public void saveForm(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        // TODO Auto-generated method stub
    }

    public void generate(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        // TODO Auto-generated method stub
    }

}