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
package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class WorkstationTypeCriteriaModifiers {

    public void showWorkstationTypesWithSubassembly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(WorkstationTypeFields.SUBASSEMBLY, true));
    }

    public void showWorkstationTypesWithoutSubassembly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.or(SearchRestrictions.eq(WorkstationTypeFields.SUBASSEMBLY, false),
                SearchRestrictions.isNull(WorkstationTypeFields.SUBASSEMBLY)));
    }
}
