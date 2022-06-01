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
package com.qcadoo.mes.basic.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class SubassemblyValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void validateUniquenessOfWorkstationAndType(final DataDefinition subassemblyDD, final Entity subassembly) {
        Entity workstationEntity = subassembly.getBelongsToField(SubassemblyFields.WORKSTATION);
        if (workstationEntity != null) {
            SearchCriterion criterionWorkstation = SearchRestrictions.belongsTo(SubassemblyFields.WORKSTATION, workstationEntity);
            SearchCriterion criterionType = SearchRestrictions.eq(SubassemblyFields.TYPE,
                    subassembly.getStringField(SubassemblyFields.TYPE));
            SearchCriterion criterionId = subassembly.getId() == null ? null : SearchRestrictions.idNe(subassembly.getId());

            long count = criterionId == null ? subassemblyDD.count(SearchRestrictions.and(criterionWorkstation, criterionType))
                    : subassemblyDD.count(SearchRestrictions.and(criterionWorkstation, criterionType, criterionId));
            if (count > 0) {
                subassembly.addError(subassemblyDD.getField(SubassemblyFields.WORKSTATION),
                        "basic.validate.global.error.uniquenessOfWorkstationAndType");
            }
        }
    }
}
