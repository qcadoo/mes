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
package com.qcadoo.mes.cmmsMachineParts;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service public class SourceCostService {

    @Autowired private DataDefinitionService dataDefinitionService;

    public DataDefinition getSourceCostDD() {
        return dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_SOURCE_COST);
    }

    public Optional<Entity> findDefaultSourceCodeForFactory(final Entity factory){
        SearchCriteriaBuilder scb = getSourceCostDD().find();
        scb.add(SearchRestrictions.belongsTo(SourceCostFields.FACTORY, factory))
                .add(SearchRestrictions.eq(SourceCostFields.ACTIVE, true))
                .add(SearchRestrictions.eq(SourceCostFields.DEFAULT_COST, true));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    public Optional<Entity> findDefaultSourceCode(){
        SearchCriteriaBuilder scb = getSourceCostDD().find();
        scb.add(SearchRestrictions.isNull(SourceCostFields.FACTORY))
                .add(SearchRestrictions.eq(SourceCostFields.ACTIVE, true))
                .add(SearchRestrictions.eq(SourceCostFields.DEFAULT_COST, true));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }
}
