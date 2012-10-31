/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.workPlans.util;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.model.api.Entity;

public class EntityOperationNumberComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = -3569221006218524772L;

    @Override
    public int compare(final Entity o1, final Entity o2) {
        return ((Entity) o1.getField("operation")).getField("number").toString()
                .compareTo(((Entity) o2.getField("operation")).getField("number").toString());
    }

}
